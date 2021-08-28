package ru.gadjini.telegram.smart.payment.bot.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.event.RefreshPaidSubscriptionEvent;
import ru.gadjini.telegram.smart.payment.bot.service.PaidBotApi;

@Component
public class CustomRefreshPaidSubscriptionEventHandler {

    private PaidBotApi paidBotApi;

    @Autowired
    public CustomRefreshPaidSubscriptionEventHandler(PaidBotApi paidBotApi) {
        this.paidBotApi = paidBotApi;
    }

    @EventListener
    public void handle(RefreshPaidSubscriptionEvent event) {
        paidBotApi.refreshSub(event.getSourceBotName(), event.getUserId());
    }
}
