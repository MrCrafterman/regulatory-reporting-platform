package dev.lgbonillar.regreporting.modules.global.validation;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DefaultValidatorRegistry implements ValidatorRegistry {

    private final Map<String, GenericValidator> validatorsByCode = new HashMap<>();
    private final Map<FileType, GenericValidator> validatorsByFileType = new HashMap<>();

    @Override
    public void register(GenericValidator validator) {
        validatorsByCode.put(validator.code(), validator);
        validatorsByFileType.put(validator.supports(), validator);
    }

    @Override
    public Optional<GenericValidator> findByCode(String code) {
        return Optional.ofNullable(validatorsByCode.get(code));
    }

    @Override
    public Optional<GenericValidator> findByFileType(FileType fileType) {
        return Optional.ofNullable(validatorsByFileType.get(fileType));
    }

    @Override
    public List<GenericValidator> getAllValidators() {
        return List.copyOf(validatorsByCode.values());
    }

    @Override
    public List<GenericValidator> getValidatorsForFileType(FileType fileType) {
        return validatorsByFileType.entrySet().stream()
                .filter(e -> e.getKey() == fileType)
                .map(Map.Entry::getValue)
                .toList();
    }
}