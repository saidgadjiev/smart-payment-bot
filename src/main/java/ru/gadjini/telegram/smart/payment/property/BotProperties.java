package ru.gadjini.telegram.smart.payment.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot")
public class BotProperties {

    private String token;

    private String name;

    private String endpoint;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
