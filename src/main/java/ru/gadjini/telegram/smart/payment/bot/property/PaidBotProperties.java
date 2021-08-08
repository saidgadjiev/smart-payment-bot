package ru.gadjini.telegram.smart.payment.bot.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("paid.bot")
public class PaidBotProperties {

    private List<String> servers;

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }
}
