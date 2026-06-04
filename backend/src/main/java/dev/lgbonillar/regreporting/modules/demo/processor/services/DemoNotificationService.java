package dev.lgbonillar.regreporting.modules.demo.processor.services;

import dev.lgbonillar.regreporting.modules.global.processor.ProcessingJobContext;
import dev.lgbonillar.regreporting.modules.global.processor.ProcessingResult;
import dev.lgbonillar.regreporting.modules.global.processor.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(DemoNotificationService.class);

    @Override
    public void sendApprovalNotification(ProcessingJobContext job, ProcessingResult result) {
        log.info("Sending approval notification for job {} - File: {}",
                job.getJobId(), job.getOriginalFilename());
    }

    @Override
    public void sendRejectionNotification(ProcessingJobContext job, String reason) {
        log.info("Sending rejection notification for job {} - Reason: {}",
                job.getJobId(), reason);
    }

    @Override
    public void sendProcessingCompletedNotification(ProcessingJobContext job) {
        log.info("Processing completed for job {} - File: {}",
                job.getJobId(), job.getOriginalFilename());
    }

    @Override
    public void sendErrorNotification(ProcessingJobContext job, Exception error) {
        log.error("Error processing job {} - File: {} - Error: {}",
                job.getJobId(), job.getOriginalFilename(), error.getMessage());
    }
}