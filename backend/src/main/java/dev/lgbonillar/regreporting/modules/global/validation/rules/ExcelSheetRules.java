package dev.lgbonillar.regreporting.modules.global.validation.rules;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationScope;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

public class ExcelSheetRules {

    private final ExcelCellReader cellReader;

    public ExcelSheetRules() {
        this.cellReader = new ExcelCellReader();
    }

    public void validateNotEmpty(Sheet sheet, List<ValidationFinding> findings) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            findings.add(ValidationFinding.error(
                    "GLOBAL__EMPTY_SHEET",
                    "The sheet does not contain any rows",
                    ValidationScope.SHEET_STRUCTURE,
                    sheet.getSheetName()
            ));
        }
    }

    public void validateHasHeader(Sheet sheet, int headerRowIndex, List<ValidationFinding> findings) {
        Row headerRow = sheet.getRow(headerRowIndex);

        if (headerRow == null || cellReader.isRowBlank(headerRow)) {
            findings.add(ValidationFinding.error(
                    "GLOBAL__SHEET_WITHOUT_HEADER",
                    "The sheet does not contain a readable header row",
                    ValidationScope.SHEET_STRUCTURE,
                    sheet.getSheetName(),
                    headerRowIndex + 1
            ));
        }
    }

    public void validateHasDataRows(Sheet sheet, int headerRowIndex, List<ValidationFinding> findings) {
        int firstDataRowIndex = headerRowIndex + 1;

        if (sheet.getLastRowNum() < firstDataRowIndex) {
            findings.add(ValidationFinding.error(
                    "GLOBAL__SHEET_WITHOUT_DATA_ROWS",
                    "The sheet contains headers but no data rows",
                    ValidationScope.ROW_DATA,
                    sheet.getSheetName(),
                    firstDataRowIndex + 1
            ));
        }
    }
}