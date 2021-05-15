package ru.gadjini.telegram.smart.payment.bot.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("payments")
public class PaymentsProperties {

    private String testToken;

    private String liveToken;

    private String qiwiUrl;

    private String yoomoneyUrl;

    private String buymeacoffeeUrl;

    private String usdtWallet;

    private String perfectmoneyWallet;

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

    public String getBuymeacoffeeUrl() {
        return buymeacoffeeUrl;
    }

    public void setBuymeacoffeeUrl(String buymeacoffeeUrl) {
        this.buymeacoffeeUrl = buymeacoffeeUrl;
    }

    public String getUsdtWallet() {
        return usdtWallet;
    }

    public void setUsdtWallet(String usdtWallet) {
        this.usdtWallet = usdtWallet;
    }

    public String getPerfectmoneyWallet() {
        return perfectmoneyWallet;
    }

    public void setPerfectmoneyWallet(String perfectmoneyWallet) {
        this.perfectmoneyWallet = perfectmoneyWallet;
    }
}
