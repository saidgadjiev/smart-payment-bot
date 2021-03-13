package ru.gadjini.telegram.smart.payment.service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.payment.exception.FloodWaitException;
import ru.gadjini.telegram.smart.payment.exception.TelegramApiException;
import ru.gadjini.telegram.smart.payment.exception.TelegramApiRequestException;
import ru.gadjini.telegram.smart.payment.property.BotProperties;

import java.util.Set;

@Service
public class TelegramBotApiService extends DefaultAbsSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotApiService.class);

    private BotProperties botProperties;

    private ObjectMapper objectMapper;

    @Autowired
    public TelegramBotApiService(BotProperties botProperties, ObjectMapper objectMapper, DefaultBotOptions botOptions) {
        super(botOptions);
        this.botProperties = botProperties;
        this.objectMapper = objectMapper;
    }

    public Boolean isChatMember(GetChatMember isChatMember) {
        return executeWithResult(null, () -> {
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(isChatMember.getChatId());
            getChatMember.setUserId(isChatMember.getUserId());

            ChatMember member = execute(getChatMember);

            return isInGroup(member.getStatus());
        });
    }

    public Boolean sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        return executeWithResult(null, () -> {
            return execute(objectMapper.convertValue(answerCallbackQuery, AnswerCallbackQuery.class));
        });
    }

    public Message sendMessage(SendMessage sendMessage) {
        return executeWithResult(sendMessage.getChatId(), () -> {
            Message execute = execute(objectMapper.convertValue(sendMessage, SendMessage.class));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    public void editReplyMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        executeWithoutResult(editMessageReplyMarkup.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageReplyMarkup, EditMessageReplyMarkup.class));
        });
    }

    public void editMessageText(EditMessageText editMessageText) {
        executeWithoutResult(editMessageText.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageText, EditMessageText.class));
        });
    }

    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        executeWithoutResult(editMessageCaption.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageCaption, EditMessageCaption.class));
        });
    }

    public Boolean deleteMessage(DeleteMessage deleteMessage) {
        return executeWithResult(deleteMessage.getChatId(), () -> {
            return execute(objectMapper.convertValue(deleteMessage, DeleteMessage.class));
        });
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    private boolean isInGroup(String status) {
        if (StringUtils.isBlank(status)) {
            return true;
        }
        return Set.of("creator", "administrator", "member", "restricted").contains(status);
    }

    private void executeWithoutResult(String chatId, Executable executable) {
        try {
            executable.executeWithException();
        } catch (Exception e) {
            throw catchException(chatId, e);
        }
    }

    private <V> V executeWithResult(String chatId, Callable<V> executable) {
        try {
            return executable.executeWithResult();
        } catch (Exception e) {
            throw catchException(chatId, e);
        }
    }

    private RuntimeException catchException(String chatId, Exception ex) {
        if (ex instanceof org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException) {
            org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException e = (org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException) ex;
            LOGGER.error("(" + chatId + ")" + e.getMessage() + "\n" + e.getErrorCode() + "\n" + e.getApiResponse(), e);
            if (e.getErrorCode() == 429) {
                return new FloodWaitException(e.getApiResponse(), 30);
            }
            return new TelegramApiRequestException(chatId, e.getMessage(), e.getErrorCode(), e.getApiResponse(), e);
        } else {
            LOGGER.error("(" + chatId + ")" + ex.getMessage(), ex);
            return new TelegramApiException(ex);
        }
    }

    private interface Executable {

        void executeWithException() throws Exception;
    }

    private interface Callable<V> {

        V executeWithResult() throws Exception;
    }
}
