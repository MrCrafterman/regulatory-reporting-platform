package dev.lgbonillar.regreporting.modules.global.validation;

public enum GlobalRules {

    WORKBOOK_WITHOUT_SHEETS("GLOBAL__WORKBOOK_WITHOUT_SHEETS", "The workbook must contain at least one sheet", ValidationScope.FILE_STRUCTURE),
    MISSING_REQUIRED_SHEET("GLOBAL__MISSING_REQUIRED_SHEET", "The workbook does not contain a required sheet", ValidationScope.SHEET_STRUCTURE),
    EMPTY_SHEET("GLOBAL__EMPTY_SHEET", "The sheet does not contain any rows", ValidationScope.SHEET_STRUCTURE),
    SHEET_WITHOUT_HEADER("GLOBAL__SHEET_WITHOUT_HEADER", "The sheet does not contain a readable header row", ValidationScope.SHEET_STRUCTURE),
    SHEET_WITHOUT_DATA_ROWS("GLOBAL__SHEET_WITHOUT_DATA_ROWS", "The sheet contains headers but no data rows", ValidationScope.ROW_DATA),
    DUPLICATED_HEADER("GLOBAL__DUPLICATED_HEADER", "The sheet contains a duplicated header", ValidationScope.COLUMN_STRUCTURE),
    MISSING_REQUIRED_COLUMN("GLOBAL__MISSING_REQUIRED_COLUMN", "A required column is missing", ValidationScope.COLUMN_STRUCTURE),
    INVALID_COLUMN_ORDER("GLOBAL__INVALID_COLUMN_ORDER", "The column does not match the expected layout", ValidationScope.COLUMN_STRUCTURE),
    REQUIRED_VALUE_MISSING("GLOBAL__REQUIRED_VALUE_MISSING", "A required value is missing", ValidationScope.ROW_DATA),
    INVALID_NUMERIC_VALUE("GLOBAL__INVALID_NUMERIC_VALUE", "The value is not a valid number", ValidationScope.ROW_DATA),
    INVALID_DATE_VALUE("GLOBAL__INVALID_DATE_VALUE", "The value is not a valid date", ValidationScope.ROW_DATA),
    INVALID_CURRENCY_VALUE("GLOBAL__INVALID_CURRENCY_VALUE", "The value is not a valid currency", ValidationScope.ROW_DATA),
    DUPLICATED_VALUE("GLOBAL__DUPLICATED_VALUE", "The value must be unique within the dataset", ValidationScope.BUSINESS_RULE),
    FILE_READ_ERROR("GLOBAL__FILE_READ_ERROR", "The file could not be read", ValidationScope.SYSTEM),
    UNSUPPORTED_FILE_TYPE("GLOBAL__UNSUPPORTED_FILE_TYPE", "The file type is not supported", ValidationScope.SYSTEM);

    private final String code;
    private final String message;
    private final ValidationScope scope;

    GlobalRules(String code, String message, ValidationScope scope) {
        this.code = code;
        this.message = message;
        this.scope = scope;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ValidationScope getScope() {
        return scope;
    }

    public ValidationFinding toFinding(String sheetName, Integer rowNumber, String columnName, String fieldName, String rejectedValue, String expectedValue, String actualValue) {
        return ValidationFinding.error(code, message, scope, sheetName, rowNumber, columnName, fieldName, rejectedValue, expectedValue, actualValue);
    }

    public ValidationFinding toFinding(String sheetName, Integer rowNumber, String columnName, String fieldName) {
        return ValidationFinding.error(code, message, scope, sheetName, rowNumber, columnName, fieldName);
    }

    public ValidationFinding toFinding() {
        return ValidationFinding.error(code, message, scope);
    }
}