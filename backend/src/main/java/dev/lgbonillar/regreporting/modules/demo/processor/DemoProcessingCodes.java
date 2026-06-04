package dev.lgbonillar.regreporting.modules.demo.processor;

import dev.lgbonillar.regreporting.modules.global.processor.ProcessingScope;
import dev.lgbonillar.regreporting.modules.global.processor.ProcessingSeverity;

public enum DemoProcessingCodes {

    SALES_TOTAL_EXCEEDS_THRESHOLD("SALES_TOTAL_EXCEEDS_THRESHOLD", "Total sales exceed threshold", ProcessingScope.SUMMARY),
    SALES_TREND_INCREASING("SALES_TREND_INCREASING", "Sales show increasing trend", ProcessingScope.CHART),
    SALES_TREND_DECREASING("SALES_TREND_DECREASING", "Sales show decreasing trend", ProcessingScope.CHART),
    TOP_ZONE_IDENTIFIED("TOP_ZONE_IDENTIFIED", "Top performing zone identified", ProcessingScope.SUMMARY),
    ORPHAN_RECORDS_DETECTED("ORPHAN_RECORDS_DETECTED", "Records without proper associations detected", ProcessingScope.REPORT),
    DATA_QUALITY_WARNING("DATA_QUALITY_WARNING", "Data quality issues detected", ProcessingScope.REPORT),
    PROCESSING_COMPLETED("PROCESSING_COMPLETED", "Demo report processing completed successfully", ProcessingScope.SUMMARY),
    DEMO_REPORT_GENERATED("DEMO_REPORT_GENERATED", "Demo report has been generated", ProcessingScope.REPORT);

    private final String code;
    private final String message;
    private final ProcessingScope scope;

    DemoProcessingCodes(String code, String message, ProcessingScope scope) {
        this.code = code;
        this.message = message;
        this.scope = scope;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ProcessingScope getScope() {
        return scope;
    }
}