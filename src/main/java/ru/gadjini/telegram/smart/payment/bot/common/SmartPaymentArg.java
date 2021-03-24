package ru.gadjini.telegram.smart.payment.bot.common;

public enum SmartPaymentArg {

    PLAN_ID("pli");

    private String name;

    SmartPaymentArg(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
