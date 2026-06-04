package dev.lgbonillar.regreporting.modules.global.validation.rules;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ExcelCellRules {

    public BigDecimal getNumericValue(Row row, int columnIndex) {
        var cell = row.getCell(columnIndex);

        if (cell == null) {
            throw new IllegalArgumentException("Cell is blank");
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> parseBigDecimal(cell.getStringCellValue());
            case FORMULA -> BigDecimal.valueOf(cell.getNumericCellValue());
            default -> throw new IllegalArgumentException("Cell is not numeric");
        };
    }

    public LocalDate getDateValue(Row row, int columnIndex) {
        var cell = row.getCell(columnIndex);

        if (cell == null) {
            throw new IllegalArgumentException("Cell is blank");
        }

        if (!org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            throw new IllegalArgumentException("Cell is not a date");
        }

        return cell.getDateCellValue()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public boolean isNumeric(Row row, int columnIndex) {
        try {
            getNumericValue(row, columnIndex);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isDate(Row row, int columnIndex) {
        try {
            getDateValue(row, columnIndex);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value is blank");
        }

        return new BigDecimal(value.trim().replace(",", ""));
    }
}