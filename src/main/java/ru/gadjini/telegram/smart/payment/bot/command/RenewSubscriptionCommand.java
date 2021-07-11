package ru.gadjini.telegram.smart.payment.bot.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.service.payment.PaymentService;

import java.util.Locale;

@Component
public class RenewSubscriptionCommand implements BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenewSubscriptionCommand.class);

    private LocalisationService localisationService;

    private UserService userService;

    private PaymentService paymentService;

    private MessageService messageService;

    @Autowired
    public RenewSubscriptionCommand(LocalisationService localisationService,
                                    UserService userService, PaymentService paymentService,
                                    @TgMessageLimitsControl MessageService messageService) {
        this.localisationService = localisationService;
        this.userService = userService;
        this.paymentService = paymentService;
        this.messageService = messageService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        long userId = Long.parseLong(params[0]);
        int planId = Integer.parseInt(params[1]);

        PaidSubscription paidSubscription = paymentService.processPayment(userId, planId);
        Locale userLocale = userService.getLocaleOrDefault(userId);
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(userId))
                        .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_SUCCESSFUL_PAYMENT,
                                new Object[]{PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate())},
                                userLocale))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
        if (userId != message.getFrom().getId() && userService.isAdmin(message.getFrom().getId())) {
            Locale localeOrDefault = userService.getLocaleOrDefault(message.getFrom().getId());
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(message.getChatId()))
                            .text(localisationService.getMessage(SmartPaymentMessagesProperties.MESSAGE_SUCCESSFUL_PAYMENT,
                                    new Object[]{PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate())},
                                    localeOrDefault))
                            .parseMode(ParseMode.HTML)
                            .build()
            );
        }
        LOGGER.debug("Success renew({},{})", userId, planId);
    }

    @Override
    public String getCommandIdentifier() {
        return SmartPaymentCommandNames.RENEW_SUBSCRIPTION_COMMAND;
    }
}
