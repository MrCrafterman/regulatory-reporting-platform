package dev.lgbonillar.regreporting.modules.global.processor;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractReportProcessor implements ReportProcessor {

    private final List<ProcessorExtension> extensions = new ArrayList<>();

    public void addExtension(ProcessorExtension extension) {
        this.extensions.add(extension);
    }

    protected void executeBeforeProcess(ProcessingJobContext job, ValidationResult validationResult) {
        for (ProcessorExtension extension : extensions) {
            extension.beforeProcess(job, validationResult);
        }
    }

    protected void executeAfterProcess(ProcessingJobContext job, ProcessingResult result) {
        for (ProcessorExtension extension : extensions) {
            extension.afterProcess(job, result);
        }
    }

    protected void executeOnError(ProcessingJobContext job, Exception error) {
        for (ProcessorExtension extension : extensions) {
            extension.onError(job, error);
        }
    }

    @Override
    public ProcessingResult process(ProcessingJobContext job, ValidationResult validationResult) {
        executeBeforeProcess(job, validationResult);

        try {
            ProcessingResult result = doProcess(job, validationResult);
            executeAfterProcess(job, result);
            return result;
        } catch (Exception e) {
            executeOnError(job, e);
            throw e;
        }
    }

    protected abstract ProcessingResult doProcess(ProcessingJobContext job, ValidationResult validationResult);
}