package dev.lgbonillar.regreporting.modules.global.processor;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;

public interface ProcessorExtension {

    default void beforeProcess(ProcessingJobContext job, ValidationResult validationResult) {
    }

    default void afterProcess(ProcessingJobContext job, ProcessingResult result) {
    }

    default void onError(ProcessingJobContext job, Exception error) {
    }
}