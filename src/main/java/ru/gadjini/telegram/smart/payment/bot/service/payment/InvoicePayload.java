package ru.gadjini.telegram.smart.payment.bot.service.payment;

public class InvoicePayload {

    private int planId;

    public InvoicePayload(int planId) {
        this.planId = planId;
    }

    public int getPlanId() {
        return planId;
    }
}
