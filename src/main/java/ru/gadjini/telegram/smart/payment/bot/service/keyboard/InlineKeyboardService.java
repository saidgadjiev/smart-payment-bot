package ru.gadjini.telegram.smart.payment.bot.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.payment.bot.domain.PaidSubscriptionPlan;

import java.util.List;
import java.util.Locale;

@Service
public class InlineKeyboardService {

    private SmartInlineKeyboardService smartInlineKeyboardService;

    private ButtonFactory buttonFactory;

    @Autowired
    public InlineKeyboardService(SmartInlineKeyboardService smartInlineKeyboardService, ButtonFactory buttonFactory) {
        this.smartInlineKeyboardService = smartInlineKeyboardService;
        this.buttonFactory = buttonFactory;
    }

    public InlineKeyboardMarkup paymentKeyboard(PaidSubscriptionPlan paidSubscriptionPlan, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.paymentButton(paidSubscriptionPlan.getPrice(),
                paidSubscriptionPlan.getPeriod(), locale)));

        return inlineKeyboardMarkup;
    }
}
