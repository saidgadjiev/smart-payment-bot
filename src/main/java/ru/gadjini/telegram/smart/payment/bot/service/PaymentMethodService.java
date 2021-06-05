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
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.ButtonFactory;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.InlineKeyboardService;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class PaymentMethodService {

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

        for (PaymentMethod value : PaymentMethod.activeValues()) {
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
            case BUYMEACOFFEE:
                return inlineKeyboardService.nativeCurrencyKeyboard(paymentsProperties.getPaypalUrl(),
                        PaymentMethod.PAYPAL.getCurrency(), paidSubscriptionPlans, locale);
            case RAZORPAY:
                return inlineKeyboardService.nativeCurrencyKeyboard(paymentsProperties.getRazorpayUrl(),
                        PaymentMethod.RAZORPAY.getCurrency(), paidSubscriptionPlans, locale);
            case QIWI:
                return inlineKeyboardService.qiWiKeyboard(paymentsProperties.getQiwiUrl(), paidSubscriptionPlans, locale);
            case CRYPTOCURRENCY:
                return inlineKeyboardService.paymentDetailsKeyboard(PaymentMethod.CRYPTOCURRENCY,
                        PaymentMethod.CRYPTOCURRENCY.getCurrency(), paidSubscriptionPlans, locale);
            case PERFECTMONEY:
                return inlineKeyboardService.paymentDetailsKeyboard(PaymentMethod.PERFECTMONEY,
                        PaymentMethod.PERFECTMONEY.getCurrency(), paidSubscriptionPlans, locale);
            default:
                return inlineKeyboardService.telegramPaymentKeyboard(paidSubscriptionPlans, locale);
        }
    }

    public String getPaymentAdditionalInformation(PaymentMethod paymentMethod, Locale locale) {
        if (paymentMethod == PaymentMethod.TELEGRAM) {
            return localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_TELEGRAM_PAYMENT_METHOD_INFO, locale);
        } else if (paymentMethod == PaymentMethod.CRYPTOCURRENCY) {
            return localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_MANUAL_SUBSCRIPTION_RENEWAL_INFO, locale) + "\n"
                    + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_SUBSCRIPTION_RENEW_MESSAGE_ADDRESS, locale) + "\n"
                    + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_CRYPTOCURRENCY_APPS, locale);
        }
        return localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_MANUAL_SUBSCRIPTION_RENEWAL_INFO, locale) + "\n"
                + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_SUBSCRIPTION_RENEW_MESSAGE_ADDRESS, locale);
    }

    public String getCallbackAnswer(PaymentMethod paymentMethod, Locale locale) {
        return localisationService.getMessage(paymentMethod.name().toLowerCase() + ".payment.method", locale);
    }

    public enum PaymentMethod {

        PAYPAL("$", true),

        BUYMEACOFFEE("$", false),

        RAZORPAY("$", true),

        QIWI("RUB", true),

        YOOMONEY("RUB", true),

        CRYPTOCURRENCY("USDT", true),

        PERFECTMONEY("$", true),

        TELEGRAM(null, true);

        private final String currency;

        private boolean active;

        PaymentMethod(String currency, boolean active) {
            this.currency = currency;
            this.active = active;
        }

        public String getCurrency() {
            return currency;
        }

        public boolean isActive() {
            return active;
        }

        public static PaymentMethod[] activeValues() {
            return Stream.of(values()).filter(PaymentMethod::isActive).toArray(PaymentMethod[]::new);
        }
    }
}
