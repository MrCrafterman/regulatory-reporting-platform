package dev.lgbonillar.regreporting.modules.global.processor.services;

import dev.lgbonillar.regreporting.modules.global.processor.ProcessingJobContext;

import java.util.Map;

public interface ExternalApiService {

    void sendReport(ProcessingJobContext job, byte[] reportData, Map<String, String> metadata);

    byte[] fetchAdditionalData(ProcessingJobContext job, String dataKey);

    boolean testConnection();
}