package ru.gadjini.telegram.smart.payment.bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gadjini.telegram.smart.bot.commons.filter.BotFilter;
import ru.gadjini.telegram.smart.bot.commons.filter.StartCommandFilter;
import ru.gadjini.telegram.smart.bot.commons.filter.UpdateFilter;
import ru.gadjini.telegram.smart.bot.commons.filter.UpdatesHandlerFilter;

@Configuration
public class SmartPaymentBotConfiguration {

    @Bean
    public BotFilter botFilter(UpdatesHandlerFilter updatesHandlerFilter,
                               UpdateFilter updateFilter, StartCommandFilter startCommandFilter) {
        updateFilter.setNext(startCommandFilter).setNext(updatesHandlerFilter);

        return updateFilter;
    }
}
