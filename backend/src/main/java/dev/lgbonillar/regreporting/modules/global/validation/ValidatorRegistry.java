package dev.lgbonillar.regreporting.modules.global.validation;

import java.util.List;
import java.util.Optional;

public interface ValidatorRegistry {

    void register(GenericValidator validator);

    Optional<GenericValidator> findByCode(String code);

    Optional<GenericValidator> findByFileType(FileType fileType);

    List<GenericValidator> getAllValidators();

    List<GenericValidator> getValidatorsForFileType(FileType fileType);
}
