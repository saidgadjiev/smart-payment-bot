package ru.gadjini.telegram.smart.payment.bot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gadjini.telegram.smart.bot.commons.filter.*;
import ru.gadjini.telegram.smart.payment.bot.property.PaidBotProperties;

@Configuration
public class SmartPaymentBotConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartPaymentBotConfiguration.class);

    @Autowired
    public SmartPaymentBotConfiguration(PaidBotProperties paidBotProperties) {
        LOGGER.debug("Paid bot server({})", paidBotProperties.getServer());
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
