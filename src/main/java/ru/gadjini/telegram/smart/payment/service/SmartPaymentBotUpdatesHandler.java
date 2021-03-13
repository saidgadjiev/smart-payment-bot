package ru.gadjini.telegram.smart.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.payment.common.MessagesProperties;
import ru.gadjini.telegram.smart.payment.service.command.CommandExecutor;
import ru.gadjini.telegram.smart.payment.service.message.MessageService;

@Component
public class SmartPaymentBotUpdatesHandler {

    private CommandExecutor commandExecutor;

    private MessageService messageService;

    private UserService userService;

    private LocalisationService localisationService;

    @Autowired
    public SmartPaymentBotUpdatesHandler(CommandExecutor commandExecutor, MessageService messageService,
                                         UserService userService, LocalisationService localisationService) {
        this.commandExecutor = commandExecutor;
        this.messageService = messageService;
        this.userService = userService;
        this.localisationService = localisationService;
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage()
                && commandExecutor.isBotCommand(update.getMessage())
                && !commandExecutor.executeBotCommand(update.getMessage())) {
            messageService.sendMessage(
                    SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_UNKNOWN_COMMAND,
                                    userService.getLocaleOrDefault(update.getMessage().getFrom().getId())))
                            .parseMode(ParseMode.HTML)
                            .build());
        }

    }
}
