package ru.gadjini.telegram.smart.payment.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.payment.service.SmartPaymentBotUpdatesHandler;

@Component
public class UpdateFilter extends BaseBotFilter {

    private SmartPaymentBotUpdatesHandler smartPaymentBotUpdatesHandler;

    @Autowired
    public UpdateFilter(SmartPaymentBotUpdatesHandler smartPaymentBotUpdatesHandler) {
        this.smartPaymentBotUpdatesHandler = smartPaymentBotUpdatesHandler;
    }

    @Override
    public void doFilter(Update update) {
        smartPaymentBotUpdatesHandler.onUpdateReceived(update);
    }
}
