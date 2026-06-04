package dev.lgbonillar.regreporting.modules.demo.validation.rules;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationScope;
import dev.lgbonillar.regreporting.modules.global.validation.rules.ExcelBusinessRules;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DemoBusinessRules {

    private final ExcelBusinessRules globalRules;

    public DemoBusinessRules() {
        this.globalRules = new ExcelBusinessRules();
    }

    public void validateMultiplication(
            String sheetName,
            Row row,
            String leftHeader,
            int leftColumnIndex,
            String rightHeader,
            int rightColumnIndex,
            String resultHeader,
            int resultColumnIndex,
            List<ValidationFinding> findings,
            String code,
            String message
    ) {
        try {
            BigDecimal left = getNumericValue(row, leftColumnIndex);
            BigDecimal right = getNumericValue(row, rightColumnIndex);
            BigDecimal actual = getNumericValue(row, resultColumnIndex);
            BigDecimal expected = left.multiply(right);

            if (!sameAmount(expected, actual)) {
                ValidationFinding finding = new ValidationFinding(
                        dev.lgbonillar.regreporting.modules.global.validation.ValidationSeverity.ERROR,
                        ValidationScope.BUSINESS_RULE,
                        code,
                        message,
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(resultColumnIndex + 1),
                        resultHeader,
                        actual.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        expected.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        actual.setScale(2, RoundingMode.HALF_UP).toPlainString()
                );
                findings.add(finding);
            }
        } catch (IllegalArgumentException exception) {
            // Skip - numeric validation errors handled separately
        }
    }

    public void validateDateGreaterThanOrEqual(
            String sheetName,
            Row row,
            String startHeader,
            int startColumnIndex,
            String endHeader,
            int endColumnIndex,
            List<ValidationFinding> findings,
            String code,
            String message
    ) {
        try {
            var startDate = getDateValue(row, startColumnIndex);
            var endDate = getDateValue(row, endColumnIndex);

            if (endDate.isBefore(startDate)) {
                ValidationFinding finding = new ValidationFinding(
                        dev.lgbonillar.regreporting.modules.global.validation.ValidationSeverity.ERROR,
                        ValidationScope.BUSINESS_RULE,
                        code,
                        message,
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(endColumnIndex + 1),
                        endHeader,
                        endDate.toString(),
                        endHeader + " >= " + startHeader,
                        startDate.toString()
                );
                findings.add(finding);
            }
        } catch (IllegalArgumentException exception) {
            ValidationFinding finding = new ValidationFinding(
                    dev.lgbonillar.regreporting.modules.global.validation.ValidationSeverity.ERROR,
                    ValidationScope.ROW_DATA,
                    "GLOBAL__INVALID_DATE_VALUE",
                    "The date values must be valid Excel dates",
                    sheetName,
                    row.getRowNum() + 1,
                    null,
                    startHeader + " / " + endHeader,
                    null,
                    "Valid Excel date",
                    "Invalid or blank date"
            );
            findings.add(finding);
        }
    }

    public void validateUniqueValue(
            String sheetName,
            Row row,
            String header,
            int columnIndex,
            Set<String> seenValues,
            List<ValidationFinding> findings,
            String code,
            String message
    ) {
        String value = getCellText(row, columnIndex);

        if (value.isBlank()) {
            return;
        }

        if (!seenValues.add(value)) {
            ValidationFinding finding = new ValidationFinding(
                    dev.lgbonillar.regreporting.modules.global.validation.ValidationSeverity.ERROR,
                    ValidationScope.BUSINESS_RULE,
                    code,
                    message,
                    sheetName,
                    row.getRowNum() + 1,
                    String.valueOf(columnIndex + 1),
                    header,
                    value,
                    "Unique value",
                    value
            );
            findings.add(finding);
        }
    }

    private String getCellText(Row row, int columnIndex) {
        if (columnIndex < 0 || row == null) {
            return "";
        }
        var cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return cell.toString().trim();
    }

    private BigDecimal getNumericValue(Row row, int columnIndex) {
        var cell = row.getCell(columnIndex);
        if (cell == null) {
            throw new IllegalArgumentException("Cell is blank");
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> new BigDecimal(cell.getStringCellValue().trim().replace(",", ""));
            case FORMULA -> BigDecimal.valueOf(cell.getNumericCellValue());
            default -> throw new IllegalArgumentException("Cell is not numeric");
        };
    }

    private java.time.LocalDate getDateValue(Row row, int columnIndex) {
        var cell = row.getCell(columnIndex);
        if (cell == null) {
            throw new IllegalArgumentException("Cell is blank");
        }
        if (!org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            throw new IllegalArgumentException("Cell is not a date");
        }
        return cell.getDateCellValue()
                .toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }

    private boolean sameAmount(BigDecimal expected, BigDecimal actual) {
        return expected.setScale(2, RoundingMode.HALF_UP)
                .compareTo(actual.setScale(2, RoundingMode.HALF_UP)) == 0;
    }
}