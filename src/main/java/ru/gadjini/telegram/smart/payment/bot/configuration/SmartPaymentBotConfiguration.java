package ru.gadjini.telegram.smart.payment.bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gadjini.telegram.smart.bot.commons.filter.*;

@Configuration
public class SmartPaymentBotConfiguration {

    @Bean
    public BotFilter botFilter(UpdatesHandlerFilter updatesHandlerFilter,
                               TechWorkFilter techWorkFilter,
                               UpdateFilter updateFilter, UserSynchronizedFilter userSynchronizedFilter,
                               StartCommandFilter startCommandFilter) {
        updateFilter.setNext(userSynchronizedFilter).setNext(startCommandFilter).setNext(techWorkFilter).setNext(updatesHandlerFilter);

        return updateFilter;
    }
}
