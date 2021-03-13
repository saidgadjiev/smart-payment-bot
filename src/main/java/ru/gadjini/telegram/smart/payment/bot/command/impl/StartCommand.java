package ru.gadjini.telegram.smart.payment.bot.command.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.payment.bot.command.api.BotCommand;
import ru.gadjini.telegram.smart.payment.common.CommandNames;

@Component
public class StartCommand implements BotCommand {

    @Override
    public String getCommandName() {
        return CommandNames.START;
    }

    @Override
    public void processMessage(Message message, String[] args) {

    }
}
