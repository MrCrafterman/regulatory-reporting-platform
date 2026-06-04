package dev.lgbonillar.regreporting.modules.global.validation;

import dev.lgbonillar.regreporting.modules.global.validation.rules.ExcelWorkbookRules;
import dev.lgbonillar.regreporting.modules.global.validation.rules.ExcelSheetRules;
import dev.lgbonillar.regreporting.modules.global.validation.rules.ExcelHeaderRules;
import dev.lgbonillar.regreporting.modules.global.validation.rules.ExcelRowRules;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractExcelValidator implements GenericValidator {

    protected final ExcelWorkbookRules workbookRules;
    protected final ExcelSheetRules sheetRules;
    protected final ExcelHeaderRules headerRules;
    protected final ExcelRowRules rowRules;

    protected AbstractExcelValidator() {
        this.workbookRules = new ExcelWorkbookRules();
        this.sheetRules = new ExcelSheetRules();
        this.headerRules = new ExcelHeaderRules();
        this.rowRules = new ExcelRowRules();
    }

    @Override
    public FileType supports() {
        return FileType.XLSX;
    }

    @Override
    public ValidationResult validate(byte[] fileContent) {
        List<ValidationFinding> findings = new ArrayList<>();

        try {
            Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileContent));
            validateStructure(workbook, findings);
            validateData(workbook, findings);
        } catch (IOException e) {
            findings.add(ValidationFinding.error(
                    GlobalRules.FILE_READ_ERROR.getCode(),
                    "Could not read the Excel file: " + e.getMessage(),
                    ValidationScope.SYSTEM
            ));
        }

        return ValidationResult.withFindings(findings);
    }

    protected void validateStructure(Workbook workbook, List<ValidationFinding> findings) {
        workbookRules.validateHasSheets(workbook, findings);

        if (workbook.getNumberOfSheets() == 0) {
            return;
        }

        List<String> requiredSheets = getRequiredSheets();
        for (String sheetName : requiredSheets) {
            workbookRules.validateRequiredSheet(workbook, sheetName, findings);
        }

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            sheetRules.validateNotEmpty(sheet, findings);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                continue;
            }

            int headerRowIndex = getHeaderRowIndex();
            sheetRules.validateHasHeader(sheet, headerRowIndex, findings);
            sheetRules.validateHasDataRows(sheet, headerRowIndex, findings);

            if (sheet.getPhysicalNumberOfRows() <= headerRowIndex) {
                continue;
            }

            headerRules.validateNoDuplicatedHeaders(sheet, headerRowIndex, findings);

            List<String> expectedHeaders = getExpectedHeaders(sheet.getSheetName());
            if (!expectedHeaders.isEmpty()) {
                headerRules.validateExactHeaders(sheet, headerRowIndex, expectedHeaders, findings);
            }
        }
    }

    protected void validateData(Workbook workbook, List<ValidationFinding> findings) {
        List<String> requiredSheets = getRequiredSheets();

        for (String sheetName : requiredSheets) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                continue;
            }

            int headerRowIndex = getHeaderRowIndex();
            if (sheet.getPhysicalNumberOfRows() <= headerRowIndex) {
                continue;
            }

            List<String> headers = getExpectedHeaders(sheetName);
            if (headers.isEmpty()) {
                continue;
            }

            validateDataRows(sheet, headerRowIndex, headers, findings);
        }
    }

    protected void validateDataRows(Sheet sheet, int headerRowIndex, List<String> headers, List<ValidationFinding> findings) {
        int firstDataRowIndex = headerRowIndex + 1;

        for (int rowIndex = firstDataRowIndex; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            var row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            rowRules.validateRequiredValues(sheet.getSheetName(), row, headers, findings);
        }
    }

    protected abstract List<String> getRequiredSheets();

    protected abstract int getHeaderRowIndex();

    protected abstract List<String> getExpectedHeaders(String sheetName);
}