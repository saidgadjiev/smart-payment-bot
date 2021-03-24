package ru.gadjini.telegram.smart.payment.bot.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentArg;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;
import ru.gadjini.telegram.smart.payment.bot.domain.PaidSubscriptionPlan;

import java.util.Locale;

@Service
public class ButtonFactory {

    private LocalisationService localisationService;

    private SubscriptionTimeDeclensionProvider timeDeclensionProvider;

    @Autowired
    public ButtonFactory(LocalisationService localisationService, SubscriptionTimeDeclensionProvider timeDeclensionProvider) {
        this.localisationService = localisationService;
        this.timeDeclensionProvider = timeDeclensionProvider;
    }

    public InlineKeyboardButton paymentButton(PaidSubscriptionPlan paidSubscriptionPlan, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(SmartPaymentMessagesProperties.PAY_COMMAND_DESCRIPTION,
                        new Object[]{timeDeclensionProvider.getService(locale.getLanguage())
                                .months(paidSubscriptionPlan.getPeriod().getMonths()), String.valueOf(paidSubscriptionPlan.getPrice())},
                        locale)
        );

        inlineKeyboardButton.setCallbackData(SmartPaymentCommandNames.BUY + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(SmartPaymentArg.PLAN_ID.getName(), paidSubscriptionPlan.getId())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }
}
