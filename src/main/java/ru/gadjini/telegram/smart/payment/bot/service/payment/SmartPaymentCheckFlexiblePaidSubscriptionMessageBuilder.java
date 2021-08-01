package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.CheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FixedTariffPaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;

import java.util.Locale;

@Component
public class SmartPaymentCheckFlexiblePaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    @Autowired
    public SmartPaymentCheckFlexiblePaidSubscriptionMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        if (paidSubscription.isActive()) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_FLEXIBLE_SUBSCRIPTION,
                    new Object[]{
                            JodaTimeUtils.toDays(paidSubscription.getSubscriptionInterval()),
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate())
                    },
                    locale);
        } else {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_FLEXIBLE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate())
                    },
                    locale);
        }
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FLEXIBLE;
    }
}
