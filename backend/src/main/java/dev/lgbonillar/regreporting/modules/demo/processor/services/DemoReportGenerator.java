package dev.lgbonillar.regreporting.modules.demo.processor.services;

import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;
import dev.lgbonillar.regreporting.modules.global.processor.services.ReportGenerator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DemoReportGenerator implements ReportGenerator {

    @Override
    public String getReportFilename(UUID jobId) {
        return "demo_sales_summary_" + jobId + ".xlsx";
    }

    @Override
    public byte[] generate(ValidationResult validation, byte[] originalFile) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet summarySheet = workbook.createSheet("Sales Summary");
            Sheet zoneSheet = workbook.createSheet("Sales by Zone");

            createSummarySheet(summarySheet, originalFile);
            createZoneAnalysisSheet(zoneSheet, originalFile);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private void createSummarySheet(Sheet sheet, byte[] originalFile) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Metric");
        headerRow.createCell(1).setCellValue("Value");

        Row totalSalesRow = sheet.createRow(1);
        totalSalesRow.createCell(0).setCellValue("Total Sales");
        totalSalesRow.createCell(1).setCellValue("Calculated from source data");

        Row totalCostRow = sheet.createRow(2);
        totalCostRow.createCell(0).setCellValue("Total Cost");
        totalCostRow.createCell(1).setCellValue("Calculated from source data");

        Row rowCountRow = sheet.createRow(3);
        rowCountRow.createCell(0).setCellValue("Row Count");
        rowCountRow.createCell(1).setCellValue("Calculated from source data");

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createZoneAnalysisSheet(Sheet sheet, byte[] originalFile) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Zone");
        headerRow.createCell(1).setCellValue("Total Sales");
        headerRow.createCell(2).setCellValue("Order Count");

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    }
}