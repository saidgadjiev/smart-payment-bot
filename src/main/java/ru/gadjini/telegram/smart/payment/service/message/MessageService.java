package ru.gadjini.telegram.smart.payment.service.message;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.payment.common.MessagesProperties;
import ru.gadjini.telegram.smart.payment.exception.TelegramApiRequestException;
import ru.gadjini.telegram.smart.payment.service.LocalisationService;
import ru.gadjini.telegram.smart.payment.service.telegram.TelegramBotApiService;

import java.util.Locale;
import java.util.function.Consumer;

@Service
@SuppressWarnings("PMD")
public class MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private LocalisationService localisationService;

    private TelegramBotApiService telegramService;

    @Autowired
    public MessageService(LocalisationService localisationService,
                          TelegramBotApiService telegramService) {
        this.localisationService = localisationService;
        this.telegramService = telegramService;
    }

    public void sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        try {
            telegramService.sendAnswerCallbackQuery(answerCallbackQuery);
        } catch (Exception ignore) {
        }
    }

    public boolean isChatMember(String chatId, int userId) {
        GetChatMember isChatMember = new GetChatMember();
        isChatMember.setChatId(chatId);
        isChatMember.setUserId(userId);

        try {
            return BooleanUtils.toBoolean(telegramService.isChatMember(isChatMember));
        } catch (Exception ex) {
            return false;
        }
    }

    public void sendMessage(SendMessage sendMessage) {
        sendMessage(sendMessage, null);
    }

    public void sendMessage(SendMessage sendMessage, Consumer<Message> callback) {
        sendMessage0(sendMessage, callback);
    }

    public void removeInlineKeyboard(long chatId, int messageId) {
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(messageId);

        try {
            telegramService.editReplyMarkup(edit);
        } catch (Exception ignore) {
        }
    }

    public void editMessage(EditMessageText editMessageText, boolean ignoreException) {
        editMessageText.setParseMode(ParseMode.HTML);

        try {
            telegramService.editMessageText(editMessageText);
        } catch (Exception ex) {
            if (!ignoreException) {
                throw ex;
            }
        }
    }

    public void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup, boolean ignoreException) {
        try {
            telegramService.editReplyMarkup(editMessageReplyMarkup);
        } catch (Exception ex) {
            if (!ignoreException) {
                throw ex;
            }
        }
    }

    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        editMessageCaption.setParseMode(ParseMode.HTML);

        telegramService.editMessageCaption(editMessageCaption);
    }

    public void deleteMessage(long chatId, int messageId) {
        try {
            telegramService.deleteMessage(new DeleteMessage(String.valueOf(chatId), messageId));
        } catch (Exception ignore) {
        }
    }

    public void sendErrorMessage(long chatId, Locale locale) {
        sendMessage(SendMessage.builder().chatId(String.valueOf(chatId))
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_ERROR, locale))
                .parseMode(ParseMode.HTML).build());
    }

    public void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale) {
        sendMessage(SendMessage.builder().chatId(String.valueOf(chatId)).text(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_RESTARTED, locale))
                .parseMode(ParseMode.HTML)
                .replyMarkup(replyKeyboard).build());
    }

    private void sendMessage0(SendMessage sendMessage, Consumer<Message> callback) {
        try {
            sendMessage.disableWebPagePreview();
            sendMessage.setAllowSendingWithoutReply(true);
            Message message = telegramService.sendMessage(sendMessage);

            if (callback != null) {
                callback.accept(message);
            }
        } catch (TelegramApiRequestException ex) {
            LOGGER.error("Error send message({})", sendMessage);
            throw ex;
        }
    }
}
