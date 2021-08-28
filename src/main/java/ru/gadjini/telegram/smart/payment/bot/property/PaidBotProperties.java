package ru.gadjini.telegram.smart.payment.bot.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("paid.bot")
public class PaidBotProperties {

    private Map<String, String> servers;

    public Map<String, String> getServers() {
        return servers;
    }

    public void setServers(Map<String, String> servers) {
        for (String key : servers.keySet()) {
            servers.put(key.replace(".", "_"), servers.get(key));
            servers.remove(key);
        }

        this.servers = servers;
    }
}
