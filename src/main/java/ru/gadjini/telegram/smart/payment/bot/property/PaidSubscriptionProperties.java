package ru.gadjini.telegram.smart.payment.bot.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("paid.subscription")
public class PaidSubscriptionProperties {

    private String server;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
