package ru.gadjini.telegram.smart.payment.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverter;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.payment.bot.common.CurrencyConstants;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.ButtonFactory;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.InlineKeyboardService;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class PaymentMethodService {

    private static final Consumer<TelegramCurrencyConverter> RUB_CUSTOMIZER = telegramCurrencyConverter -> {
        telegramCurrencyConverter.getTelegramCurrencies()
                .getCurrencies().get(CurrencyConstants.RUB).setMinAmount(7300);
    };

    private InlineKeyboardService inlineKeyboardService;

    private SmartInlineKeyboardService smartInlineKeyboardService;

    private LocalisationService localisationService;

    private ButtonFactory buttonFactory;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private PaymentsProperties paymentsProperties;

    private MessageService messageService;

    private MediaMessageService mediaMessageService;

    @Autowired
    public PaymentMethodService(InlineKeyboardService inlineKeyboardService,
                                SmartInlineKeyboardService smartInlineKeyboardService,
                                LocalisationService localisationService, ButtonFactory buttonFactory,
                                PaidSubscriptionPlanService paidSubscriptionPlanService,
                                PaymentsProperties paymentsProperties, @TgMessageLimitsControl MessageService messageService,
                                @Qualifier("mediaLimits") MediaMessageService mediaMessageService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.smartInlineKeyboardService = smartInlineKeyboardService;
        this.localisationService = localisationService;
        this.buttonFactory = buttonFactory;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.paymentsProperties = paymentsProperties;
        this.messageService = messageService;
        this.mediaMessageService = mediaMessageService;
    }

    public void sendPaymentDetails(long chatId, PaymentMethod paymentMethod, Locale locale) {
        if (paymentMethod == PaymentMethod.CRYPTOCURRENCY) {
            mediaMessageService.sendPhoto(
                    SendPhoto.builder()
                            .chatId(String.valueOf(chatId))
                            .photo(new InputFile(paymentsProperties.getUsdtWallet()))
                            .build()
            );
        } else if (paymentMethod == PaymentMethod.OSON) {
            mediaMessageService.sendPhoto(
                    SendPhoto.builder()
                            .chatId(String.valueOf(chatId))
                            .photo(new InputFile(paymentsProperties.getOsonWallet()))
                            .build()
            );
        } else if (paymentMethod == PaymentMethod.PERFECTMONEY) {
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(chatId))
                            .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_PERFECTMONEY_PAYMENT_DETAILS,
                                    new Object[]{paymentsProperties.getPerfectmoneyWallet()}, locale))
                            .parseMode(ParseMode.HTML)
                            .build()
            );
        }
    }

    public InlineKeyboardMarkup getPaymentMethodsKeyboard(Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.BANK_CARD, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.APPLE_PAY, locale),
                buttonFactory.paymentMethod(PaymentMethod.GOOGLE_PAY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.YANDEX_PAY, locale),
                buttonFactory.paymentMethod(PaymentMethod.SAMSUNG_PAY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.PAYPAL, locale),
                buttonFactory.paymentMethod(PaymentMethod.RAZORPAY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.QIWI, locale),
                buttonFactory.paymentMethod(PaymentMethod.YOOMONEY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.OSON, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.CRYPTOCURRENCY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.PERFECTMONEY, locale)));

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentMethod(PaymentMethod.TELEGRAM, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getPaymentKeyboard(PaymentMethod paymentMethod, Locale locale) {
        List<PaidSubscriptionPlan> paidSubscriptionPlans = paidSubscriptionPlanService.getActivePlans();
        switch (paymentMethod) {
            case YOOMONEY:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(paymentsProperties.getYoomoneyUrl(),
                        PaymentMethod.YOOMONEY.getCurrency(), paidSubscriptionPlans, locale, RUB_CUSTOMIZER);
            case PAYPAL:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(paymentsProperties.getPaypalUrl(),
                        PaymentMethod.PAYPAL.getCurrency(), paidSubscriptionPlans, locale);
            case GOOGLE_PAY:
            case APPLE_PAY:
            case BANK_CARD:
            case SAMSUNG_PAY:
            case YANDEX_PAY:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(paymentsProperties.getRobokassaUrl(locale),
                        paymentMethod.getCurrency(), paidSubscriptionPlans, locale, RUB_CUSTOMIZER);
            case RAZORPAY:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(paymentsProperties.getRazorpayUrl(),
                        PaymentMethod.RAZORPAY.getCurrency(), paidSubscriptionPlans, locale);
            case QIWI:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(paymentsProperties.getQiwiUrl(),
                        PaymentMethod.QIWI.getCurrency(), paidSubscriptionPlans, locale, RUB_CUSTOMIZER);
            case CRYPTOCURRENCY:
                return inlineKeyboardService.paymentDetailsKeyboard(PaymentMethod.CRYPTOCURRENCY, paidSubscriptionPlans, locale);
            case OSON:
                return inlineKeyboardService.paymentDetailsKeyboard(PaymentMethod.OSON, paidSubscriptionPlans, locale);
            case PERFECTMONEY:
                return inlineKeyboardService.paymentDetailsKeyboard(PaymentMethod.PERFECTMONEY, paidSubscriptionPlans, locale);
            default:
                return inlineKeyboardService.telegramPaymentKeyboard(paidSubscriptionPlans, locale);
        }
    }

    public String getPaymentAdditionalInformation(PaymentMethod paymentMethod, Locale locale) {
        if (paymentMethod == PaymentMethod.TELEGRAM) {
            return localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_TELEGRAM_PAYMENT_METHOD_INFO, locale);
        } else if (PaymentMethod.ROBOKASSA_METHODS.contains(paymentMethod)) {
            return localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_ROBOKASSA_PAYMENT_METHOD_INFO, locale) + "\n"
                    + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_MANUAL_SUBSCRIPTION_RENEWAL_INFO, locale) + "\n"
                    + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_SUBSCRIPTION_RENEW_MESSAGE_ADDRESS, locale) + "\n\n"
                    + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_ROBOKASSA_PAYMENT_METHODS, locale);
        }

        return localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_MANUAL_SUBSCRIPTION_RENEWAL_INFO, locale) + "\n"
                + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_SUBSCRIPTION_RENEW_MESSAGE_ADDRESS, locale) + "\n\n"
                + localisationService.getMessage("message." + paymentMethod.localisationPaymentAppsName() + ".apps", locale);
    }

    public String getCallbackAnswer(PaymentMethod paymentMethod, Locale locale) {
        return localisationService.getMessage(paymentMethod.localisationPaymentMethodName() + ".payment.method", locale);
    }

    public enum PaymentMethod {

        BANK_CARD("RUB"),

        GOOGLE_PAY("RUB"),

        APPLE_PAY("RUB"),

        SAMSUNG_PAY("RUB"),

        YANDEX_PAY("RUB"),

        PAYPAL("$"),

        RAZORPAY("$"),

        QIWI("RUB"),

        OSON("UZS"),

        YOOMONEY("RUB"),

        CRYPTOCURRENCY("USDT"),

        PERFECTMONEY("$"),

        TELEGRAM("UAH");

        public static Set<PaymentMethod> ROBOKASSA_METHODS = Set.of(GOOGLE_PAY, APPLE_PAY, SAMSUNG_PAY, BANK_CARD, YANDEX_PAY);

        private final String currency;

        PaymentMethod(String currency) {
            this.currency = currency;
        }

        public String getCurrency() {
            return currency;
        }

        public static PaymentMethod getValue(String name, Supplier<UserException> exceptionSupplier) {
            for (PaymentMethod value : values()) {
                if (value.name().equals(name)) {
                    return value;
                }
            }

            throw exceptionSupplier.get();
        }

        public String localisationPaymentAppsName() {
            if (ROBOKASSA_METHODS.contains(this)) {
                return "robokassa";
            }
            return name().toLowerCase().replace("_", ".");
        }

        public String localisationPaymentMethodName() {
            return name().toLowerCase().replace("_", ".");
        }
    }
}
