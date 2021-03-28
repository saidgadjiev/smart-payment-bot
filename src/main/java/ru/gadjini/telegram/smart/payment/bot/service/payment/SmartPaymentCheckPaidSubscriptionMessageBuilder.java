package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.CheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;

import java.util.Locale;

@Component
public class SmartPaymentCheckPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    @Autowired
    public SmartPaymentCheckPaidSubscriptionMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        if (paidSubscription == null) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_NOT_FOUND,
                    locale
            );
        } else if (paidSubscription.isTrial()) {
            if (paidSubscription.isActive()) {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION,
                        new Object[]{
                                PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate())
                        },
                        locale);
            } else {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_EXPIRED,
                        new Object[]{
                                PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate())
                        },
                        locale);
            }
        } else if (paidSubscription.isActive()) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_SUBSCRIPTION,
                    new Object[]{
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate())
                    },
                    locale);
        } else {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate())
                    },
                    locale);
        }
    }
}
