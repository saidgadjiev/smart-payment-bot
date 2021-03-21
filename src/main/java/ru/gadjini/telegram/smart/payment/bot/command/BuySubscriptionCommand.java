package ru.gadjini.telegram.smart.payment.bot.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;
import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;
import ru.gadjini.telegram.smart.bot.commons.property.ProfileProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.payment.bot.common.MessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;
import ru.gadjini.telegram.smart.payment.bot.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.payment.bot.property.PaymentsProperties;
import ru.gadjini.telegram.smart.payment.bot.service.PaidSubscriptionPlanService;

import java.util.List;
import java.util.Locale;

@Component
public class BuySubscriptionCommand implements BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuySubscriptionCommand.class);

    private MessageService messageService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private ProfileProperties profileProperties;

    private PaymentsProperties paymentsProperties;

    private LocalisationService localisationService;

    private UserService userService;

    @Autowired
    public BuySubscriptionCommand(@TgMessageLimitsControl MessageService messageService,
                                  PaidSubscriptionPlanService paidSubscriptionPlanService,
                                  ProfileProperties profileProperties, PaymentsProperties paymentsProperties,
                                  LocalisationService localisationService, UserService userService) {
        this.messageService = messageService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.profileProperties = profileProperties;
        this.paymentsProperties = paymentsProperties;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @Override
    public void processMessage(Message message, String[] strings) {
        PaidSubscriptionPlan paidSubscriptionPlan = paidSubscriptionPlanService.getPlan();
        SendInvoice invoice = createInvoice(message.getFrom().getId(), paidSubscriptionPlan);

        messageService.sendInvoice(invoice);
    }

    @Override
    public String getCommandIdentifier() {
        return SmartPaymentCommandNames.BUY_COMMAND;
    }

    private SendInvoice createInvoice(int userId, PaidSubscriptionPlan paidSubscriptionPlan) {
        Locale locale = userService.getLocaleOrDefault(userId);
        SendInvoice sendInvoice = SendInvoice.builder()
                .chatId(userId)
                .title(localisationService.getMessage(MessagesProperties.MESSAGE_INVOICE_TITLE, locale))
                .description(localisationService.getMessage(MessagesProperties.MESSAGE_INVOICE_DESCRIPTION, locale))
                .providerToken(getPaymentProviderToken())
                .currency(paidSubscriptionPlan.getCurrency())
                .prices(List.of(new LabeledPrice("Pay", normalizePrice(paidSubscriptionPlan.getPrice()))))
                .startParameter("smart-payment")
                .payload(String.valueOf(userId))
                .build();

        LOGGER.debug("Send invoice({})", userId);

        return sendInvoice;
    }

    private int normalizePrice(double price) {
        return (int) (price * TgConstants.PAYMENTS_USD_FACTOR);
    }

    private String getPaymentProviderToken() {
        return profileProperties.isActive(Profiles.PROFILE_DEV_PRIMARY)
                ? paymentsProperties.getTestToken()
                : paymentsProperties.getLiveToken();
    }
}
