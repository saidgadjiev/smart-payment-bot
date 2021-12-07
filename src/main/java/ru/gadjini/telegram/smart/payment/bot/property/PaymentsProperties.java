package ru.gadjini.telegram.smart.payment.bot.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

import java.util.Locale;
import java.util.Map;

@ConfigurationProperties("payments")
public class PaymentsProperties {

    private String testToken;

    private String liveToken;

    private String qiwiUrl;

    private String yoomoneyUrl;

    private String paypalUrl;

    private String buymeacoffeeUrl;

    private String razorpayUrl;

    private String osonWallet;

    private String perfectmoneyWallet;

    private Map<String, Map<Double, String>> robokassa;

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

    public String getPerfectmoneyWallet() {
        return perfectmoneyWallet;
    }

    public void setPerfectmoneyWallet(String perfectmoneyWallet) {
        this.perfectmoneyWallet = perfectmoneyWallet;
    }

    public String getRazorpayUrl() {
        return razorpayUrl;
    }

    public void setRazorpayUrl(String razorpayUrl) {
        this.razorpayUrl = razorpayUrl;
    }

    public String getOsonWallet() {
        return osonWallet;
    }

    public void setOsonWallet(String osonWallet) {
        this.osonWallet = osonWallet;
    }

    public Map<String, Map<Double, String>> getRobokassa() {
        return robokassa;
    }

    public void setRobokassa(Map<String, Map<Double, String>> robokassa) {
        this.robokassa = robokassa;
    }

    public String getRobokassaUrl(Double price, Locale locale) {
        return robokassa.getOrDefault(locale.getLanguage().toLowerCase(), robokassa.get(LocalisationService.EN_LOCALE)).get(price);
    }

    public String getBuymeacoffeeUrl() {
        return buymeacoffeeUrl;
    }

    public void setBuymeacoffeeUrl(String buymeacoffeeUrl) {
        this.buymeacoffeeUrl = buymeacoffeeUrl;
    }
}
