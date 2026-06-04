package dev.lgbonillar.regreporting.modules.global.processor.services;

import dev.lgbonillar.regreporting.modules.global.processor.ProcessingJobContext;
import dev.lgbonillar.regreporting.modules.global.processor.ProcessingResult;

public interface NotificationService {

    void sendApprovalNotification(ProcessingJobContext job, ProcessingResult result);

    void sendRejectionNotification(ProcessingJobContext job, String reason);

    void sendProcessingCompletedNotification(ProcessingJobContext job);

    void sendErrorNotification(ProcessingJobContext job, Exception error);
}