package ru.gadjini.telegram.smart.payment.bot.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("payments")
public class PaymentsProperties {

    private String testToken;

    private String liveToken;

    private String qiwiUrl;

    private String yoomoneyUrl;

    private String paypalUrl;

    private String cryptocurrencyUrl;

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

    public String getQiwiUrl() {
        return qiwiUrl;
    }

    public void setQiwiUrl(String qiwiUrl) {
        this.qiwiUrl = qiwiUrl;
    }

    public String getYoomoneyUrl() {
        return yoomoneyUrl;
    }

    public void setYoomoneyUrl(String yoomoneyUrl) {
        this.yoomoneyUrl = yoomoneyUrl;
    }

    public String getPaypalUrl() {
        return paypalUrl;
    }

    public void setPaypalUrl(String paypalUrl) {
        this.paypalUrl = paypalUrl;
    }

    public String getCryptocurrencyUrl() {
        return cryptocurrencyUrl;
    }

    public void setCryptocurrencyUrl(String cryptocurrencyUrl) {
        this.cryptocurrencyUrl = cryptocurrencyUrl;
    }
}
