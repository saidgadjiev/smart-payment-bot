package ru.gadjini.telegram.smart.payment.bot.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaymentsHandler;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;
import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.property.ProfileProperties;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverter;
import ru.gadjini.telegram.smart.bot.commons.service.currency.TelegramCurrencyConverterFactory;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentArg;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.model.InvoicePayload;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;
import ru.gadjini.telegram.smart.payment.bot.service.PaymentMethodService;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.InlineKeyboardService;
import ru.gadjini.telegram.smart.payment.bot.service.message.PaymentMessageBuilder;
import ru.gadjini.telegram.smart.payment.bot.service.payment.PaymentService;

import java.util.List;
import java.util.Locale;

@Component
public class BuySubscriptionCommand implements BotCommand, PaymentsHandler, CallbackBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuySubscriptionCommand.class);

    private MessageService messageService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private ProfileProperties profileProperties;

    private PaymentsProperties paymentsProperties;

    private LocalisationService localisationService;

    private UserService userService;

    private SubscriptionTimeDeclensionProvider timeDeclensionProvider;

    private InlineKeyboardService inlineKeyboardService;

    private PaymentService paymentService;

    private TelegramCurrencyConverterFactory telegramCurrencyConverterFactory;

    private PaymentMethodService paymentMethodService;

    private PaymentMessageBuilder messageBuilder;

    private Jackson jackson;

    @Autowired
    public BuySubscriptionCommand(@TgMessageLimitsControl MessageService messageService,
                                  PaidSubscriptionPlanService paidSubscriptionPlanService,
                                  ProfileProperties profileProperties, PaymentsProperties paymentsProperties,
                                  LocalisationService localisationService, UserService userService,
                                  SubscriptionTimeDeclensionProvider timeDeclensionProvider,
                                  InlineKeyboardService inlineKeyboardService, PaymentService paymentService,
                                  TelegramCurrencyConverterFactory telegramCurrencyConverterFactory,
                                  PaymentMethodService paymentMethodService, PaymentMessageBuilder messageBuilder,
                                  Jackson jackson) {
        this.messageService = messageService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.profileProperties = profileProperties;
        this.paymentsProperties = paymentsProperties;
        this.localisationService = localisationService;
        this.userService = userService;
        this.timeDeclensionProvider = timeDeclensionProvider;
        this.inlineKeyboardService = inlineKeyboardService;
        this.paymentService = paymentService;
        this.telegramCurrencyConverterFactory = telegramCurrencyConverterFactory;
        this.paymentMethodService = paymentMethodService;
        this.messageBuilder = messageBuilder;
        this.jackson = jackson;
    }

    @Override
    public void preCheckout(PreCheckoutQuery preCheckoutQuery) {
        LOGGER.debug("Start preCheckout({})", preCheckoutQuery.getFrom().getId());
        Locale locale = userService.getLocaleOrDefault(preCheckoutQuery.getFrom().getId());

        try {
            InvoicePayload invoicePayload = jackson.readValue(preCheckoutQuery.getInvoicePayload(), InvoicePayload.class);

            messageService.sendAnswerPreCheckoutQuery(
                    AnswerPreCheckoutQuery.builder()
                            .preCheckoutQueryId(preCheckoutQuery.getId())
                            .ok(true)
                            .build()
            );
            LOGGER.debug("Success pre checkout({}, {})", preCheckoutQuery.getFrom().getId(), invoicePayload.getPlanId());
        } catch (Throwable e) {
            LOGGER.error("(" + preCheckoutQuery.getFrom().getId() + ")" + e.getMessage(), e);
            messageService.sendAnswerPreCheckoutQuery(
                    AnswerPreCheckoutQuery.builder()
                            .preCheckoutQueryId(preCheckoutQuery.getId())
                            .ok(false)
                            .errorMessage(localisationService.getMessage(
                                    SmartPaymentMessagesProperties.MESSAGE_PRE_CHECKOUT_ERROR,
                                    locale))
                            .build()
            );
        }
    }

    @Override
    public void successfulPayment(Message message) {
        LOGGER.debug("Start successfulPayment({})", message.getFrom().getId());
        InvoicePayload invoicePayload = jackson.readValue(message.getSuccessfulPayment().getInvoicePayload(), InvoicePayload.class);
        PaidSubscription paidSubscription = paymentService.processPayment(message.getFrom().getId(), invoicePayload.getPlanId());
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text(messageBuilder.getSuccessfulPaymentMessage(paidSubscription, locale))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
        LOGGER.debug("Successful payment({}, {})", message.getFrom().getId(), invoicePayload.getPlanId());
    }

    @Override
    public void processMessage(Message message, String[] strings) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text(messageBuilder.getBuyWelcome(locale))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(paymentMethodService.getPaidSubscriptionTariffKeyboard(locale))
                        .build()
        );
    }

    @Override
    public void processCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {
        LOGGER.debug("Start send invoice({})", callbackQuery.getFrom().getId());
        Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
        PaidSubscriptionPlan paidSubscriptionPlan = paidSubscriptionPlanService.getPlanById(
                requestParams.getInt(SmartPaymentArg.PLAN_ID.getKey())
        );
        SendInvoice invoice = createInvoice(callbackQuery.getFrom().getId(), paidSubscriptionPlan, locale);

        messageService.sendInvoice(invoice);
        messageService.sendAnswerCallbackQuery(
                AnswerCallbackQuery.builder()
                        .callbackQueryId(callbackQuery.getId())
                        .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_INVOICE_SENT_ANSWER, locale))
                        .build()
        );
        LOGGER.debug("Send invoice({}, {})", callbackQuery.getFrom().getId(), paidSubscriptionPlan.getId());
    }

    @Override
    public void processNonCommandCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {
        if (requestParams.contains(SmartPaymentArg.PAYMENT_DETAILS.getKey())) {
            Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
            PaymentMethodService.PaymentMethod paymentMethod = PaymentMethodService.PaymentMethod
                    .getValue(requestParams.getString(SmartPaymentArg.PAYMENT_METHOD.getKey()), () -> {
                        return new UserException(localisationService.getMessage(
                                SmartPaymentMessagesProperties.MESSAGE_INACTIVE_PAYMENT_METHOD, locale));
                    });
            LOGGER.debug("Payment details({})", paymentMethod);
            paymentMethodService.sendPaymentDetails(callbackQuery.getFrom().getId(), paymentMethod, locale);
            messageService.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_PAYMENT_DETAILS_ANSWER, locale))
                    .build());
        } else if (requestParams.contains(SmartPaymentArg.PAYMENT_METHOD.getKey())) {
            Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
            PaymentMethodService.PaymentMethod paymentMethod = PaymentMethodService.PaymentMethod
                    .getValue(requestParams.getString(SmartPaymentArg.PAYMENT_METHOD.getKey()), () -> {
                        return new UserException(localisationService.getMessage(
                                SmartPaymentMessagesProperties.MESSAGE_INACTIVE_PAYMENT_METHOD, locale));
                    });
            LOGGER.debug("Payment method({},{})", callbackQuery.getFrom().getId(), paymentMethod.name());

            PaidSubscriptionTariffType tariffType = requestParams.get(SmartPaymentArg.PAYMENT_TARIFF.getKey(),
                    PaidSubscriptionTariffType::valueOf);

            messageService.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            if (paymentMethod.isCompositeMethod()) {
                messageService.sendMessage(
                        SendMessage.builder()
                                .chatId(String.valueOf(callbackQuery.getMessage().getChatId()))
                                .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_BUY_WELCOME,
                                        new Object[]{
                                                localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_FEATURES, locale)
                                        }, locale)
                                        + "\n\n" + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_CHOOSE_PAYMENT_SERVICE, locale))
                                .parseMode(ParseMode.HTML)
                                .replyMarkup(paymentMethodService.getCompositePaymentMethodKeyboard(tariffType, paymentMethod, locale))
                                .build()
                );
            } else {
                messageService.sendMessage(
                        SendMessage.builder()
                                .chatId(String.valueOf(callbackQuery.getMessage().getChatId()))
                                .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_BUY_WELCOME,
                                        new Object[]{
                                                localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_FEATURES, locale)
                                        }, locale)
                                        + "\n\n" + paymentMethodService.getPaymentAdditionalInformation(paymentMethod, locale))
                                .parseMode(ParseMode.HTML)
                                .replyMarkup(paymentMethodService.getPaymentKeyboard(tariffType, paymentMethod, locale))
                                .build()
                );
            }

            messageService.sendAnswerCallbackQuery(
                    AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId())
                            .text(paymentMethodService.getCallbackAnswer(paymentMethod, locale))
                            .build()
            );
        } else if (requestParams.contains(SmartPaymentArg.GO_TO_TARIFFS.getKey())) {
            messageService.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(callbackQuery.getMessage().getChatId()))
                            .text(messageBuilder.getBuyWelcome(locale))
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(paymentMethodService.getPaidSubscriptionTariffKeyboard(locale))
                            .build()
            );
        } else if (requestParams.contains(SmartPaymentArg.GO_BACK.getKey())) {
            PaidSubscriptionTariffType tariffType = requestParams.get(SmartPaymentArg.PAYMENT_TARIFF.getKey(),
                    PaidSubscriptionTariffType::valueOf);

            Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
            messageService.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(callbackQuery.getFrom().getId()))
                            .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_BUY_WELCOME,
                                    new Object[]{
                                            localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_FEATURES, locale)
                                    },
                                    locale)
                                    + "\n\n" + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_CHOOSE_CONVENIENT_PAYMENT_METHOD, locale))
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(paymentMethodService.getPaymentMethodsKeyboard(tariffType, locale))
                            .build()
            );

            messageService.sendAnswerCallbackQuery(
                    AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId())
                            .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_CHOOSE_CONVENIENT_PAYMENT_METHOD_ANSWER, locale))
                            .build()
            );
        } else if (requestParams.contains(SmartPaymentArg.PAYMENT_TARIFF.getKey())) {
            Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());

            PaidSubscriptionTariffType tariffType = requestParams.get(SmartPaymentArg.PAYMENT_TARIFF.getKey(),
                    PaidSubscriptionTariffType::valueOf);

            messageService.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(callbackQuery.getMessage().getChatId()))
                            .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_BUY_WELCOME,
                                    new Object[]{
                                            localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_FEATURES, locale)
                                    }, locale)
                                    + "\n\n" + localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_CHOOSE_CONVENIENT_PAYMENT_METHOD, locale))
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(paymentMethodService.getPaymentMethodsKeyboard(tariffType, locale))
                            .build()
            );
        }
    }

    @Override
    public String getName() {
        return SmartPaymentCommandNames.BUY;
    }

    @Override
    public String getCommandIdentifier() {
        return SmartPaymentCommandNames.BUY;
    }

    private SendInvoice createInvoice(long userId, PaidSubscriptionPlan paidSubscriptionPlan, Locale locale) {
        LOGGER.debug("Create new invoice({})", userId);
        double usd = paidSubscriptionPlan.getPrice();
        TelegramCurrencyConverter converter = telegramCurrencyConverterFactory.createConverter();
        double targetPrice = NumberUtils.round(converter.convertTo(usd, PaymentMethodService.PaymentMethod.TELEGRAM.getCurrency()), 2);
        String description = localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_INVOICE_DESCRIPTION, new Object[]{
                timeDeclensionProvider.getService(locale.getLanguage()).localize(paidSubscriptionPlan.getPeriod())
        }, locale);

        SendInvoice.SendInvoiceBuilder sendInvoiceBuilder = SendInvoice.builder()
                .chatId(String.valueOf(userId))
                .title(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_INVOICE_TITLE, locale))
                .description(description)
                .providerToken(getPaymentProviderToken())
                .currency(PaymentMethodService.PaymentMethod.TELEGRAM.getCurrency())
                .payload(jackson.writeValueAsString(new InvoicePayload(paidSubscriptionPlan.getId())))
                .prices(List.of(new LabeledPrice("Pay", normalizePrice(targetPrice))))
                .replyMarkup(inlineKeyboardService.invoiceKeyboard(usd, targetPrice, locale))
                .startParameter("SmartPayment");

        return sendInvoiceBuilder.build();
    }

    private int normalizePrice(double price) {
        return (int) (price * TgConstants.PAYMENTS_AMOUNT_FACTOR);
    }

    private String getPaymentProviderToken() {
        return profileProperties.isActive(Profiles.PROFILE_DEV_PRIMARY)
                ? paymentsProperties.getTestToken()
                : paymentsProperties.getLiveToken();
    }
}
