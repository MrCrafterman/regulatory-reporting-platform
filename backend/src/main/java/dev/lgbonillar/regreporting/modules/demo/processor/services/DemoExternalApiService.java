package dev.lgbonillar.regreporting.modules.demo.processor.services;

import dev.lgbonillar.regreporting.modules.global.processor.ProcessingJobContext;
import dev.lgbonillar.regreporting.modules.global.processor.services.ExternalApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DemoExternalApiService implements ExternalApiService {

    private static final Logger log = LoggerFactory.getLogger(DemoExternalApiService.class);

    @Override
    public void sendReport(ProcessingJobContext job, byte[] reportData, Map<String, String> metadata) {
        log.info("Sending report to external API for job {} - Data size: {} bytes",
                job.getJobId(), reportData != null ? reportData.length : 0);
    }

    @Override
    public byte[] fetchAdditionalData(ProcessingJobContext job, String dataKey) {
        log.info("Fetching additional data for job {} - Key: {}", job.getJobId(), dataKey);
        return new byte[0];
    }

    @Override
    public boolean testConnection() {
        log.info("Testing connection to external API");
        return true;
    }
}