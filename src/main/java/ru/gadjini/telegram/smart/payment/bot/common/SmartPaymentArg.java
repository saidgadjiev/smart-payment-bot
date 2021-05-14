package ru.gadjini.telegram.smart.payment.bot.common;

public enum SmartPaymentArg {

    PLAN_ID("pli"),
    PAYMENT_METHOD("pm"),
    GO_BACK("gbc"),
    PAYMENT_DETAILS("det");

    private String key;

    SmartPaymentArg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
