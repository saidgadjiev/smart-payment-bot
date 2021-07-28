package ru.gadjini.telegram.smart.payment.bot.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionTariff;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffService;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;

import java.util.List;
import java.util.Locale;

@Component
public class PaymentMessageBuilder {

    private LocalisationService localisationService;

    private SubscriptionProperties subscriptionProperties;

    private PaidSubscriptionTariffService paidSubscriptionTariffService;

    @Autowired
    public PaymentMessageBuilder(LocalisationService localisationService, SubscriptionProperties subscriptionProperties,
                                 PaidSubscriptionTariffService paidSubscriptionTariffService) {
        this.localisationService = localisationService;
        this.subscriptionProperties = subscriptionProperties;
        this.paidSubscriptionTariffService = paidSubscriptionTariffService;
    }

    public String getBuyWelcome(Locale locale) {
        String buyWelcome = localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_BUY_WELCOME, new Object[]{
                subscriptionProperties.getPaidBotName()
        }, locale);

        StringBuilder tariff = new StringBuilder();
        List<PaidSubscriptionTariff> activeTariffs = paidSubscriptionTariffService.getActiveTariffs();

        for (PaidSubscriptionTariff activeTariff : activeTariffs) {
            if (tariff.length() > 0) {
                tariff.append("\n\n");
            }
            tariff.append(
                    localisationService.getMessage("message." + activeTariff.getTariffType().name().toLowerCase() +
                            ".payment.plan.description", locale)
            );
        }

        return buyWelcome + "\n\n" + tariff.toString();
    }
}
