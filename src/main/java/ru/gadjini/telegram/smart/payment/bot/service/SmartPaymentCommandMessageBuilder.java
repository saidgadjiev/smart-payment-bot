package ru.gadjini.telegram.smart.payment.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentMessagesProperties;

import java.util.Locale;

@Service
public class SmartPaymentCommandMessageBuilder implements CommandMessageBuilder {

    private LocalisationService localisationService;

    @Autowired
    public SmartPaymentCommandMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public String getCommandsInfo(Locale locale) {
        return CommandParser.COMMAND_START_CHAR + CommandNames.START_COMMAND_NAME +
                " - " + localisationService.getMessage(MessagesProperties.START_COMMAND_DESCRIPTION, locale) +
                "\n" + CommandParser.COMMAND_START_CHAR + SmartPaymentCommandNames.BUY +
                " - " + localisationService.getMessage(SmartPaymentMessagesProperties.BUY_COMMAND_DESCRIPTION, locale) +
                "\n" + CommandParser.COMMAND_START_CHAR + CommandNames.SUBSCRIPTION +
                " - " + localisationService.getMessage(MessagesProperties.SUBSCRIPTION_COMMAND_DESCRIPTION, locale) +
                "\n" + CommandParser.COMMAND_START_CHAR + CommandNames.HELP_COMMAND +
                " - " + localisationService.getMessage(MessagesProperties.HELP_COMMAND_DESCRIPTION, locale);
    }
}
