package ru.gadjini.telegram.smart.payment.bot.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverter;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverterFactory;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.payment.bot.service.PaymentMethodService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class InlineKeyboardService {

    private SmartInlineKeyboardService smartInlineKeyboardService;

    private ButtonFactory buttonFactory;

    private TelegramCurrencyConverterFactory telegramCurrencyConverterFactory;

    @Autowired
    public InlineKeyboardService(SmartInlineKeyboardService smartInlineKeyboardService,
                                 ButtonFactory buttonFactory, TelegramCurrencyConverterFactory telegramCurrencyConverterFactory) {
        this.smartInlineKeyboardService = smartInlineKeyboardService;
        this.buttonFactory = buttonFactory;
        this.telegramCurrencyConverterFactory = telegramCurrencyConverterFactory;
    }

    public InlineKeyboardMarkup getCompositePaymentMethodsKeyboard(PaidSubscriptionTariffType tariffType,
                                                                   Set<PaymentMethodService.PaymentMethod> paymentMethods,
                                                                   Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        for (PaymentMethodService.PaymentMethod method : paymentMethods) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(
                    buttonFactory.paymentMethod(tariffType, method, locale)));
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackButton(tariffType, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getPaymentMethodsKeyboard(PaidSubscriptionTariffType tariffType, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.BANK_CARD, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.APPLE_PAY, locale),
                buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.GOOGLE_PAY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.YANDEX_PAY, locale),
                buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.SAMSUNG_PAY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.PAYPAL, locale),
                buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.RAZORPAY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.QIWI, locale),
                buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.YOOMONEY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.WEBMONEY, locale),
                buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.PERFECTMONEY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.CRYPTOCURRENCY, locale),
                buttonFactory.paymentMethod(tariffType, PaymentMethodService.PaymentMethod.OSON, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goToTariffs(locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup telegramPaymentKeyboard(PaidSubscriptionTariffType tariffType, List<PaidSubscriptionPlan> paidSubscriptionPlans, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        TelegramCurrencyConverter telegramCurrencyConverter = telegramCurrencyConverterFactory.createConverter();
        paidSubscriptionPlans.forEach(paidSubscriptionPlan -> {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.telegramPaymentButton(paidSubscriptionPlan,
                    telegramCurrencyConverter, locale)));
        });
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackButton(tariffType, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup paymentDetailsKeyboard(PaidSubscriptionTariffType tariff, PaymentMethodService.PaymentMethod method,
                                                       List<PaidSubscriptionPlan> paidSubscriptionPlans, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        TelegramCurrencyConverter telegramCurrencyConverter = telegramCurrencyConverterFactory.createConverter();
        paidSubscriptionPlans.forEach(paidSubscriptionPlan -> {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentDetailsButton(
                    method, method.getCurrency(),
                    telegramCurrencyConverter, paidSubscriptionPlan, locale)));
        });
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackButton(tariff, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup paymentUrlPaymentMethodKeyboard(PaidSubscriptionTariffType tariffType, String paymentUrl, String currency,
                                                                List<PaidSubscriptionPlan> paidSubscriptionPlans,
                                                                Locale locale) {
        return paymentUrlPaymentMethodKeyboard(tariffType, paymentUrl, currency, paidSubscriptionPlans, locale, null);
    }

    public InlineKeyboardMarkup paymentUrlPaymentMethodKeyboard(PaidSubscriptionTariffType tariffType, Map<Double, String> paymentUrls, String currency,
                                                                List<PaidSubscriptionPlan> paidSubscriptionPlans,
                                                                Locale locale, Consumer<TelegramCurrencyConverter> converterCustomizer) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        TelegramCurrencyConverter telegramCurrencyConverter = telegramCurrencyConverterFactory.createConverter();
        if (converterCustomizer != null) {
            converterCustomizer.accept(telegramCurrencyConverter);
        }
        paidSubscriptionPlans.forEach(paidSubscriptionPlan -> {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentUrlPaymentButton(paymentUrls.get(paidSubscriptionPlan.getPrice()), paidSubscriptionPlan,
                    currency, telegramCurrencyConverter, locale)));
        });
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackButton(tariffType, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup paymentUrlPaymentMethodKeyboard(PaidSubscriptionTariffType tariffType, String paymentUrl, String currency,
                                                                List<PaidSubscriptionPlan> paidSubscriptionPlans,
                                                                Locale locale, Consumer<TelegramCurrencyConverter> converterCustomizer) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        TelegramCurrencyConverter telegramCurrencyConverter = telegramCurrencyConverterFactory.createConverter();
        if (converterCustomizer != null) {
            converterCustomizer.accept(telegramCurrencyConverter);
        }
        paidSubscriptionPlans.forEach(paidSubscriptionPlan -> {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentUrlPaymentButton(paymentUrl, paidSubscriptionPlan,
                    currency, telegramCurrencyConverter, locale)));
        });
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackButton(tariffType, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup invoiceKeyboard(double usd, double targetPrice, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.payButton(usd, targetPrice, locale)));

        return inlineKeyboardMarkup;
    }
}
