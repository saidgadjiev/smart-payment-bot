package ru.gadjini.telegram.smart.payment.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.payment.bot.common.SmartPaymentCommandNames;

import java.util.List;

@Component
public class BuySubscriptionCommand implements BotCommand {

    private MessageService messageService;

    @Autowired
    public BuySubscriptionCommand(@TgMessageLimitsControl MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void processMessage(Message message, String[] strings) {
        SendInvoice invoice = SendInvoice.builder()
                .chatId(message.getChatId().intValue())
                .title("Test payment")
                .description("Test description")
                .providerToken("410694247:TEST:408cc90e-94be-4f55-bdf7-c105e578dc7b")
                .currency("UAH")
                .prices(List.of(new LabeledPrice("Pay", 7300)))
                .startParameter("smart-payment")
                .payload("test-user")
                .build();

        messageService.sendInvoice(invoice);
    }

    @Override
    public String getCommandIdentifier() {
        return SmartPaymentCommandNames.BUY_COMMAND;
    }
}
