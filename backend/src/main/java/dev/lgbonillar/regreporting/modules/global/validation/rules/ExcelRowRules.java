package dev.lgbonillar.regreporting.modules.global.validation.rules;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationScope;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

public class ExcelRowRules {

    private final ExcelCellReader cellReader;

    public ExcelRowRules() {
        this.cellReader = new ExcelCellReader();
    }

    public void validateRequiredValues(
            String sheetName,
            Row row,
            List<String> headers,
            List<ValidationFinding> findings
    ) {
        for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
            String header = headers.get(columnIndex);
            String value = cellReader.getCellText(row, columnIndex);

            if (value.isBlank()) {
                findings.add(ValidationFinding.error(
                        "GLOBAL__REQUIRED_VALUE_MISSING",
                        "A required value is missing",
                        ValidationScope.ROW_DATA,
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(columnIndex + 1),
                        header
                ));
            }
        }
    }
}