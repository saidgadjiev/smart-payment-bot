package ru.gadjini.telegram.smart.payment.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MyBotCommand {

   default boolean accept(Message message) {
        return true;
    }
}
