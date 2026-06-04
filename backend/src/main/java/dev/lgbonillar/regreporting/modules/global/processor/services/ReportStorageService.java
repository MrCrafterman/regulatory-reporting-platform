package dev.lgbonillar.regreporting.modules.global.processor.services;

import dev.lgbonillar.regreporting.modules.global.processor.ProcessingJobContext;

import java.util.UUID;

public interface ReportStorageService {

    void storeReport(UUID jobId, String filename, byte[] content);

    byte[] getReport(UUID jobId, String filename);

    java.util.List<String> listReports(UUID jobId);
}