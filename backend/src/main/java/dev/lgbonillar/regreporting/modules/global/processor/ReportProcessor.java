package dev.lgbonillar.regreporting.modules.global.processor;

import dev.lgbonillar.regreporting.modules.global.validation.FileType;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;

public interface ReportProcessor {

    String code();

    FileType supports();

    ProcessingResult process(ProcessingJobContext job, ValidationResult validationResult);
}