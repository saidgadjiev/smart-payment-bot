package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.CheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;

import java.time.ZoneOffset;
import java.util.Locale;

@Component
public class SmartPaymentCheckPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private SubscriptionTimeDeclensionProvider subscriptionTimeDeclensionProvider;

    @Autowired
    public SmartPaymentCheckPaidSubscriptionMessageBuilder(LocalisationService localisationService,
                                                           PaidSubscriptionPlanService paidSubscriptionPlanService,
                                                           SubscriptionTimeDeclensionProvider subscriptionTimeDeclensionProvider) {
        this.localisationService = localisationService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.subscriptionTimeDeclensionProvider = subscriptionTimeDeclensionProvider;
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
                                PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate())
                        },
                        locale);
            } else {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_EXPIRED,
                        new Object[]{
                                PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate())
                        },
                        locale);
            }
        } else if (paidSubscription.isActive()) {
            Period planPeriod = paidSubscriptionPlanService.getPlanPeriod(paidSubscription.getPlanId());
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_SUBSCRIPTION,
                    new Object[]{
                            PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            subscriptionTimeDeclensionProvider.getService(locale.getLanguage()).months(planPeriod.getMonths()),
                            PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate())
                    },
                    locale);
        } else {
            Period planPeriod = paidSubscriptionPlanService.getPlanPeriod(paidSubscription.getPlanId());
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            subscriptionTimeDeclensionProvider.getService(locale.getLanguage()).months(planPeriod.getMonths()),
                            PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate())
                    },
                    locale);
        }
    }
}
