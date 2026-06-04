package dev.lgbonillar.regreporting.modules.demo.validation;

import dev.lgbonillar.regreporting.modules.global.validation.AbstractExcelValidator;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;
import dev.lgbonillar.regreporting.modules.demo.validation.rules.DemoBusinessRules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DemoExcelValidator extends AbstractExcelValidator {

    public static final String CODE = "DEMO_EXCEL_VALIDATOR";
    public static final String SHEET_NAME = "Hoja1";
    public static final int HEADER_ROW_INDEX = 0;

    public static final List<String> EXPECTED_HEADERS = List.of(
            "ID Cliente",
            "Zona",
            "País",
            "Tipo de producto",
            "Canal de venta",
            "Prioridad",
            "Fecha pedido",
            "ID Pedido",
            "Fecha envío",
            "Unidades",
            "Precio Unitario",
            "Coste unitario",
            "Importe venta total",
            "Importe Coste total"
    );

    private final DemoBusinessRules businessRules;

    public DemoExcelValidator() {
        super();
        this.businessRules = new DemoBusinessRules();
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public List<String> getGlobalRuleCodes() {
        return List.of(
                "GLOBAL__WORKBOOK_WITHOUT_SHEETS",
                "GLOBAL__MISSING_REQUIRED_SHEET",
                "GLOBAL__EMPTY_SHEET",
                "GLOBAL__SHEET_WITHOUT_HEADER",
                "GLOBAL__SHEET_WITHOUT_DATA_ROWS",
                "GLOBAL__DUPLICATED_HEADER",
                "GLOBAL__MISSING_REQUIRED_COLUMN",
                "GLOBAL__INVALID_COLUMN_ORDER",
                "GLOBAL__REQUIRED_VALUE_MISSING",
                "GLOBAL__INVALID_DATE_VALUE"
        );
    }

    @Override
    protected List<String> getRequiredSheets() {
        return List.of(SHEET_NAME);
    }

    @Override
    protected int getHeaderRowIndex() {
        return HEADER_ROW_INDEX;
    }

    @Override
    protected List<String> getExpectedHeaders(String sheetName) {
        return EXPECTED_HEADERS;
    }

    @Override
    protected void validateDataRows(
            org.apache.poi.ss.usermodel.Sheet sheet,
            int headerRowIndex,
            List<String> headers,
            List<dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding> findings
    ) {
        int firstDataRowIndex = headerRowIndex + 1;
        int lastRowIndex = sheet.getLastRowNum();

        if (lastRowIndex < firstDataRowIndex) {
            return;
        }

        Set<String> seenOrderIds = new HashSet<>();

        for (int rowIndex = firstDataRowIndex; rowIndex <= lastRowIndex; rowIndex++) {
            var row = sheet.getRow(rowIndex);

            if (row == null || isRowBlank(row)) {
                continue;
            }

            super.validateDataRows(sheet, headerRowIndex, headers, findings);

            validateNumericValues(sheet.getSheetName(), row, findings);
            validateBusinessCalculations(sheet.getSheetName(), row, findings);
            validateDateRange(sheet.getSheetName(), row, findings);
            validateDuplicatedOrderId(sheet.getSheetName(), row, seenOrderIds, findings);
        }
    }

    private void validateNumericValues(
            String sheetName,
            org.apache.poi.ss.usermodel.Row row,
            List<dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding> findings
    ) {
        validateNumericValue(sheetName, row, "Unidades", findings);
        validateNumericValue(sheetName, row, "Precio Unitario", findings);
        validateNumericValue(sheetName, row, "Coste unitario", findings);
        validateNumericValue(sheetName, row, "Importe venta total", findings);
        validateNumericValue(sheetName, row, "Importe Coste total", findings);
    }

    private void validateNumericValue(
            String sheetName,
            org.apache.poi.ss.usermodel.Row row,
            String header,
            List<dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding> findings
    ) {
        int columnIndex = EXPECTED_HEADERS.indexOf(header);
        try {
            getNumericValue(row, columnIndex);
        } catch (IllegalArgumentException exception) {
            findings.add(dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding.error(
                    DemoRules.DEMO_INVALID_NUMERIC_VALUE.getCode(),
                    DemoRules.DEMO_INVALID_NUMERIC_VALUE.getMessage(),
                    dev.lgbonillar.regreporting.modules.global.validation.ValidationScope.ROW_DATA,
                    sheetName,
                    row.getRowNum() + 1,
                    String.valueOf(columnIndex + 1),
                    header,
                    getCellText(row, columnIndex),
                    "Numeric value",
                    getCellText(row, columnIndex)
            ));
        }
    }

    private void validateBusinessCalculations(
            String sheetName,
            org.apache.poi.ss.usermodel.Row row,
            List<dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding> findings
    ) {
        businessRules.validateMultiplication(
                sheetName,
                row,
                "Unidades",
                EXPECTED_HEADERS.indexOf("Unidades"),
                "Precio Unitario",
                EXPECTED_HEADERS.indexOf("Precio Unitario"),
                "Importe venta total",
                EXPECTED_HEADERS.indexOf("Importe venta total"),
                findings,
                DemoRules.DEMO_AMOUNT_CALCULATION_MISMATCH.getCode(),
                DemoRules.DEMO_AMOUNT_CALCULATION_MISMATCH.getMessage()
        );

        businessRules.validateMultiplication(
                sheetName,
                row,
                "Unidades",
                EXPECTED_HEADERS.indexOf("Unidades"),
                "Coste unitario",
                EXPECTED_HEADERS.indexOf("Coste unitario"),
                "Importe Coste total",
                EXPECTED_HEADERS.indexOf("Importe Coste total"),
                findings,
                DemoRules.DEMO_AMOUNT_CALCULATION_MISMATCH.getCode(),
                DemoRules.DEMO_AMOUNT_CALCULATION_MISMATCH.getMessage()
        );
    }

    private void validateDateRange(
            String sheetName,
            org.apache.poi.ss.usermodel.Row row,
            List<dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding> findings
    ) {
        businessRules.validateDateGreaterThanOrEqual(
                sheetName,
                row,
                "Fecha pedido",
                EXPECTED_HEADERS.indexOf("Fecha pedido"),
                "Fecha envío",
                EXPECTED_HEADERS.indexOf("Fecha envío"),
                findings,
                DemoRules.DEMO_INVALID_SHIPPING_DATE_RANGE.getCode(),
                DemoRules.DEMO_INVALID_SHIPPING_DATE_RANGE.getMessage()
        );
    }

    private void validateDuplicatedOrderId(
            String sheetName,
            org.apache.poi.ss.usermodel.Row row,
            Set<String> seenOrderIds,
            List<dev.lgbonillar.regreporting.modules.global.validation.ValidationFinding> findings
    ) {
        businessRules.validateUniqueValue(
                sheetName,
                row,
                "ID Pedido",
                EXPECTED_HEADERS.indexOf("ID Pedido"),
                seenOrderIds,
                findings,
                DemoRules.DEMO_DUPLICATED_ORDER_ID.getCode(),
                DemoRules.DEMO_DUPLICATED_ORDER_ID.getMessage()
        );
    }

    private org.apache.poi.ss.usermodel.Row getRow(org.apache.poi.ss.usermodel.Sheet sheet, int rowIndex) {
        return sheet.getRow(rowIndex);
    }

    private boolean isRowBlank(org.apache.poi.ss.usermodel.Row row) {
        if (row == null || row.getFirstCellNum() < 0 || row.getLastCellNum() < 0) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (!getCellText(row, i).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellText(org.apache.poi.ss.usermodel.Row row, int columnIndex) {
        if (columnIndex < 0 || row == null) {
            return "";
        }
        var cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return cell.toString().trim();
    }

    private java.math.BigDecimal getNumericValue(org.apache.poi.ss.usermodel.Row row, int columnIndex) {
        var cell = row.getCell(columnIndex);
        if (cell == null) {
            throw new IllegalArgumentException("Cell is blank");
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> java.math.BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> new java.math.BigDecimal(cell.getStringCellValue().trim().replace(",", ""));
            case FORMULA -> java.math.BigDecimal.valueOf(cell.getNumericCellValue());
            default -> throw new IllegalArgumentException("Cell is not numeric");
        };
    }
}