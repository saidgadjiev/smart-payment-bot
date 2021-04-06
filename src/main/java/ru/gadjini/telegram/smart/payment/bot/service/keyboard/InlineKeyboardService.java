package ru.gadjini.telegram.smart.payment.bot.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverter;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverterFactory;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;

import java.util.List;
import java.util.Locale;

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

    public InlineKeyboardMarkup paymentKeyboard(List<PaidSubscriptionPlan> paidSubscriptionPlans, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        TelegramCurrencyConverter telegramCurrencyConverter = telegramCurrencyConverterFactory.createConverter();
        paidSubscriptionPlans.forEach(paidSubscriptionPlan -> {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentButton(paidSubscriptionPlan,
                    telegramCurrencyConverter, locale)));
        });

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup invoiceKeyboard(double usd, double rubles, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.payButton(usd, rubles, locale)));

        return inlineKeyboardMarkup;
    }
}
