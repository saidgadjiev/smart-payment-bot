package ru.gadjini.telegram.smart.payment.bot.service.message;

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
        StringBuilder builder = new StringBuilder();

        builder.append(CommandParser.COMMAND_START_CHAR).append(CommandNames.START_COMMAND_NAME).append(" - ").append(localisationService.getMessage(MessagesProperties.START_COMMAND_DESCRIPTION, locale)).append("\n");
        builder.append(CommandParser.COMMAND_START_CHAR).append(SmartPaymentCommandNames.BUY).append(" - ").append(localisationService.getMessage(SmartPaymentMessagesProperties.BUY_COMMAND_DESCRIPTION, locale)).append("\n");
        builder.append(CommandParser.COMMAND_START_CHAR).append(CommandNames.SUBSCRIPTION).append(" - ").append(localisationService.getMessage(MessagesProperties.SUBSCRIPTION_COMMAND_DESCRIPTION, locale)).append("\n");
        builder.append(CommandParser.COMMAND_START_CHAR).append(CommandNames.REFRESH_SUBSCRIPTION).append(" - ").append(localisationService.getMessage(MessagesProperties.REFRESH_SUBSCRIPTION_COMMAND_DESCRIPTION, locale)).append("\n");
        builder.append(CommandParser.COMMAND_START_CHAR).append(CommandNames.TIME_COMMAND).append(" - ").append(localisationService.getMessage(MessagesProperties.BOT_TIME_COMMAND_DESCRIPTION, locale)).append("\n");
        builder.append(CommandParser.COMMAND_START_CHAR).append(CommandNames.LANGUAGE_COMMAND_NAME).append(" - ").append(localisationService.getMessage(MessagesProperties.LANGUAGE_COMMAND_DESCRIPTION, locale)).append("\n");
        builder.append(CommandParser.COMMAND_START_CHAR).append(CommandNames.HELP_COMMAND).append(" - ").append(localisationService.getMessage(MessagesProperties.HELP_COMMAND_DESCRIPTION, locale));

        return builder.toString();
    }
}
