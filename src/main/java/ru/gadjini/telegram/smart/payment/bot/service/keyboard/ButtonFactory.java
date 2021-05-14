package ru.gadjini.telegram.smart.payment.bot.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.telegram.smart.bot.commons.command.impl.CallbackDelegate;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverter;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentArg;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.service.PaymentMethodService;

import java.util.Locale;

@Service
public class ButtonFactory {

    private LocalisationService localisationService;

    private SubscriptionTimeDeclensionProvider timeDeclensionProvider;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public ButtonFactory(LocalisationService localisationService,
                         SubscriptionTimeDeclensionProvider timeDeclensionProvider,
                         SubscriptionProperties subscriptionProperties) {
        this.localisationService = localisationService;
        this.timeDeclensionProvider = timeDeclensionProvider;
        this.subscriptionProperties = subscriptionProperties;
    }

    public InlineKeyboardButton telegramPaymentButton(PaidSubscriptionPlan paidSubscriptionPlan,
                                                      TelegramCurrencyConverter telegramCurrencyConverter, Locale locale) {
        double usd = paidSubscriptionPlan.getPrice();
        double targetPrice = telegramCurrencyConverter.convertTo(paidSubscriptionPlan.getPrice(), subscriptionProperties.getPaymentCurrency());

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.PAY_COMMAND_DESCRIPTION,
                        new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                .months(paidSubscriptionPlan.getPeriod().getMonths()),
                                NumberUtils.toString(targetPrice, 2), subscriptionProperties.getPaymentCurrency(),
                                NumberUtils.toString(usd, 2)},
                        locale)
        );

        inlineKeyboardButton.setCallbackData(SmartPaymentCommandNames.BUY + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(SmartPaymentArg.PLAN_ID.getKey(), paidSubscriptionPlan.getId())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton payNativeCurrencyButton(String paymentUrl, String currency, PaidSubscriptionPlan paidSubscriptionPlan, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.PAY_TARGET_COMMAND_DESCRIPTION,
                        new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                .months(paidSubscriptionPlan.getPeriod().getMonths()),
                                NumberUtils.toString(paidSubscriptionPlan.getPrice(), 2), currency
                        },
                        locale)
        );

        inlineKeyboardButton.setUrl(paymentUrl);

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton paymentDetailsButton(PaymentMethodService.PaymentMethod paymentMethod,
                                                     String currency, PaidSubscriptionPlan paidSubscriptionPlan, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.PAY_TARGET_COMMAND_DESCRIPTION,
                        new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                .months(paidSubscriptionPlan.getPeriod().getMonths()),
                                NumberUtils.toString(paidSubscriptionPlan.getPrice(), 2) +
                                currency
                        },
                        locale)
        );

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartPaymentCommandNames.BUY)
                        .add(SmartPaymentArg.PAYMENT_DETAILS.getKey(), true)
                        .add(SmartPaymentArg.PAYMENT_METHOD.getKey(), paymentMethod.name())
                        .add(SmartPaymentArg.GO_BACK.getKey(), true)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton qiWiPaymentButton(String paymentUrl, PaidSubscriptionPlan paidSubscriptionPlan,
                                                  TelegramCurrencyConverter telegramCurrencyConverter, Locale locale) {
        double usd = paidSubscriptionPlan.getPrice();
        double targetPrice = telegramCurrencyConverter.convertTo(paidSubscriptionPlan.getPrice(),
                PaymentMethodService.PaymentMethod.QIWI.getCurrency());

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.PAY_COMMAND_DESCRIPTION,
                        new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                .months(paidSubscriptionPlan.getPeriod().getMonths()),
                                NumberUtils.toString(targetPrice, 2), PaymentMethodService.PaymentMethod.QIWI.getCurrency(),
                                NumberUtils.toString(usd, 2)},
                        locale)
        );

        inlineKeyboardButton.setUrl(paymentUrl);

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton yooMoneyPaymentButton(String paymentUrl, PaidSubscriptionPlan paidSubscriptionPlan,
                                                      TelegramCurrencyConverter telegramCurrencyConverter, Locale locale) {
        double usd = paidSubscriptionPlan.getPrice();
        double targetPrice = telegramCurrencyConverter.convertTo(paidSubscriptionPlan.getPrice(),
                PaymentMethodService.PaymentMethod.YOOMONEY.getCurrency());

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.PAY_COMMAND_DESCRIPTION,
                        new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                .months(paidSubscriptionPlan.getPeriod().getMonths()),
                                NumberUtils.toString(targetPrice, 2), PaymentMethodService.PaymentMethod.YOOMONEY.getCurrency(),
                                NumberUtils.toString(usd, 2)},
                        locale)
        );

        inlineKeyboardButton.setUrl(paymentUrl);

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton paymentMethod(PaymentMethodService.PaymentMethod paymentMethod, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(paymentMethod.name().toLowerCase() + ".payment.method", locale));

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartPaymentCommandNames.BUY)
                        .add(SmartPaymentArg.PAYMENT_METHOD.getKey(), paymentMethod.name())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton payButton(double usd, double targetPrice, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.INVOICE_PAY_COMMAND_DESCRIPTION,
                        new Object[]{NumberUtils.toString(targetPrice, 2),
                                subscriptionProperties.getPaymentCurrency(), NumberUtils.toString(usd, 2)},
                        locale));

        inlineKeyboardButton.setPay(true);

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton goBackButton(Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale));

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartPaymentCommandNames.BUY)
                        .add(SmartPaymentArg.GO_BACK.getKey(), true)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }
}
