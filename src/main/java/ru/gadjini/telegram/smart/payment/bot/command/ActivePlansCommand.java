package ru.gadjini.telegram.smart.payment.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;

import java.util.List;
import java.util.Locale;

@Component
public class ActivePlansCommand implements BotCommand {

    private MessageService messageService;

    private UserService userService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private SubscriptionTimeDeclensionProvider timeDeclensionProvider;

    @Autowired
    public ActivePlansCommand(@TgMessageLimitsControl MessageService messageService,
                              UserService userService, PaidSubscriptionPlanService paidSubscriptionPlanService,
                              SubscriptionTimeDeclensionProvider timeDeclensionProvider) {
        this.messageService = messageService;
        this.userService = userService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.timeDeclensionProvider = timeDeclensionProvider;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        List<PaidSubscriptionPlan> paidSubscriptionPlans = paidSubscriptionPlanService.getActivePlans();
        StringBuilder text = new StringBuilder();
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        for (PaidSubscriptionPlan paidSubscriptionPlan : paidSubscriptionPlans) {
            if (text.length() > 0) {
                text.append("\n");
            }
            text.append("ID - ").append(paidSubscriptionPlan.getId()).append("\n");
            text.append("Period - ").append(timeDeclensionProvider.getService(locale.getLanguage())
                    .months(paidSubscriptionPlan.getPeriod().getMonths())).append("\n");
            text.append("Cost - ").append(paidSubscriptionPlan.getPrice()).append(" USD").append("\n");
        }

        messageService.sendMessage(SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(text.toString())
                .build());
    }

    @Override
    public String getCommandIdentifier() {
        return SmartPaymentCommandNames.ACTIVE_PLANS_COMMAND;
    }
}
