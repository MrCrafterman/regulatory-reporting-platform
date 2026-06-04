package dev.lgbonillar.regreporting.modules.global.validation.rules;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationScope;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExcelHeaderRules {

    private final ExcelCellReader cellReader;

    public ExcelHeaderRules() {
        this.cellReader = new ExcelCellReader();
    }

    public void validateNoDuplicatedHeaders(Sheet sheet, int headerRowIndex, List<ValidationFinding> findings) {
        Row headerRow = sheet.getRow(headerRowIndex);
        Set<String> headers = new HashSet<>();

        if (headerRow == null) {
            return;
        }

        for (int columnIndex = headerRow.getFirstCellNum(); columnIndex < headerRow.getLastCellNum(); columnIndex++) {
            String header = cellReader.getCellText(headerRow, columnIndex);

            if (header.isBlank()) {
                continue;
            }

            String normalizedHeader = header.trim().toLowerCase();

            if (!headers.add(normalizedHeader)) {
                findings.add(ValidationFinding.error(
                        "GLOBAL__DUPLICATED_HEADER",
                        "The sheet contains a duplicated header",
                        ValidationScope.COLUMN_STRUCTURE,
                        sheet.getSheetName(),
                        headerRowIndex + 1,
                        String.valueOf(columnIndex + 1),
                        header
                ));
            }
        }
    }

    public void validateExactHeaders(Sheet sheet, int headerRowIndex, List<String> expectedHeaders, List<ValidationFinding> findings) {
        Row headerRow = sheet.getRow(headerRowIndex);

        for (int index = 0; index < expectedHeaders.size(); index++) {
            String expectedHeader = expectedHeaders.get(index);
            String actualHeader = cellReader.getCellText(headerRow, index);

            if (actualHeader.isBlank()) {
                findings.add(ValidationFinding.error(
                        "GLOBAL__MISSING_REQUIRED_COLUMN",
                        "A required column is missing",
                        ValidationScope.COLUMN_STRUCTURE,
                        sheet.getSheetName(),
                        headerRowIndex + 1,
                        String.valueOf(index + 1),
                        expectedHeader
                ));
                continue;
            }

            if (!actualHeader.equals(expectedHeader)) {
                findings.add(ValidationFinding.error(
                        "GLOBAL__INVALID_COLUMN_ORDER",
                        "The column does not match the expected layout",
                        ValidationScope.COLUMN_STRUCTURE,
                        sheet.getSheetName(),
                        headerRowIndex + 1,
                        String.valueOf(index + 1),
                        expectedHeader
                ));
            }
        }
    }
}