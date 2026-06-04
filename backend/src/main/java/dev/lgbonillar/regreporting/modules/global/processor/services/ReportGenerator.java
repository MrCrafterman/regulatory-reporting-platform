package dev.lgbonillar.regreporting.modules.global.processor.services;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;

import java.util.UUID;

public interface ReportGenerator {

    String getReportFilename(UUID jobId);

    byte[] generate(ValidationResult validation, byte[] originalFile);
}