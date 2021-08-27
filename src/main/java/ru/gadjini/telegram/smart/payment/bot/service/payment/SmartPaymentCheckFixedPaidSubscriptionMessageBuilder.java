package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.CheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FixedTariffPaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

import java.util.Locale;

@Component
public class SmartPaymentCheckFixedPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    private PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder;

    private FixedTariffPaidSubscriptionService fixedTariffPaidSubscriptionService;

    @Autowired
    public SmartPaymentCheckFixedPaidSubscriptionMessageBuilder(LocalisationService localisationService,
                                                                PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder,
                                                                FixedTariffPaidSubscriptionService fixedTariffPaidSubscriptionService) {
        this.localisationService = localisationService;
        this.paidSubscriptionMessageBuilder = paidSubscriptionMessageBuilder;
        this.fixedTariffPaidSubscriptionService = fixedTariffPaidSubscriptionService;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        if (fixedTariffPaidSubscriptionService.isSubscriptionPeriodActive(paidSubscription)) {
            return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_FIXED_SUBSCRIPTION,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndAt())
                    },
                    locale)
            )
                    .withSubscriptionFor()
                    .withUtcTime()
                    .withPurchaseDate(paidSubscription.getPurchasedAt())
                    .withRenewInstructions()
                    .buildMessage(locale);
        } else {
            return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                    MessagesProperties.MESSAGE_FIXED_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndAt()),
                    },
                    locale)
            )
                    .withSubscriptionFor()
                    .withUtcTime()
                    .withPurchaseDate(paidSubscription.getPurchasedAt())
                    .withRenewInstructions()
                    .buildMessage(locale);
        }
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FIXED;
    }
}
