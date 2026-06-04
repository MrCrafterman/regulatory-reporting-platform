package dev.lgbonillar.regreporting.modules.global.processor;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;

import java.util.List;

public record ProcessingResult(
        String processorCode,
        String message,
        List<ProcessingFinding> findings
) {

    public boolean hasErrors() {
        return findings.stream()
                .anyMatch(f -> f.severity() == ProcessingSeverity.ERROR);
    }

    public static ProcessingResult successful(String processorCode, String message) {
        return new ProcessingResult(processorCode, message, List.of());
    }

    public static ProcessingResult withFindings(
            String processorCode,
            String message,
            List<ProcessingFinding> findings
    ) {
        return new ProcessingResult(processorCode, message, findings);
    }
}