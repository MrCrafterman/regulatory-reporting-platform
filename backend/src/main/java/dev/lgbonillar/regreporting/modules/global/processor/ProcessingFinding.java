package dev.lgbonillar.regreporting.modules.global.processor;

public record ProcessingFinding(
        ProcessingSeverity severity,
        ProcessingScope scope,
        String code,
        String message,
        String category,
        byte[] data
) {
    public static ProcessingFinding info(String code, String message, ProcessingScope scope) {
        return new ProcessingFinding(ProcessingSeverity.INFO, scope, code, message, null, null);
    }

    public static ProcessingFinding info(String code, String message, ProcessingScope scope, String category) {
        return new ProcessingFinding(ProcessingSeverity.INFO, scope, code, message, category, null);
    }

    public static ProcessingFinding warning(String code, String message, ProcessingScope scope) {
        return new ProcessingFinding(ProcessingSeverity.WARNING, scope, code, message, null, null);
    }

    public static ProcessingFinding error(String code, String message, ProcessingScope scope) {
        return new ProcessingFinding(ProcessingSeverity.ERROR, scope, code, message, null, null);
    }

    public static ProcessingFinding withData(String code, String message, ProcessingScope scope, String category, byte[] data) {
        return new ProcessingFinding(ProcessingSeverity.INFO, scope, code, message, category, data);
    }
}