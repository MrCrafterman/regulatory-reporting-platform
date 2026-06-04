package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.modules.global.validation.FileType;
import dev.lgbonillar.regreporting.modules.global.validation.GenericValidator;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;
import dev.lgbonillar.regreporting.modules.global.validation.ValidatorRegistry;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class UploadedFileValidationService {

    private final ValidatorRegistry validatorRegistry;
    private final UploadedFileValidationRunService validationRunService;
    private final UploadedFileFindingService findingService;
    private final FileStorageService fileStorageService;

    public UploadedFileValidationService(
            ValidatorRegistry validatorRegistry,
            UploadedFileValidationRunService validationRunService,
            UploadedFileFindingService findingService,
            FileStorageService fileStorageService
    ) {
        this.validatorRegistry = validatorRegistry;
        this.validationRunService = validationRunService;
        this.findingService = findingService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public UploadedFileStatus validate(UploadedFile uploadedFile, UploadedFileValidationRunSource source, String username) {
        ValidationResult result;
        try {
            byte[] fileContent = fileStorageService.loadAsBytes(uploadedFile.getStoragePath());

            GenericValidator validator = validatorRegistry.findByFileType(FileType.XLSX)
                    .orElseThrow(() -> new IllegalStateException("No validator found for file type XLSX"));

            result = validator.validate(fileContent);
        } catch (IOException e) {
            result = ValidationResult.withFindings(List.of(
                    ValidationFinding.error(
                            "GLOBAL__FILE_READ_ERROR",
                            "Could not read file: " + e.getMessage(),
                            GenericValidator dev.lgbonillar.regreporting.modules.global.validation.ValidationScope.SYSTEM
                    )
            ));
        }

        UploadedFileValidationRunStatus runStatus = toRunStatus(result);

        UploadedFileValidationRun validationRun = validationRunService.createValidationRun(
                uploadedFile,
                runStatus,
                source,
                validationSummary(runStatus),
                username
        );

        if (!result.findings().isEmpty()) {
            findingService.saveFindings(validationRun, uploadedFile, convertToFindingCommands(result.findings()));
        }

        return toFileStatus(runStatus);
    }

    private UploadedFileValidationRunStatus toRunStatus(ValidationResult result) {
        if (!result.hasErrors()) {
            return UploadedFileValidationRunStatus.PASSED;
        }

        boolean hasSystemError = result.findings().stream()
                .anyMatch(finding -> finding.severity() == dev.lgbonillar.regreporting.modules.global.validation.ValidationSeverity.ERROR
                        && finding.scope() == dev.lgbonillar.regreporting.modules.global.validation.ValidationScope.SYSTEM);

        return hasSystemError
                ? UploadedFileValidationRunStatus.SYSTEM_FAILED
                : UploadedFileValidationRunStatus.FAILED;
    }

    private UploadedFileStatus toFileStatus(UploadedFileValidationRunStatus runStatus) {
        return switch (runStatus) {
            case PASSED -> UploadedFileStatus.STORED;
            case FAILED -> UploadedFileStatus.PENDING_CORRECTION;
            case SYSTEM_FAILED -> UploadedFileStatus.FAILED;
        };
    }

    private String validationSummary(UploadedFileValidationRunStatus status) {
        return switch (status) {
            case PASSED -> "Uploaded file validation passed";
            case FAILED -> "Uploaded file validation found issues";
            case SYSTEM_FAILED -> "Uploaded file validation failed due to a system error";
        };
    }

    private List<dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand> convertToFindingCommands(
            List<ValidationFinding> findings
    ) {
        return findings.stream()
                .map(f -> new dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand(
                        convertSeverity(f.severity()),
                        convertScope(f.scope()),
                        f.code(),
                        f.message(),
                        f.sheetName(),
                        f.rowNumber(),
                        f.columnName(),
                        f.fieldName(),
                        f.rejectedValue(),
                        f.expectedValue(),
                        f.actualValue()
                ))
                .toList();
    }

    private ProcessingFindingSeverity convertSeverity(dev.lgbonillar.regreporting.modules.global.validation.ValidationSeverity severity) {
        return switch (severity) {
            case ERROR -> ProcessingFindingSeverity.ERROR;
            case WARNING -> ProcessingFindingSeverity.WARNING;
            case INFO -> ProcessingFindingSeverity.INFO;
        };
    }

    private ProcessingFindingScope convertScope(dev.lgbonillar.regreporting.modules.global.validation.ValidationScope scope) {
        return switch (scope) {
            case FILE_STRUCTURE, SHEET_STRUCTURE, COLUMN_STRUCTURE, ROW_DATA, BUSINESS_RULE, CROSS_FILE_VALIDATION -> ProcessingFindingScope.BUSINESS_RULE;
            case SYSTEM -> ProcessingFindingScope.SYSTEM;
        };
    }
}