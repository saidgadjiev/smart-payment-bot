package ru.gadjini.telegram.smart.payment.bot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gadjini.telegram.smart.bot.commons.filter.*;
import ru.gadjini.telegram.smart.payment.bot.property.PaidBotProperties;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;

@Configuration
public class SmartPaymentBotConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartPaymentBotConfiguration.class);

    @Autowired
    public SmartPaymentBotConfiguration(PaidBotProperties paidBotProperties, PaymentsProperties paymentsProperties) {
        LOGGER.debug("Paid bot server({})", paidBotProperties.getServer());
        LOGGER.debug("Qiwi({})", paymentsProperties.getQiwiUrl());
        LOGGER.debug("YooMoney({})", paymentsProperties.getYoomoneyUrl());
        LOGGER.debug("BuyMeACoffee({})", paymentsProperties.getBuymeacoffeeUrl());
        LOGGER.debug("Crypto({})", paymentsProperties.getUsdtWallet());
    }

    @Bean
    public BotFilter botFilter(UpdatesHandlerFilter updatesHandlerFilter,
                               TechWorkFilter techWorkFilter,
                               UpdateFilter updateFilter, UserSynchronizedFilter userSynchronizedFilter,
                               StartCommandFilter startCommandFilter) {
        updateFilter.setNext(userSynchronizedFilter).setNext(startCommandFilter).setNext(techWorkFilter).setNext(updatesHandlerFilter);

        return updateFilter;
    }
}
