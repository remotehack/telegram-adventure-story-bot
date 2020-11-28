package space.remotehack;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class RemotehackBot extends TelegramLongPollingBot {
    public static final String BOT_NAME = "RemotehackBot";
    public static final String BOT_TOKEN_ENV_VAR = "REMOTEHACK_TELEGRAM_BOT_TOKEN";
    public static final String BOT_TOKEN = System.getenv(BOT_TOKEN_ENV_VAR);

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println(update.getMessage().getText());
            SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
            message.setChatId(String.valueOf(update.getMessage().getChatId()));
            message.setText(update.getMessage().getText());

            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No message text");
        }
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }


}