package dev.lgbonillar.regreporting.modules.global.validation.rules;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationScope;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class ExcelWorkbookRules {

    public void validateHasSheets(Workbook workbook, List<ValidationFinding> findings) {
        if (workbook.getNumberOfSheets() == 0) {
            findings.add(ValidationFinding.error(
                    "GLOBAL__WORKBOOK_WITHOUT_SHEETS",
                    "The workbook must contain at least one sheet",
                    ValidationScope.FILE_STRUCTURE
            ));
        }
    }

    public void validateRequiredSheet(Workbook workbook, String expectedSheetName, List<ValidationFinding> findings) {
        if (workbook.getSheet(expectedSheetName) == null) {
            findings.add(ValidationFinding.error(
                    "GLOBAL__MISSING_REQUIRED_SHEET",
                    "The workbook does not contain a required sheet",
                    ValidationScope.SHEET_STRUCTURE,
                    expectedSheetName
            ));
        }
    }

    public void validateRequiredSheets(Workbook workbook, List<String> expectedSheetNames, List<ValidationFinding> findings) {
        for (String expectedSheetName : expectedSheetNames) {
            validateRequiredSheet(workbook, expectedSheetName, findings);
        }
    }
}