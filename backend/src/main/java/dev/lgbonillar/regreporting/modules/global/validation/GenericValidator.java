package dev.lgbonillar.regreporting.modules.global.validation;

import java.util.List;

public interface GenericValidator {

    String code();

    FileType supports();

    ValidationResult validate(byte[] fileContent);

    List<String> getGlobalRuleCodes();
}