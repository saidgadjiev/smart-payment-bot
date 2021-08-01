package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.CheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FixedTariffPaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.Locale;

@Component
public class SmartPaymentCheckFixedPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private SubscriptionTimeDeclensionProvider subscriptionTimeDeclensionProvider;

    @Autowired
    public SmartPaymentCheckFixedPaidSubscriptionMessageBuilder(LocalisationService localisationService,
                                                                PaidSubscriptionPlanService paidSubscriptionPlanService,
                                                                SubscriptionTimeDeclensionProvider subscriptionTimeDeclensionProvider) {
        this.localisationService = localisationService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.subscriptionTimeDeclensionProvider = subscriptionTimeDeclensionProvider;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        if (paidSubscription.isActive()) {
            Period planPeriod = paidSubscriptionPlanService.getPlanPeriod(paidSubscription.getPlanId());
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_FLEXIBLE_SUBSCRIPTION,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            subscriptionTimeDeclensionProvider.getService(locale.getLanguage()).localize(planPeriod),
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC))
                    },
                    locale);
        } else {
            Period planPeriod = paidSubscriptionPlanService.getPlanPeriod(paidSubscription.getPlanId());
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_FLEXIBLE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            subscriptionTimeDeclensionProvider.getService(locale.getLanguage()).localize(planPeriod),
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC))
                    },
                    locale);
        }
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FIXED;
    }
}
