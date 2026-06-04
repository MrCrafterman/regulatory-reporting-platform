package dev.lgbonillar.regreporting.modules.demo.validation;

public enum DemoRules {

    DEMO_INVALID_NUMERIC_VALUE("DEMO__INVALID_NUMERIC_VALUE", "The value must be numeric", dev.lgbonillar.regreporting.modules.global.validation.ValidationScope.ROW_DATA),
    DEMO_AMOUNT_CALCULATION_MISMATCH("DEMO__AMOUNT_CALCULATION_MISMATCH", "The calculated amount does not match the reported amount", dev.lgbonillar.regreporting.modules.global.validation.ValidationScope.BUSINESS_RULE),
    DEMO_INVALID_SHIPPING_DATE_RANGE("DEMO__INVALID_SHIPPING_DATE_RANGE", "The shipping date cannot be before the order date", dev.lgbonillar.regreporting.modules.global.validation.ValidationScope.BUSINESS_RULE),
    DEMO_DUPLICATED_ORDER_ID("DEMO__DUPLICATED_ORDER_ID", "The order id must be unique within the sales report", dev.lgbonillar.regreporting.modules.global.validation.ValidationScope.BUSINESS_RULE);

    private final String code;
    private final String message;
    private final dev.lgbonillar.regreporting.modules.global.validation.ValidationScope scope;

    DemoRules(String code, String message, dev.lgbonillar.regreporting.modules.global.validation.ValidationScope scope) {
        this.code = code;
        this.message = message;
        this.scope = scope;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public dev.lgbonillar.regreporting.modules.global.validation.ValidationScope getScope() {
        return scope;
    }
}