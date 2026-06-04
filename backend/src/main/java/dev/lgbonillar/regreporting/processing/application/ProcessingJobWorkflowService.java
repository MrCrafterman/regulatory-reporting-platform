package dev.lgbonillar.regreporting.processing.application;

import dev.lgbonillar.regreporting.modules.global.processor.*;
import dev.lgbonillar.regreporting.modules.global.validation.*;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobStatus;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobTransitionSource;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobResponse;
import dev.lgbonillar.regreporting.shared.ForbiddenOperationException;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobWorkflowService {

    private final ProcessingJobQueryService processingJobQueryService;
    private final ProcessingJobHistoryService processingJobHistoryService;
    private final CurrentUserProvider currentUserProvider;
    private final ProcessingJobFindingService processingJobFindingService;
    private final ValidatorRegistry validatorRegistry;
    private final ProcessorRegistry processorRegistry;
    private final FileStorageService fileStorageService;

    public ProcessingJobWorkflowService(
            ProcessingJobQueryService processingJobQueryService,
            ProcessingJobHistoryService processingJobHistoryService,
            CurrentUserProvider currentUserProvider,
            ProcessingJobFindingService processingJobFindingService,
            ValidatorRegistry validatorRegistry,
            ProcessorRegistry processorRegistry,
            FileStorageService fileStorageService
    ) {
        this.processingJobQueryService = processingJobQueryService;
        this.processingJobHistoryService = processingJobHistoryService;
        this.currentUserProvider = currentUserProvider;
        this.processingJobFindingService = processingJobFindingService;
        this.validatorRegistry = validatorRegistry;
        this.processorRegistry = processorRegistry;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ProcessingJobResponse startProcessing(UUID jobId) {
        ProcessingJob processingJob = processingJobQueryService.getJob(jobId);

        processingJobQueryService.requireCanView(processingJob);
        processingJob.getUploadedFile().ensureCanBeProcessed();

        User currentUser = currentUserProvider.getCurrentUser();
        requireRole(currentUser, UserRole.ANALYST, "start processing");
        requireOwnsJob(currentUser, processingJob);

        ProcessingJobStatus previousStatus = processingJob.getStatus();

        processingJob.startProcessing(currentUser);

        processingJobHistoryService.recordTransition(
                processingJob,
                previousStatus,
                processingJob.getStatus(),
                ProcessingJobTransitionSource.USER,
                currentUser,
                "Analyst started processing"
        );

        ValidationResult validationResult = runValidation(processingJob);

        if (validationResult.hasErrors()) {
            processingJob.markProcessingFailed("Validation failed with " + validationResult.errorCount() + " error(s)");
            
            processingJobHistoryService.recordTransition(
                    processingJob,
                    processingJob.getStatus(),
                    processingJob.getStatus(),
                    ProcessingJobTransitionSource.SYSTEM,
                    null,
                    "Validation failed"
            );

            return processingJobQueryService.toProcessingJobResponse(processingJob);
        }

        ProcessingJobContext jobContext = createJobContext(processingJob);

        ReportProcessor processor = processorRegistry.findByFileType(FileType.XLSX)
                .orElseThrow(() -> new IllegalStateException("No processor found for file type XLSX"));

        ProcessingResult result = processor.process(jobContext, validationResult);

        processingJobFindingService.replaceProcessingJobFindings(
                processingJob,
                convertToFindingCommands(result.findings())
        );

        ProcessingJobStatus processingStatus = processingJob.getStatus();

        if (result.hasErrors()) {
            processingJob.markProcessingFailed(result.message());
        } else {
            processingJob.markProcessingCompleted();
        }

        processingJobHistoryService.recordTransition(
                processingJob,
                processingStatus,
                processingJob.getStatus(),
                ProcessingJobTransitionSource.SYSTEM,
                null,
                result.message()
        );

        return processingJobQueryService.toProcessingJobResponse(processingJob);
    }

    @Transactional
    public ProcessingJobResponse completeProcessing(UUID jobId) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        job.getUploadedFile().ensureCanBeProcessed();

        ProcessingJobStatus previousStatus = job.getStatus();

        job.markProcessingCompleted();

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.SYSTEM,
                null,
                "Automatic processing completed successfully"
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse failProcessing(UUID jobId, String reason) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        ProcessingJobStatus previousStatus = job.getStatus();

        job.markProcessingFailed(reason);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.SYSTEM,
                null,
                reason.trim()
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse approve(UUID jobId) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        job.getUploadedFile().ensureCanBeProcessed();

        User administrator = currentUserProvider.getCurrentUser();
        requireRole(administrator, UserRole.ADMINISTRATOR, "approve processing jobs");

        ProcessingJobStatus previousStatus = job.getStatus();

        job.approve(administrator);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                "Administrator approved submission"
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse reject(UUID jobId, String reason) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        job.getUploadedFile().ensureCanBeProcessed();

        User administrator = currentUserProvider.getCurrentUser();
        requireRole(administrator, UserRole.ADMINISTRATOR, "reject processing jobs");

        ProcessingJobStatus previousStatus = job.getStatus();

        job.reject(administrator, reason);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                reason.trim()
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse revoke(UUID jobId, String reason) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        job.getUploadedFile().ensureCanBeProcessed();

        User administrator = currentUserProvider.getCurrentUser();
        requireRole(administrator, UserRole.ADMINISTRATOR, "revoke processing jobs");

        ProcessingJobStatus previousStatus = job.getStatus();

        job.revoke(administrator, reason);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                reason.trim()
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    private ValidationResult runValidation(ProcessingJob processingJob) {
        try {
            byte[] fileContent = fileStorageService.loadAsBytes(processingJob.getUploadedFile().getStoragePath());
            
            GenericValidator validator = validatorRegistry.findByFileType(FileType.XLSX)
                    .orElseThrow(() -> new IllegalStateException("No validator found for file type XLSX"));

            return validator.validate(fileContent);
        } catch (IOException e) {
            return ValidationResult.withFindings(List.of(
                    ValidationFinding.error(
                            "GLOBAL__FILE_READ_ERROR",
                            "Could not read file: " + e.getMessage(),
                            ValidationScope.SYSTEM
                    )
            ));
        }
    }

    private ProcessingJobContext createJobContext(ProcessingJob processingJob) {
        return new ProcessingJobContext() {
            @Override
            public UUID getJobId() {
                return processingJob.getId();
            }

            @Override
            public UUID getFileId() {
                return processingJob.getUploadedFile().getId();
            }

            @Override
            public String getOriginalFilename() {
                return processingJob.getUploadedFile().getOriginalFilename();
            }

            @Override
            public byte[] getFileContent() {
                try {
                    return fileStorageService.loadAsBytes(processingJob.getUploadedFile().getStoragePath());
                } catch (IOException e) {
                    return new byte[0];
                }
            }

            @Override
            public User getTriggeredBy() {
                return processingJob.getTriggeredBy();
            }

            @Override
            public java.time.LocalDateTime getTriggeredAt() {
                return processingJob.getTriggeredAt();
            }
        };
    }

    private List<dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand> convertToFindingCommands(
            List<ProcessingFinding> findings
    ) {
        return findings.stream()
                .map(f -> new dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand(
                        convertSeverity(f.severity()),
                        convertScope(f.scope()),
                        f.code(),
                        f.message(),
                        null,
                        null,
                        null,
                        f.category(),
                        null,
                        null,
                        null
                ))
                .toList();
    }

    private dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity convertSeverity(ProcessingSeverity severity) {
        return switch (severity) {
            case ERROR -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity.ERROR;
            case WARNING -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity.WARNING;
            case INFO -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity.INFO;
        };
    }

    private dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope convertScope(ProcessingScope scope) {
        return switch (scope) {
            case SUMMARY -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope.BUSINESS_RULE;
            case CHART -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope.BUSINESS_RULE;
            case REPORT -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope.BUSINESS_RULE;
            case NOTIFICATION -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope.SYSTEM;
            case API_CALL -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope.SYSTEM;
            case SYSTEM -> dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope.SYSTEM;
        };
    }

    private void requireRole(User user, UserRole role, String action) {
        if (!user.hasRole(role)) {
            throw new ForbiddenOperationException(
                    "You are not allowed to " + action
            );
        }
    }

    private void requireOwnsJob(User user, ProcessingJob job) {
        String uploadedBy = job.getUploadedFile()
                .getUploadedBy()
                .getUsername();

        if (!uploadedBy.equals(user.getUsername())) {
            throw new ForbiddenOperationException(
                    "You can only start processing jobs uploaded by your user"
            );
        }
    }
}
