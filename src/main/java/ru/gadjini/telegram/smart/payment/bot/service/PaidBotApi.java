package ru.gadjini.telegram.smart.payment.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.controller.api.PaidSubscriptionApi;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.payment.bot.property.PaidBotProperties;

import java.util.Map;

@Component
public class PaidBotApi {

    private PaidBotProperties paidSubscriptionProperties;

    private PaidSubscriptionApi paidSubscriptionApi;

    private BotProperties botProperties;

    @Autowired
    public PaidBotApi(PaidSubscriptionApi paidSubscriptionApi, BotProperties botProperties,
                      PaidBotProperties paidSubscriptionProperties) {
        this.botProperties = botProperties;
        this.paidSubscriptionApi = paidSubscriptionApi;
        this.paidSubscriptionProperties = paidSubscriptionProperties;
    }

    public void refreshSub(String sourceBotName, long userId) {
        for (Map.Entry<String, String> server : paidSubscriptionProperties.getServers().entrySet()) {
            if (sourceBotName.equals(server.getKey())) {
                continue;
            }
            paidSubscriptionApi.refreshPaidSubscription(server.getValue(), userId, botProperties.getName());
        }
    }

    public void refreshSub(long userId) {
        for (String server : paidSubscriptionProperties.getServers().values()) {
            paidSubscriptionApi.refreshPaidSubscription(server, userId, botProperties.getName());
        }
    }
}
