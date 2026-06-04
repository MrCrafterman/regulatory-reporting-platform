package dev.lgbonillar.regreporting.modules.global.validation;

import java.util.List;

public record ValidationResult(
        boolean valid,
        List<ValidationFinding> findings
) {
    public static ValidationResult success() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult withFindings(List<ValidationFinding> findings) {
        return new ValidationResult(!hasErrors(findings), findings);
    }

    public boolean hasErrors() {
        return findings.stream().anyMatch(f -> f.severity() == ValidationSeverity.ERROR);
    }

    public boolean hasWarnings() {
        return findings.stream().anyMatch(f -> f.severity() == ValidationSeverity.WARNING);
    }

    public int errorCount() {
        return (int) findings.stream().filter(f -> f.severity() == ValidationSeverity.ERROR).count();
    }

    public int warningCount() {
        return (int) findings.stream().filter(f -> f.severity() == ValidationSeverity.WARNING).count();
    }

    private static boolean hasErrors(List<ValidationFinding> findings) {
        return findings.stream().anyMatch(f -> f.severity() == ValidationSeverity.ERROR);
    }
}