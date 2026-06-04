package dev.lgbonillar.regreporting.modules.global.processor;

import dev.lgbonillar.regreporting.modules.global.validation.FileType;

import java.util.List;
import java.util.Optional;

public interface ProcessorRegistry {

    void register(ReportProcessor processor);

    Optional<ReportProcessor> findByCode(String code);

    Optional<ReportProcessor> findByFileType(FileType fileType);

    List<ReportProcessor> getAllProcessors();

    List<ReportProcessor> getProcessorsForFileType(FileType fileType);
}