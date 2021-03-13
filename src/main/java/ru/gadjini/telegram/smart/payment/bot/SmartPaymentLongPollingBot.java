package ru.gadjini.telegram.smart.payment.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.payment.filter.BotFilter;
import ru.gadjini.telegram.smart.payment.property.BotProperties;

@Component
public class SmartPaymentLongPollingBot extends TelegramLongPollingBot {

    private BotProperties botProperties;

    private BotFilter botFilter;

    @Autowired
    public SmartPaymentLongPollingBot(BotProperties botProperties, BotFilter botFilter) {
        this.botProperties = botProperties;
        this.botFilter = botFilter;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        botFilter.doFilter(update);
    }
}
