package ru.gadjini.telegram.smart.payment.bot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.LocalBotApi;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.TelegramBotApi;
import ru.gadjini.telegram.smart.bot.commons.filter.*;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FatherCheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiMethodExecutor;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramMediaService;
import ru.gadjini.telegram.smart.payment.bot.property.PaidBotProperties;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;
import ru.gadjini.telegram.smart.payment.bot.service.payment.SmartPaymentCheckFixedPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.payment.bot.service.payment.SmartPaymentCheckFlexiblePaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.payment.bot.service.payment.SmartPaymentCommonPaidSubscriptionMessageBuilder;

import java.util.Map;

@Configuration
public class SmartPaymentBotConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartPaymentBotConfiguration.class);

    @Autowired
    public SmartPaymentBotConfiguration(PaidBotProperties paidBotProperties, PaymentsProperties paymentsProperties) {
        LOGGER.debug("Paid bot servers({})", paidBotProperties.getServers());
        LOGGER.debug("Qiwi({})", paymentsProperties.getQiwiUrl());
        LOGGER.debug("YooMoney({})", paymentsProperties.getYoomoneyUrl());
        LOGGER.debug("PayPal({})", paymentsProperties.getPaypalUrl());
        LOGGER.debug("Razorpay({})", paymentsProperties.getRazorpayUrl());
        LOGGER.debug("BuyMeACoffee({})", paymentsProperties.getBuymeacoffeeUrl());
        LOGGER.debug("PerfectMoney({})", paymentsProperties.getPerfectmoneyWallet());
    }

    @Bean
    public BotFilter botFilter(UpdatesHandlerFilter updatesHandlerFilter,
                               TechWorkFilter techWorkFilter,
                               UpdateFilter updateFilter, UserSynchronizedFilter userSynchronizedFilter,
                               StartCommandFilter startCommandFilter) {
        updateFilter.setNext(userSynchronizedFilter).setNext(startCommandFilter).setNext(techWorkFilter).setNext(updatesHandlerFilter);

        return updateFilter;
    }

    @Bean
    @LocalBotApi
    public TelegramMediaService localTelegramBotApiMediaService(BotProperties botProperties,
                                                                @LocalBotApi DefaultBotOptions options, BotApiProperties botApiProperties,
                                                                TelegramBotApiMethodExecutor exceptionHandler) {
        return new TelegramBotApiMediaService(botProperties, options, botApiProperties, exceptionHandler);
    }

    @Bean
    @TelegramBotApi
    public TelegramMediaService telegramBotApiMediaService(BotProperties botProperties,
                                                           @TelegramBotApi DefaultBotOptions options, BotApiProperties botApiProperties,
                                                           TelegramBotApiMethodExecutor exceptionHandler) {
        return new TelegramBotApiMediaService(botProperties, options, botApiProperties, exceptionHandler);
    }

    @Bean
    public FatherCheckPaidSubscriptionMessageBuilder checkPaidSubscriptionMessageBuilder(
            PaidSubscriptionPlanService paidSubscriptionPlanService,
            SmartPaymentCheckFixedPaidSubscriptionMessageBuilder smartPaymentCheckPaidSubscriptionMessageBuilder,
            SmartPaymentCheckFlexiblePaidSubscriptionMessageBuilder smartPaymentCheckFlexiblePaidSubscriptionMessageBuilder,
            SmartPaymentCommonPaidSubscriptionMessageBuilder commonPaidSubscriptionMessageBuilder) {
        return new FatherCheckPaidSubscriptionMessageBuilder(
                commonPaidSubscriptionMessageBuilder,
                paidSubscriptionPlanService,
                Map.of(PaidSubscriptionTariffType.FIXED,
                        smartPaymentCheckPaidSubscriptionMessageBuilder,
                        PaidSubscriptionTariffType.FLEXIBLE,
                        smartPaymentCheckFlexiblePaidSubscriptionMessageBuilder)
        );
    }
}
