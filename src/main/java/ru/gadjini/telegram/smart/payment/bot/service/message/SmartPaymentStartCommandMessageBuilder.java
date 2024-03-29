package ru.gadjini.telegram.smart.payment.bot.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.StartCommandMessageBuilder;

import java.util.Locale;

@Component
public class SmartPaymentStartCommandMessageBuilder implements StartCommandMessageBuilder {

    private LocalisationService localisationService;

    private CommandMessageBuilder commandMessageBuilder;

    @Autowired
    public SmartPaymentStartCommandMessageBuilder(LocalisationService localisationService,
                                                  CommandMessageBuilder commandMessageBuilder) {
        this.localisationService = localisationService;
        this.commandMessageBuilder = commandMessageBuilder;
    }

    @Override
    public String getWelcomeMessage(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_WELCOME,
                new Object[]{commandMessageBuilder.getCommandsInfo(locale)},
                locale);
    }
}
