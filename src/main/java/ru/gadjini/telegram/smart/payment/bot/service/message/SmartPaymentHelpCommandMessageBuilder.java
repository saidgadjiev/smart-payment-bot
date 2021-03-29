package ru.gadjini.telegram.smart.payment.bot.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.HelpCommandMessageBuilder;

import java.util.Locale;

@Component
public class SmartPaymentHelpCommandMessageBuilder implements HelpCommandMessageBuilder {

    private LocalisationService localisationService;

    private CommandMessageBuilder commandMessageBuilder;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public SmartPaymentHelpCommandMessageBuilder(LocalisationService localisationService,
                                                 CommandMessageBuilder commandMessageBuilder,
                                                 SubscriptionProperties subscriptionProperties) {
        this.localisationService = localisationService;
        this.commandMessageBuilder = commandMessageBuilder;
        this.subscriptionProperties = subscriptionProperties;
    }

    @Override
    public String getWelcomeMessage(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_HELP,
                new Object[]{subscriptionProperties.getPaidBotName(), commandMessageBuilder.getCommandsInfo(locale)},
                locale);
    }
}
