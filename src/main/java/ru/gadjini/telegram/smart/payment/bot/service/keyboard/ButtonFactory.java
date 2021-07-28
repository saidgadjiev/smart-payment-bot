package ru.gadjini.telegram.smart.payment.bot.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.telegram.smart.bot.commons.command.impl.CallbackDelegate;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverter;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;
import ru.gadjini.telegram.smart.payment.bot.common.CurrencyConstants;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentArg;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.service.PaymentMethodService;

import java.util.Locale;

@Service
@SuppressWarnings("CPD-START")
public class ButtonFactory {

    private LocalisationService localisationService;

    private SubscriptionTimeDeclensionProvider timeDeclensionProvider;

    @Autowired
    public ButtonFactory(LocalisationService localisationService, SubscriptionTimeDeclensionProvider timeDeclensionProvider) {
        this.localisationService = localisationService;
        this.timeDeclensionProvider = timeDeclensionProvider;
    }

    public InlineKeyboardButton telegramPaymentButton(PaidSubscriptionPlan paidSubscriptionPlan,
                                                      TelegramCurrencyConverter telegramCurrencyConverter, Locale locale) {
        double usd = paidSubscriptionPlan.getPrice();
        double targetPrice = telegramCurrencyConverter.convertTo(paidSubscriptionPlan.getPrice(), PaymentMethodService.PaymentMethod.TELEGRAM.getCurrency());

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.PAY_COMMAND_DESCRIPTION,
                        new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                .localize(paidSubscriptionPlan.getPeriod()),
                                NumberUtils.toString(targetPrice, 2), PaymentMethodService.PaymentMethod.TELEGRAM.getCurrency(),
                                NumberUtils.toString(usd, 2)},
                        locale)
        );

        inlineKeyboardButton.setCallbackData(SmartPaymentCommandNames.BUY + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(SmartPaymentArg.PLAN_ID.getKey(), paidSubscriptionPlan.getId())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton paymentDetailsButton(PaymentMethodService.PaymentMethod paymentMethod,
                                                     String currency, TelegramCurrencyConverter telegramCurrencyConverter,
                                                     PaidSubscriptionPlan paidSubscriptionPlan, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton;
        if (CurrencyConstants.USD_SYMBOL.equals(currency)) {
            inlineKeyboardButton = new InlineKeyboardButton(
                    localisationService.getMessage(SmartPaymentMessagesProperties.PAY_TARGET_COMMAND_DESCRIPTION,
                            new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                    .localize(paidSubscriptionPlan.getPeriod()),
                                    NumberUtils.toString(paidSubscriptionPlan.getPrice(), 2) + currency
                            },
                            locale)
            );
        } else {
            double usd = paidSubscriptionPlan.getPrice();
            double targetPrice = telegramCurrencyConverter.convertTo(paidSubscriptionPlan.getPrice(), currency);

            inlineKeyboardButton = new InlineKeyboardButton(
                    localisationService.getMessage(SmartPaymentMessagesProperties.PAY_COMMAND_DESCRIPTION,
                            new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                    .localize(paidSubscriptionPlan.getPeriod()),
                                    NumberUtils.toString(targetPrice, 2), currency,
                                    NumberUtils.toString(usd, 2)},
                            locale)
            );
        }

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartPaymentCommandNames.BUY)
                        .add(SmartPaymentArg.PAYMENT_DETAILS.getKey(), true)
                        .add(SmartPaymentArg.PAYMENT_METHOD.getKey(), paymentMethod.name())
                        .add(SmartPaymentArg.GO_BACK.getKey(), true)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton paymentUrlPaymentButton(String paymentUrl, PaidSubscriptionPlan paidSubscriptionPlan,
                                                        String currency, TelegramCurrencyConverter telegramCurrencyConverter, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton;
        if (CurrencyConstants.USD_SYMBOL.equals(currency)) {
            inlineKeyboardButton = new InlineKeyboardButton(
                    localisationService.getMessage(SmartPaymentMessagesProperties.PAY_TARGET_COMMAND_DESCRIPTION,
                            new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                    .localize(paidSubscriptionPlan.getPeriod()),
                                    NumberUtils.toString(paidSubscriptionPlan.getPrice(), 2) + currency
                            },
                            locale)
            );
        } else {
            double usd = paidSubscriptionPlan.getPrice();
            double targetPrice = telegramCurrencyConverter.convertTo(paidSubscriptionPlan.getPrice(), currency);

            inlineKeyboardButton = new InlineKeyboardButton(
                    localisationService.getMessage(SmartPaymentMessagesProperties.PAY_COMMAND_DESCRIPTION,
                            new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                    .localize(paidSubscriptionPlan.getPeriod()),
                                    NumberUtils.toString(targetPrice, 2), currency,
                                    NumberUtils.toString(usd, 2)},
                            locale)
            );
        }

        inlineKeyboardButton.setUrl(paymentUrl);

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton tariffButton(PaidSubscriptionTariffType tariffType, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(tariffType.name().toLowerCase() + ".payment.plan", locale));

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartPaymentCommandNames.BUY)
                        .add(SmartPaymentArg.PAYMENT_TARIFF.getKey(), tariffType.name())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton paymentMethod(PaidSubscriptionTariffType tariffType,
                                              PaymentMethodService.PaymentMethod paymentMethod, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(paymentMethod.localisationPaymentMethodName() + ".payment.method", locale));

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartPaymentCommandNames.BUY)
                        .add(SmartPaymentArg.PAYMENT_TARIFF.getKey(), tariffType.name())
                        .add(SmartPaymentArg.PAYMENT_METHOD.getKey(), paymentMethod.name())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton payButton(double usd, double targetPrice, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.INVOICE_PAY_COMMAND_DESCRIPTION,
                        new Object[]{NumberUtils.toString(targetPrice, 2),
                                PaymentMethodService.PaymentMethod.TELEGRAM.getCurrency(), NumberUtils.toString(usd, 2)},
                        locale));

        inlineKeyboardButton.setPay(true);

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton goBackButton(PaidSubscriptionTariffType tariff, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale));

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartPaymentCommandNames.BUY)
                        .add(SmartPaymentArg.GO_BACK.getKey(), true)
                        .add(SmartPaymentArg.PAYMENT_TARIFF.getKey(), tariff.name())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }


    public InlineKeyboardButton goToTariffs(Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale));

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartPaymentCommandNames.BUY)
                        .add(SmartPaymentArg.GO_TO_TARIFFS.getKey(), true)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }
}
