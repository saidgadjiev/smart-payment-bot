package ru.gadjini.telegram.smart.payment.bot.command;

import com.antkorwin.xsync.XSync;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaymentsHandler;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;
import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.property.ProfileProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.utils.SmartMath;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentArg;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;
import ru.gadjini.telegram.smart.payment.bot.service.keyboard.InlineKeyboardService;
import ru.gadjini.telegram.smart.payment.bot.service.payment.InvoicePayload;
import ru.gadjini.telegram.smart.payment.bot.service.payment.PaymentService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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

    private Gson gson;

    private XSync<Long> longXSync;

    @Autowired
    public BuySubscriptionCommand(@TgMessageLimitsControl MessageService messageService,
                                  PaidSubscriptionPlanService paidSubscriptionPlanService,
                                  ProfileProperties profileProperties, PaymentsProperties paymentsProperties,
                                  LocalisationService localisationService, UserService userService,
                                  SubscriptionTimeDeclensionProvider timeDeclensionProvider,
                                  InlineKeyboardService inlineKeyboardService, PaymentService paymentService,
                                  Gson gson, XSync<Long> longXSync) {
        this.messageService = messageService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.profileProperties = profileProperties;
        this.paymentsProperties = paymentsProperties;
        this.localisationService = localisationService;
        this.userService = userService;
        this.timeDeclensionProvider = timeDeclensionProvider;
        this.inlineKeyboardService = inlineKeyboardService;
        this.paymentService = paymentService;
        this.gson = gson;
        this.longXSync = longXSync;
    }

    @Override
    public void preCheckout(PreCheckoutQuery preCheckoutQuery) {
        Locale locale = userService.getLocaleOrDefault(preCheckoutQuery.getFrom().getId());
        PaymentService.CheckoutValidationResult checkoutValidationResult = paymentService.validateCheckout(preCheckoutQuery.getFrom().getId());

        if (checkoutValidationResult.isValid()) {
            messageService.sendAnswerPreCheckoutQuery(
                    AnswerPreCheckoutQuery.builder()
                            .preCheckoutQueryId(preCheckoutQuery.getId())
                            .ok(true)
                            .build()
            );
        } else {
            messageService.sendAnswerPreCheckoutQuery(
                    AnswerPreCheckoutQuery.builder()
                            .preCheckoutQueryId(preCheckoutQuery.getId())
                            .ok(false)
                            .errorMessage(localisationService.getMessage(
                                    SmartPaymentMessagesProperties.MESSAGE_INVALID_CHECKOUT_DATE,
                                    new Object[]{
                                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(checkoutValidationResult.getSubscriptionEndDate()),
                                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(checkoutValidationResult.getNextCheckoutDate())
                                    },
                                    locale))
                            .build()
            );
        }
    }

    @Override
    public void successfulPayment(Message message) {
        InvoicePayload invoicePayload = gson.fromJson(message.getSuccessfulPayment().getInvoicePayload(), InvoicePayload.class);
        LocalDate paidSubscriptionEndData = paymentService.processPayment(message.getFrom().getId(), invoicePayload.getPlanId());
        long paidSubscriptionDaysLeft = ChronoUnit.DAYS.between(LocalDate.now(ZoneOffset.UTC), paidSubscriptionEndData);
        Locale localeOrDefault = userService.getLocaleOrDefault(message.getFrom().getId());
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_SUCCESSFUL_PAYMENT,
                                new Object[]{timeDeclensionProvider.getService(localeOrDefault.getLanguage()).day((int) paidSubscriptionDaysLeft)},
                                localeOrDefault))
                        .build()
        );
    }

    @Override
    public void processMessage(Message message, String[] strings) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        PaidSubscriptionPlan paidSubscriptionPlan = paidSubscriptionPlanService.getActivePlan();
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_BUY_WELCOME, new Object[]{
                                timeDeclensionProvider.getService(locale.getLanguage()).months(paidSubscriptionPlan.getPeriod().getMonths())
                        }, locale))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(inlineKeyboardService.paymentKeyboard(paidSubscriptionPlan, locale))
                        .build()
        );
    }

    @Override
    public void processCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {
        longXSync.execute((long) callbackQuery.getFrom().getId(), () -> {
            Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
            PaymentService.CheckoutValidationResult checkoutValidationResult = paymentService.validateCheckout(callbackQuery.getFrom().getId());
            if (checkoutValidationResult.isValid()) {
                PaidSubscriptionPlan paidSubscriptionPlan = paidSubscriptionPlanService.getPlanById(
                        requestParams.getInt(SmartPaymentArg.PLAN_ID.getName())
                );
                SendInvoice invoice = createInvoice(callbackQuery.getFrom().getId(), paidSubscriptionPlan, locale);

                messageService.sendInvoice(invoice);
                messageService.sendAnswerCallbackQuery(
                        AnswerCallbackQuery.builder()
                                .callbackQueryId(callbackQuery.getId())
                                .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_INVOICE_SENT_ANSWER, locale))
                                .build()
                );
            } else {
                messageService.sendAnswerCallbackQuery(
                        AnswerCallbackQuery.builder()
                                .callbackQueryId(callbackQuery.getId())
                                .text(localisationService.getMessage(
                                        SmartPaymentMessagesProperties.MESSAGE_INVALID_CHECKOUT_DATE,
                                        new Object[]{
                                                PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(checkoutValidationResult.getSubscriptionEndDate()),
                                                PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(checkoutValidationResult.getNextCheckoutDate())
                                        },
                                        locale))
                                .showAlert(true)
                                .cacheTime(getCheckoutInvalidAnswerCacheTime(checkoutValidationResult.getNextCheckoutDate()))
                                .build()
                );
            }
        });
    }

    @Override
    public String getName() {
        return SmartPaymentCommandNames.BUY;
    }

    @Override
    public String getCommandIdentifier() {
        return SmartPaymentCommandNames.BUY;
    }

    private SendInvoice createInvoice(int userId, PaidSubscriptionPlan paidSubscriptionPlan, Locale locale) {
        SendInvoice sendInvoice = SendInvoice.builder()
                .chatId(userId)
                .title(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_INVOICE_TITLE, locale))
                .description(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_INVOICE_DESCRIPTION, new Object[]{
                        timeDeclensionProvider.getService(locale.getLanguage()).months(paidSubscriptionPlan.getPeriod().getMonths())
                }, locale))
                .providerToken(getPaymentProviderToken())
                .currency(paidSubscriptionPlan.getCurrency())
                .payload(gson.toJson(new InvoicePayload(paidSubscriptionPlan.getId())))
                .prices(List.of(new LabeledPrice("Pay", normalizePrice(paidSubscriptionPlan.getPrice()))))
                .startParameter("smart-payment")
                .build();

        LOGGER.debug("Send invoice({})", userId);

        return sendInvoice;
    }

    private int normalizePrice(double price) {
        return (int) (price * TgConstants.PAYMENTS_USD_FACTOR);
    }

    private int getCheckoutInvalidAnswerCacheTime(LocalDate nextCheckoutDate) {
        nextCheckoutDate = nextCheckoutDate.minusDays(3);
        long cacheTime = Duration.between(nextCheckoutDate, LocalDate.now()).toSeconds();

        return SmartMath.toExactInt(cacheTime);
    }

    private String getPaymentProviderToken() {
        return profileProperties.isActive(Profiles.PROFILE_DEV_PRIMARY)
                ? paymentsProperties.getTestToken()
                : paymentsProperties.getLiveToken();
    }
}
