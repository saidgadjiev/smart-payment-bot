package ru.gadjini.telegram.smart.payment.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.ButtonFactory;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.InlineKeyboardService;

import java.util.List;
import java.util.Locale;

@Service
public class PaymentMethodService {

    private InlineKeyboardService inlineKeyboardService;

    private SmartInlineKeyboardService smartInlineKeyboardService;

    private LocalisationService localisationService;

    private ButtonFactory buttonFactory;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private PaymentsProperties paymentsProperties;

    @Autowired
    public PaymentMethodService(InlineKeyboardService inlineKeyboardService,
                                SmartInlineKeyboardService smartInlineKeyboardService,
                                LocalisationService localisationService, ButtonFactory buttonFactory,
                                PaidSubscriptionPlanService paidSubscriptionPlanService,
                                PaymentsProperties paymentsProperties) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.smartInlineKeyboardService = smartInlineKeyboardService;
        this.localisationService = localisationService;
        this.buttonFactory = buttonFactory;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.paymentsProperties = paymentsProperties;
    }

    public InlineKeyboardMarkup getPaymentMethodsKeyboard(Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        for (PaymentMethod value : PaymentMethod.values()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(value, locale)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getPaymentKeyboard(PaymentMethod paymentMethod, Locale locale) {
        List<PaidSubscriptionPlan> paidSubscriptionPlans = paidSubscriptionPlanService.getActivePlans();
        switch (paymentMethod) {
            case YOOMONEY:
                return inlineKeyboardService.yooMoneyKeyboard(paymentsProperties.getYoomoneyUrl(), paidSubscriptionPlans, locale);
            case PAYPAL:
                return inlineKeyboardService.payPalKeyboard(paymentsProperties.getPaypalUrl(), paidSubscriptionPlans, locale);
            case QIWI:
                return inlineKeyboardService.qiWiKeyboard(paymentsProperties.getQiwiUrl(), paidSubscriptionPlans, locale);
            case TELEGRAM:
            default:
                return inlineKeyboardService.telegramPaymentKeyboard(paidSubscriptionPlans, locale);
        }
    }

    public String getPaymentAdditionalInformation(PaymentMethod paymentMethod, Locale locale) {
        return localisationService.getMessage(paymentMethod.name().toLowerCase() + ".payment.method.info", locale);
    }

    public String getCallbackAnswer(PaymentMethod paymentMethod, Locale locale) {
        return localisationService.getMessage(paymentMethod.name().toLowerCase() + ".payment.method", locale);
    }

    public enum PaymentMethod {

        TELEGRAM(null),

        PAYPAL("USD"),

        QIWI("RUB"),

        YOOMONEY("RUB");

        private final String currency;

        PaymentMethod(String currency) {
            this.currency = currency;
        }

        public String getCurrency() {
            return currency;
        }
    }
}
