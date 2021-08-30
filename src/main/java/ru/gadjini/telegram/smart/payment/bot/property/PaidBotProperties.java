package ru.gadjini.telegram.smart.payment.bot.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties
public class PaidBotProperties {

    @Value("#{${paid.bot.servers}}")
    private Map<String, String> servers;

    public Map<String, String> getServers() {
        return servers;
    }
}
