package ru.gadjini.telegram.smart.payment.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotCommand extends MyBotCommand {

    void processMessage(Message message, String[] args);

    String getCommandName();
}
