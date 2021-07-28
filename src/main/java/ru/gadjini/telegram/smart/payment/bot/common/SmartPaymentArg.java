package ru.gadjini.telegram.smart.payment.bot.common;

public enum SmartPaymentArg {

    PLAN_ID("pli"),
    PAYMENT_METHOD("pm"),
    PAYMENT_TARIFF("ptr"),
    GO_BACK("gbc"),
    GO_TO_TARIFFS("gtt"),
    PAYMENT_DETAILS("det");

    private String key;

    SmartPaymentArg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
