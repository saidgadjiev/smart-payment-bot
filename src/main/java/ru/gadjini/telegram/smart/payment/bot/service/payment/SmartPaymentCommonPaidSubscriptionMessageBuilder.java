package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.CommonCheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FixedTariffPaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.Locale;

@Component
public class SmartPaymentCommonPaidSubscriptionMessageBuilder implements CommonCheckPaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    private PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder;

    @Autowired
    public SmartPaymentCommonPaidSubscriptionMessageBuilder(LocalisationService localisationService,
                                                            PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder) {
        this.localisationService = localisationService;
        this.paidSubscriptionMessageBuilder = paidSubscriptionMessageBuilder;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        if (paidSubscription == null) {
            return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_NOT_FOUND, locale)
            )
                    .withRenewInstructions()
                    .buildMessage(locale);
        } else if (paidSubscription.isTrial()) {
            if (paidSubscription.isActive()) {
                return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION,
                        new Object[]{
                                FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate())
                        },
                        locale)
                )
                        .withSubscriptionFor()
                        .withUtcTime()
                        .withRenewInstructions()
                        .buildMessage(locale);
            } else {
                return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_EXPIRED,
                        new Object[]{
                                FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate())
                        },
                        locale)
                )
                        .withSubscriptionFor()
                        .withUtcTime()
                        .withRenewInstructions()
                        .buildMessage(locale);
            }
        } else {
            return null;
        }
    }
}
