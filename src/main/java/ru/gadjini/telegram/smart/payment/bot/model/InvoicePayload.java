package ru.gadjini.telegram.smart.payment.bot.model;

public class InvoicePayload {

    private int planId;

    public InvoicePayload() {

    }

    public InvoicePayload(int planId) {
        this.planId = planId;
    }

    public int getPlanId() {
        return planId;
    }
}
