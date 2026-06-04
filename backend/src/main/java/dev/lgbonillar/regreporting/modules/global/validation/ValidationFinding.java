package dev.lgbonillar.regreporting.modules.global.validation;

public record ValidationFinding(
        ValidationSeverity severity,
        ValidationScope scope,
        String code,
        String message,
        String sheetName,
        Integer rowNumber,
        String columnName,
        String fieldName,
        String rejectedValue,
        String expectedValue,
        String actualValue
) {
    public static ValidationFinding error(String code, String message, ValidationScope scope) {
        return new ValidationFinding(ValidationSeverity.ERROR, scope, code, message, null, null, null, null, null, null, null);
    }

    public static ValidationFinding error(String code, String message, ValidationScope scope, String sheetName) {
        return new ValidationFinding(ValidationSeverity.ERROR, scope, code, message, sheetName, null, null, null, null, null, null);
    }

    public static ValidationFinding error(String code, String message, ValidationScope scope, String sheetName, Integer rowNumber) {
        return new ValidationFinding(ValidationSeverity.ERROR, scope, code, message, sheetName, rowNumber, null, null, null, null, null);
    }

    public static ValidationFinding error(String code, String message, ValidationScope scope, String sheetName, Integer rowNumber, String columnName) {
        return new ValidationFinding(ValidationSeverity.ERROR, scope, code, message, sheetName, rowNumber, columnName, null, null, null, null);
    }

    public static ValidationFinding error(String code, String message, ValidationScope scope, String sheetName, Integer rowNumber, String columnName, String fieldName) {
        return new ValidationFinding(ValidationSeverity.ERROR, scope, code, message, sheetName, rowNumber, columnName, fieldName, null, null, null);
    }

    public static ValidationFinding error(String code, String message, ValidationScope scope, String sheetName, Integer rowNumber, String columnName, String fieldName, String rejectedValue, String expectedValue, String actualValue) {
        return new ValidationFinding(ValidationSeverity.ERROR, scope, code, message, sheetName, rowNumber, columnName, fieldName, rejectedValue, expectedValue, actualValue);
    }

    public static ValidationFinding warning(String code, String message, ValidationScope scope) {
        return new ValidationFinding(ValidationSeverity.WARNING, scope, code, message, null, null, null, null, null, null, null);
    }

    public static ValidationFinding warning(String code, String message, ValidationScope scope, String sheetName, Integer rowNumber, String columnName, String fieldName) {
        return new ValidationFinding(ValidationSeverity.WARNING, scope, code, message, sheetName, rowNumber, columnName, fieldName, null, null, null);
    }

    public static ValidationFinding info(String code, String message, ValidationScope scope) {
        return new ValidationFinding(ValidationSeverity.INFO, scope, code, message, null, null, null, null, null, null, null);
    }
}