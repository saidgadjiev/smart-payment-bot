package ru.gadjini.telegram.smart.payment.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gadjini.telegram.smart.bot.commons.property.AuthProperties;
import ru.gadjini.telegram.smart.payment.bot.property.PaidBotProperties;

@Service
public class PaidBotApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaidBotApi.class);

    private RestTemplate restTemplate;

    private PaidBotProperties paidSubscriptionProperties;

    private AuthProperties authProperties;

    @Autowired
    public PaidBotApi(RestTemplate restTemplate, PaidBotProperties paidSubscriptionProperties,
                      AuthProperties authProperties) {
        this.restTemplate = restTemplate;
        this.paidSubscriptionProperties = paidSubscriptionProperties;
        this.authProperties = authProperties;
    }

    public void refreshSub(long userId) {
        for (String server : paidSubscriptionProperties.getServers()) {
            refreshSub(server, userId);
        }
    }

    public void refreshSub(String server, long userId) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(buildUrl(server, userId), new HttpEntity<>(authHeaders()), Void.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("Refresh paid subscription failed({}, {}, {})", server, response.getStatusCodeValue(), userId);
            }
        } catch (Throwable e) {
            LOGGER.error("Refresh paid subscription failed(" + server + ", " + userId + ")\n" + e.getMessage(), e);
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, authProperties.getToken());

        return httpHeaders;
    }

    private String buildUrl(String server, long userId) {
        return UriComponentsBuilder.fromHttpUrl(server)
                .path("/subscription/paid/{userId}/refresh")
                .buildAndExpand(userId).toUriString();
    }
}
