package dev.lgbonillar.regreporting.modules.demo.processor;

import dev.lgbonillar.regreporting.modules.demo.processor.services.DemoReportGenerator;
import dev.lgbonillar.regreporting.modules.demo.processor.services.DemoNotificationService;
import dev.lgbonillar.regreporting.modules.global.processor.*;
import dev.lgbonillar.regreporting.modules.global.validation.ValidationResult;
import org.apache.poi.ss.usermodel.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DemoReportProcessor extends AbstractReportProcessor {

    public static final String CODE = "DEMO_REPORT_PROCESSOR";

    private final DemoReportGenerator reportGenerator;
    private final DemoNotificationService notificationService;

    public DemoReportProcessor() {
        this.reportGenerator = new DemoReportGenerator();
        this.notificationService = new DemoNotificationService();
    }

    public DemoReportProcessor(DemoReportGenerator reportGenerator, DemoNotificationService notificationService) {
        this.reportGenerator = reportGenerator;
        this.notificationService = notificationService;
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public dev.lgbonillar.regreporting.modules.global.validation.FileType supports() {
        return dev.lgbonillar.regreporting.modules.global.validation.FileType.XLSX;
    }

    @Override
    protected ProcessingResult doProcess(ProcessingJobContext job, ValidationResult validationResult) {
        List<ProcessingFinding> findings = new ArrayList<>();

        byte[] fileContent = job.getFileContent();

        try {
            Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(fileContent));

            Sheet sheet = workbook.getSheet("Hoja1");
            if (sheet != null) {
                findings.addAll(analyzeSalesData(sheet, job));
            }

            byte[] generatedReport = reportGenerator.generate(validationResult, fileContent);
            if (generatedReport != null && generatedReport.length > 0) {
                findings.add(ProcessingFinding.withData(
                        DemoProcessingCodes.DEMO_REPORT_GENERATED.getCode(),
                        DemoProcessingCodes.DEMO_REPORT_GENERATED.getMessage(),
                        ProcessingScope.REPORT,
                        "sales_summary_report.xlsx",
                        generatedReport
                ));
            }

            notificationService.sendProcessingCompletedNotification(job);

        } catch (Exception e) {
            findings.add(ProcessingFinding.error(
                    "DEMO_PROCESSING_ERROR",
                    "Error processing demo report: " + e.getMessage(),
                    ProcessingScope.SYSTEM
            ));
        }

        return ProcessingResult.withFindings(CODE, "Demo report processed successfully", findings);
    }

    private List<ProcessingFinding> analyzeSalesData(Sheet sheet, ProcessingJobContext job) {
        List<ProcessingFinding> findings = new ArrayList<>();

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        Map<String, BigDecimal> salesByZone = new HashMap<>();
        Map<String, Integer> orderCountByZone = new HashMap<>();
        List<BigDecimal> monthlySales = new ArrayList<>();
        LocalDate minDate = null;
        LocalDate maxDate = null;

        int headerRowIndex = 0;
        Row headerRow = sheet.getRow(headerRowIndex);
        if (headerRow == null) {
            return findings;
        }

        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            headers.add(cell != null ? cell.toString() : "");
        }

        int unidadesIndex = headers.indexOf("Unidades");
        int precioUnitarioIndex = headers.indexOf("Precio Unitario");
        int costeUnitarioIndex = headers.indexOf("Coste unitario");
        int importeVentaIndex = headers.indexOf("Importe venta total");
        int importeCosteIndex = headers.indexOf("Importe Coste total");
        int zonaIndex = headers.indexOf("Zona");
        int fechaPedidoIndex = headers.indexOf("Fecha pedido");

        for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            try {
                BigDecimal unidades = getNumericValue(row, unidadesIndex);
                BigDecimal precioUnitario = getNumericValue(row, precioUnitarioIndex);
                BigDecimal costeUnitario = getNumericValue(row, costeUnitarioIndex);
                BigDecimal importeVenta = getNumericValue(row, importeVentaIndex);
                BigDecimal importeCoste = getNumericValue(row, importeCosteIndex);

                totalSales = totalSales.add(importeVenta);
                totalCost = totalCost.add(importeCoste);

                String zona = getCellText(row, zonaIndex);
                if (!zona.isBlank()) {
                    salesByZone.merge(zona, importeVenta, BigDecimal::add);
                    orderCountByZone.merge(zona, 1, Integer::sum);
                }

                LocalDate fechaPedido = getDateValue(row, fechaPedidoIndex);
                if (fechaPedido != null) {
                    if (minDate == null || fechaPedido.isBefore(minDate)) {
                        minDate = fechaPedido;
                    }
                    if (maxDate == null || fechaPedido.isAfter(maxDate)) {
                        maxDate = fechaPedido;
                    }
                }

            } catch (Exception ignored) {
            }
        }

        BigDecimal threshold = new BigDecimal("2000000");
        if (totalSales.compareTo(threshold) > 0) {
            findings.add(ProcessingFinding.warning(
                    DemoProcessingCodes.SALES_TOTAL_EXCEEDS_THRESHOLD.getCode(),
                    "Total sales (" + totalSales + ") exceed threshold of " + threshold,
                    ProcessingScope.SUMMARY
            ));
        }

        if (!salesByZone.isEmpty()) {
            Map.Entry<String, BigDecimal> topZone = Collections.max(salesByZone.entrySet(), Map.Entry.comparingByValue());
            findings.add(ProcessingFinding.info(
                    DemoProcessingCodes.TOP_ZONE_IDENTIFIED.getCode(),
                    "Top performing zone: " + topZone.getKey() + " with sales of " + topZone.getValue(),
                    ProcessingScope.SUMMARY
            ));
        }

        findings.add(ProcessingFinding.info(
                DemoProcessingCodes.PROCESSING_COMPLETED.getCode(),
                "Demo report processed: " + totalSales + " total sales, " + salesByZone.size() + " zones analyzed",
                ProcessingScope.SUMMARY
        ));

        return findings;
    }

    private BigDecimal getNumericValue(Row row, int columnIndex) {
        if (columnIndex < 0 || row == null) {
            throw new IllegalArgumentException("Invalid cell");
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            throw new IllegalArgumentException("Cell is null");
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> new BigDecimal(cell.getStringCellValue().trim().replace(",", ""));
            case FORMULA -> BigDecimal.valueOf(cell.getNumericCellValue());
            default -> throw new IllegalArgumentException("Cell is not numeric");
        };
    }

    private LocalDate getDateValue(Row row, int columnIndex) {
        if (columnIndex < 0 || row == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null || !DateUtil.isCellDateFormatted(cell)) {
            return null;
        }
        return cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private String getCellText(Row row, int columnIndex) {
        if (columnIndex < 0 || row == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return cell.toString().trim();
    }
}