package dev.lgbonillar.regreporting.modules.global.validation;

public enum FileType {
    XLSX,
    CSV,
    JSON,
    XML;

    public static FileType fromFilename(String filename) {
        if (filename == null) {
            return null;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xlsx")) {
            return XLSX;
        }
        if (lower.endsWith(".csv")) {
            return CSV;
        }
        if (lower.endsWith(".json")) {
            return JSON;
        }
        if (lower.endsWith(".xml")) {
            return XML;
        }
        return null;
    }
}