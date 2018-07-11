package net.chmilevfa.telegram.bots.currency;

import net.chmilevfa.telegram.BotConfig;
import net.chmilevfa.telegram.bots.currency.dao.file.JsonFileDao;
import net.chmilevfa.telegram.bots.currency.service.StringService;
import net.chmilevfa.telegram.bots.currency.state.*;
import net.chmilevfa.telegram.bots.currency.state.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import static net.chmilevfa.telegram.bots.currency.service.StringService.GO_TO_MAIN_MENU;

/**
 * Implements currency bot behaviour.
 * Delegates handling particular behaviour of every possible bot
 * state {@link MessageState} to implementations of {@link StateHandler}.
 *
 * @see MessageState
 * @see StateHandler
 *
 * @author chmilevfa
 * @since 08.07.18
 */
@Service("currencyBot")
public class CurrencyBot extends TelegramLongPollingBot {

    /** Handlers for all possible bot's states */
    private final StateHandler defaultStateHandler;
    private final StateHandler mainMenuStateHandler;
    private final StateHandler firstCurrencyHandler;
    private final StateHandler secondCurrencyHandler;
    private final StateHandler settingsStateHandler;
    private final StateHandler feedbackStateHandler;

    private final JsonFileDao dao;

    @Autowired
    public CurrencyBot(
            StateHandler defaultStateHandler,
            StateHandler mainMenuStateHandler,
            StateHandler firstCurrencyHandler,
            StateHandler secondCurrencyHandler,
            StateHandler settingsStateHandler,
            StateHandler feedbackStateHandler,
            JsonFileDao dao) {
        this.defaultStateHandler = defaultStateHandler;
        this.mainMenuStateHandler = mainMenuStateHandler;
        this.firstCurrencyHandler = firstCurrencyHandler;
        this.secondCurrencyHandler = secondCurrencyHandler;
        this.settingsStateHandler = settingsStateHandler;
        this.feedbackStateHandler = feedbackStateHandler;
        this.dao = dao;
    }

    @Override
    public String getBotUsername() {
        return BotConfig.CURRENCY_BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.CURRENCY_BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            try {
                handleIncomingMessage(update.getMessage()); // Call method to send the message
            } catch (TelegramApiException e) {
                //TODO logger
                e.printStackTrace();
            }
        }
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        MessageState messageState = getMessageState(message.getFrom().getId(), message.getChatId());

        SendMessage sendMessageRequest;
        switch (messageState) {
            case MAIN_MENU:
                sendMessageRequest = mainMenuStateHandler.getMessageToSend(message);
                break;
            case CHOOSE_CURRENT_RATE_FIRST:
                sendMessageRequest = firstCurrencyHandler.getMessageToSend(message);
                break;
            case CHOOSE_CURRENT_RATE_SECOND:
                sendMessageRequest = secondCurrencyHandler.getMessageToSend(message);
                break;
            case FEEDBACK:
                sendMessageRequest = handleFeedback(message);
                break;
            case SETTINGS:
            case DEFAULT:
            default:
                sendMessageRequest = defaultStateHandler.getMessageToSend(message);
        }
        execute(sendMessageRequest);
    }

    private SendMessage handleFeedback(Message message) throws TelegramApiException {
        dao.saveMessageState(message.getFrom().getId(), message.getChatId(), MessageState.MAIN_MENU);

        SendMessage sendMessageRequest;
        if (message.hasText()) {
            switch (message.getText()) {
                case GO_TO_MAIN_MENU:
                    sendMessageRequest = defaultStateHandler.getMessageToSend(message);
                    break;
                default:
                    sendMessageRequest = feedbackStateHandler.getMessageToSend(message);
                    sendFeedbackToDeveloper(message);
            }
        } else {
            sendMessageRequest = feedbackStateHandler.getMessageToSend(message);
            sendFeedbackToDeveloper(message);
        }
        return sendMessageRequest;
    }

    private void sendFeedbackToDeveloper(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId((long) BotConfig.MASTER_ID);
        sendMessage.setText(
                String.format(
                        StringService.FEEDBACK_FOR_DEVELOPER,
                        message.getFrom().getUserName()
                ) + message.getText());
        execute(sendMessage);
    }

    private MessageState getMessageState(Integer userId, Long chatId) {
        return dao.getState(userId, chatId);
    }
}
