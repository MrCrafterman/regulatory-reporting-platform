package dev.lgbonillar.regreporting.modules.global.processor;

import dev.lgbonillar.regreporting.modules.global.validation.FileType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DefaultProcessorRegistry implements ProcessorRegistry {

    private final Map<String, ReportProcessor> processorsByCode = new HashMap<>();
    private final Map<FileType, ReportProcessor> processorsByFileType = new HashMap<>();

    @Override
    public void register(ReportProcessor processor) {
        processorsByCode.put(processor.code(), processor);
        processorsByFileType.put(processor.supports(), processor);
    }

    @Override
    public Optional<ReportProcessor> findByCode(String code) {
        return Optional.ofNullable(processorsByCode.get(code));
    }

    @Override
    public Optional<ReportProcessor> findByFileType(FileType fileType) {
        return Optional.ofNullable(processorsByFileType.get(fileType));
    }

    @Override
    public List<ReportProcessor> getAllProcessors() {
        return List.copyOf(processorsByCode.values());
    }

    @Override
    public List<ReportProcessor> getProcessorsForFileType(FileType fileType) {
        return processorsByFileType.entrySet().stream()
                .filter(e -> e.getKey() == fileType)
                .map(Map.Entry::getValue)
                .toList();
    }
}