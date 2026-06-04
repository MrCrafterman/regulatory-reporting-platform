package dev.lgbonillar.regreporting.modules.global.processor;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;

public interface ProcessingJobContext {

    java.util.UUID getJobId();

    java.util.UUID getFileId();

    String getOriginalFilename();

    byte[] getFileContent();

    dev.lgbonillar.regreporting.users.domain.User getTriggeredBy();

    java.time.LocalDateTime getTriggeredAt();
}