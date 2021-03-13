package ru.gadjini.telegram.smart.payment.service.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.payment.bot.command.api.BotCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

    private Map<String, BotCommand> botCommands = new HashMap<>();

    private CommandParser commandParser;

    @Autowired
    public CommandExecutor(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    @Autowired
    public void setBotCommands(Set<BotCommand> commands) {
        commands.forEach(botCommand -> botCommands.put(botCommand.getCommandName(), botCommand));
    }

    public boolean isBotCommand(Message message) {
        return message.isCommand();
    }

    public boolean executeBotCommand(Message message) {
        CommandParser.CommandParseResult commandParseResult = commandParser.parseBotCommand(message);
        BotCommand botCommand = botCommands.get(commandParseResult.getCommandName());

        if (botCommand != null) {
            LOGGER.debug("Bot({}, {})", message.getFrom().getId(), botCommand.getClass().getSimpleName());

            if (botCommand.accept(message)) {
                botCommand.processMessage(message, commandParseResult.getParameters());

                return true;
            }
        }

        return false;
    }
}
