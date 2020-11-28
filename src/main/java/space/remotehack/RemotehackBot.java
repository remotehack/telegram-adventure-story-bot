package space.remotehack;

import org.jetbrains.annotations.NotNull;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class RemotehackBot extends AbilityBot {
    public static final String BOT_NAME = "RemotehackBot";
    public static final String BOT_TOKEN_ENV_VAR = "REMOTEHACK_TELEGRAM_BOT_TOKEN";
    public static final String BOT_TOKEN = System.getenv(BOT_TOKEN_ENV_VAR);
    public static final Integer CREATOR_ID;

    static {
        String creatorIdString = System.getenv("REMOTEHACK_TELEGRAM_BOT_CREATOR_ID");
        CREATOR_ID = creatorIdString != null ? Integer.parseInt(creatorIdString) : 0;
    }

    public RemotehackBot() {
        super(BOT_TOKEN, BOT_NAME);
    }

    public RemotehackBot(DBContext db) {
        super(BOT_TOKEN, BOT_NAME, db);
    }

    @Override
    public int creatorId() {
        return CREATOR_ID;
    }

    /**
     * This is needed for our tests, to provide a mocked SilentSender
     *
     * @param silentSender
     */
    public void setSilentSender(SilentSender silentSender) {
        silent = silentSender;
    }

    public Ability start() {
        return Ability
                .builder()
                .name("start")
                .info("We're going on an adventure")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> sendMessage(String.valueOf(context.chatId()), "You're asleep, what do you want to do?",
                        new String[]{"Wake up", "Sleep some more!"}))
                .build();
    }

    public ReplyFlow directionFlow() {
        String callBenCmd = "Call Ben";
        String checkBarnCmd = "Check the barn";
        String chickensCmd = "Go look at some chickens";
        String[] options = new String[]{
                callBenCmd,
                checkBarnCmd,
                chickensCmd
        };

        Reply checkBarn = Reply.of(upd -> {
                    SendPhoto sendPhotoRequest = new SendPhoto();
                    // Set destination chat id
                    sendPhotoRequest.setChatId(String.valueOf(getChatId(upd)));
                    // Set the photo url as a simple photo
                    sendPhotoRequest.setPhoto(new InputFile("https://i.insider.com/57ffa3b952dd7340018b48fa?width=1000&format=jpeg&auto=webp"));
                    try {
                        // Execute the method
                        execute(sendPhotoRequest);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    silent.send("Your found your car. You can go back home now", getChatId(upd));
                },
                hasMessageWith(checkBarnCmd));

        // todo: replace video with a gif file to make it embedded in telegram
        ReplyFlow seeChickensReply = ReplyFlow.builder(db)
                .action(upd -> sendMessage(
                        String.valueOf(getChatId(upd)), "https://www.youtube.com/watch?v=F-X4SLhorvw",
                        new String[]{checkBarnCmd}
                ))
                .onlyIf(hasMessageWith(chickensCmd))
                .next(checkBarn)
                .build();

        ReplyFlow callBen = ReplyFlow.builder(db)
                .action(upd -> sendMessage(
                        String.valueOf(getChatId(upd)), "Ben didn't answer",
                        Arrays.stream(options).filter(o -> !callBenCmd.equals(o)).toArray(String[]::new)
                ))
                .onlyIf(hasMessageWith(callBenCmd))
                .next(seeChickensReply)
                .next(checkBarn)
                .build();


        return ReplyFlow.builder(db)
                .action(upd -> sendMessage(String.valueOf(getChatId(upd)),
                        "You wake up on a farm. In a stable condition. You don't know why you're there, so you go out to your car. It's gone. What do you do?",
                        options))
                .onlyIf(hasMessageWith("Wake up"))
                .next(callBen)
                .next(seeChickensReply)
                .next(checkBarn)
                .build();
    }

    @NotNull
    private Predicate<Update> hasMessageWith(String msg) {
        return upd -> upd.getMessage().getText().equalsIgnoreCase(msg);
    }

    private void sendMessage(String chatId, String messageText, String[] keyboardButtons) {
        SendMessage message = constructMessageFrom(String.valueOf(chatId),
                messageText, keyboardButtons);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage constructMessageFrom(String chatId, String messageText, String[] keyboardButtons) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String buttonText : keyboardButtons) {
            KeyboardRow row = new KeyboardRow();
            row.add(buttonText);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);

        // Add it to the message
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }
}