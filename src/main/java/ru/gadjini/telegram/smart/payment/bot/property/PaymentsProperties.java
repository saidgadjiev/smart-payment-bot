package ru.gadjini.telegram.smart.payment.bot.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("payments")
public class PaymentsProperties {

    private String testToken;

    private String liveToken;

    public String getTestToken() {
        return testToken;
    }

    public void setTestToken(String testToken) {
        this.testToken = testToken;
    }

    public String getLiveToken() {
        return liveToken;
    }

    public void setLiveToken(String liveToken) {
        this.liveToken = liveToken;
    }
}
