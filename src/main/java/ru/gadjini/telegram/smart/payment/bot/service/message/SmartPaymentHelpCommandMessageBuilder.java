package ru.gadjini.telegram.smart.payment.bot.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.HelpCommandMessageBuilder;

import java.util.Locale;

@Component
public class SmartPaymentHelpCommandMessageBuilder implements HelpCommandMessageBuilder {

    private LocalisationService localisationService;

    private CommandMessageBuilder commandMessageBuilder;

    @Autowired
    public SmartPaymentHelpCommandMessageBuilder(LocalisationService localisationService,
                                                 CommandMessageBuilder commandMessageBuilder) {
        this.localisationService = localisationService;
        this.commandMessageBuilder = commandMessageBuilder;
    }

    @Override
    public String getWelcomeMessage(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_HELP,
                new Object[]{commandMessageBuilder.getCommandsInfo(locale)},
                locale);
    }
}
