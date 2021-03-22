package ru.gadjini.telegram.smart.payment.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.payment.bot.common.MessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;

import java.util.Locale;

@Component
public class SupportCommand implements BotCommand {

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    @Autowired
    public SupportCommand(@TgMessageLimitsControl MessageService messageService,
                          LocalisationService localisationService, UserService userService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        messageService.sendMessage(
                SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_PAYMENT_SUPPORT, locale))
                .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return SmartPaymentCommandNames.SUPPORT;
    }
}
