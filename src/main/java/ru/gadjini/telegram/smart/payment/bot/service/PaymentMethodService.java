package ru.gadjini.telegram.smart.payment.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionTariff;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverter;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.payment.bot.common.CurrencyConstants;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.ButtonFactory;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.InlineKeyboardService;

import java.util.*;
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

    private PaidSubscriptionTariffService tariffService;

    private UserService userService;

    @Autowired
    public PaymentMethodService(InlineKeyboardService inlineKeyboardService,
                                SmartInlineKeyboardService smartInlineKeyboardService,
                                LocalisationService localisationService, ButtonFactory buttonFactory,
                                PaidSubscriptionPlanService paidSubscriptionPlanService,
                                PaymentsProperties paymentsProperties, @TgMessageLimitsControl MessageService messageService,
                                @Qualifier("mediaLimits") MediaMessageService mediaMessageService,
                                PaidSubscriptionTariffService tariffService, UserService userService) {
        this.inlineKeyboardService = inlineKeyboardService;
        this.smartInlineKeyboardService = smartInlineKeyboardService;
        this.localisationService = localisationService;
        this.buttonFactory = buttonFactory;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.paymentsProperties = paymentsProperties;
        this.messageService = messageService;
        this.mediaMessageService = mediaMessageService;
        this.tariffService = tariffService;
        this.userService = userService;
    }

    public void sendPaymentDetails(long chatId, PaymentMethod paymentMethod, Locale locale) {
        if (paymentMethod == PaymentMethod.CRYPTOCURRENCY) {
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(chatId))
                            .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_CRYPTO_PAYMENT_DETAILS,
                                    userService.getLocaleOrDefault(chatId)))
                            .build()
            );
        } else if (paymentMethod == PaymentMethod.OSON) {
            mediaMessageService.sendVideo(
                    SendVideo.builder()
                            .chatId(String.valueOf(chatId))
                            .video(new InputFile(paymentsProperties.getOsonWallet()))
                            .caption(localisationService.getMessage(
                                    SmartPaymentMessagesProperties.MESSAGE_OSON_USAGE, locale
                            ))
                            .supportsStreaming(true)
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

    public InlineKeyboardMarkup getPaidSubscriptionTariffKeyboard(Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        List<PaidSubscriptionTariff> activeTariffs = tariffService.getActiveTariffs();
        if (activeTariffs.size() == 1) {
            return getPaymentMethodsKeyboard(activeTariffs.iterator().next().getTariffType(), locale);
        } else {
            for (PaidSubscriptionTariff tariff : activeTariffs) {
                inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.tariffButton(tariff.getTariffType(), locale)));
            }
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getPaymentMethodsKeyboard(PaidSubscriptionTariffType tariffType, Locale locale) {
        return inlineKeyboardService.getPaymentMethodsKeyboard(tariffType, locale);
    }

    public InlineKeyboardMarkup getCompositePaymentMethodKeyboard(PaidSubscriptionTariffType tariffType, PaymentMethod paymentMethod, Locale locale) {
        return inlineKeyboardService.getCompositePaymentMethodsKeyboard(tariffType,
                getInnerPaymentMethods(paymentMethod), locale);
    }

    public InlineKeyboardMarkup getPaymentKeyboard(PaidSubscriptionTariffType tariffType, PaymentMethod paymentMethod, Locale locale) {
        List<PaidSubscriptionPlan> paidSubscriptionPlans = paidSubscriptionPlanService.getActivePlans(tariffType);
        switch (paymentMethod) {
            case YOOMONEY:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(tariffType, paymentsProperties.getYoomoneyUrl(),
                        PaymentMethod.YOOMONEY.getCurrency(), paidSubscriptionPlans, locale, RUB_CUSTOMIZER);
            case PAYPAL:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(tariffType, paymentsProperties.getPaypalUrl(),
                        PaymentMethod.PAYPAL.getCurrency(), paidSubscriptionPlans, locale);
            case BUYMEACOFFEE:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(tariffType, paymentsProperties.getBuymeacoffeeUrl(),
                        PaymentMethod.BUYMEACOFFEE.getCurrency(), paidSubscriptionPlans, locale);
            case GOOGLE_PAY:
            case APPLE_PAY:
            case BANK_CARD:
            case SAMSUNG_PAY:
            case YANDEX_PAY:
            case WEBMONEY:
            case ROBOKASSA:
                Map<Double, String> paymentUrls = new HashMap<>();
                for (PaidSubscriptionPlan paidSubscriptionPlan : paidSubscriptionPlans) {
                    paymentUrls.put(paidSubscriptionPlan.getPrice(), paymentsProperties.getRobokassaUrl(paidSubscriptionPlan.getPrice(), locale));
                }

                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(tariffType, paymentUrls,
                        paymentMethod.getCurrency(), paidSubscriptionPlans, locale, RUB_CUSTOMIZER);
            case RAZORPAY:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(tariffType, paymentsProperties.getRazorpayUrl(),
                        PaymentMethod.RAZORPAY.getCurrency(), paidSubscriptionPlans, locale);
            case QIWI:
                return inlineKeyboardService.paymentUrlPaymentMethodKeyboard(tariffType, paymentsProperties.getQiwiUrl(),
                        PaymentMethod.QIWI.getCurrency(), paidSubscriptionPlans, locale, RUB_CUSTOMIZER);
            case CRYPTOCURRENCY:
                return inlineKeyboardService.paymentDetailsKeyboard(tariffType, PaymentMethod.CRYPTOCURRENCY, paidSubscriptionPlans, locale);
            case OSON:
                return inlineKeyboardService.paymentDetailsKeyboard(tariffType, PaymentMethod.OSON, paidSubscriptionPlans, locale);
            case PERFECTMONEY:
                return inlineKeyboardService.paymentDetailsKeyboard(tariffType, PaymentMethod.PERFECTMONEY, paidSubscriptionPlans, locale);
            default:
                return inlineKeyboardService.telegramPaymentKeyboard(tariffType, paidSubscriptionPlans, locale);
        }
    }

    public String getPaymentAdditionalInformation(PaymentMethod paymentMethod, Locale locale) {
        StringBuilder paymentMethodInfo = new StringBuilder();
        if (paymentMethod == PaymentMethod.TELEGRAM) {
            paymentMethodInfo
                    .append(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_VISA_MASTER_ONLY, locale));
        }
        if (paymentMethodInfo.length() > 0) {
            paymentMethodInfo.append("\n");
        }
        paymentMethodInfo
                .append(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_NON_NATIVE_CURRENCY, new Object[]{
                        paymentMethod.getCurrency()
                }, locale))
                .append("\n")
                .append(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_MANUAL_SUBSCRIPTION_RENEWAL_INFO, locale))
                .append("\n")
                .append(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_SUBSCRIPTION_RENEW_MESSAGE_ADDRESS, locale));

        return paymentMethodInfo.toString();
    }

    public String getCallbackAnswer(PaymentMethod paymentMethod, Locale locale) {
        return localisationService.getMessage(paymentMethod.localisationPaymentMethodName() + ".payment.method", locale);
    }

    private Set<PaymentMethod> getInnerPaymentMethods(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case BANK_CARD:
                return Set.of(PaymentMethod.BUYMEACOFFEE, PaymentMethod.ROBOKASSA, PaymentMethod.TELEGRAM);
            case GOOGLE_PAY:
            case APPLE_PAY:
                return Set.of(PaymentMethod.BUYMEACOFFEE, PaymentMethod.ROBOKASSA);
        }

        return Set.of();
    }

    public enum PaymentMethod {

        BANK_CARD("RUB", true),

        GOOGLE_PAY("RUB", true),

        APPLE_PAY("RUB", true),

        SAMSUNG_PAY("RUB"),

        YANDEX_PAY("RUB"),

        PAYPAL("$"),

        RAZORPAY("$"),

        QIWI("RUB"),

        WEBMONEY("RUB"),

        OSON("UZS"),

        YOOMONEY("RUB"),

        CRYPTOCURRENCY("USDT"),

        PERFECTMONEY("$"),

        TELEGRAM("UAH"),

        BUYMEACOFFEE("$"),

        ROBOKASSA("RUB");

        private final String currency;

        private final boolean compositeMethod;

        PaymentMethod(String currency) {
            this(currency, false);
        }

        PaymentMethod(String currency, boolean compositeMethod) {
            this.currency = currency;
            this.compositeMethod = compositeMethod;
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

        public String localisationPaymentMethodName() {
            return name().toLowerCase().replace("_", ".");
        }

        public boolean isCompositeMethod() {
            return compositeMethod;
        }
    }
}
