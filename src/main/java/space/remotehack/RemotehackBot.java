package space.remotehack;

import org.jetbrains.annotations.NotNull;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class RemotehackBot extends AbilityBot {
    public static final String BOT_NAME = "RemotehackBot";
    public static final String BOT_TOKEN_ENV_VAR = "REMOTEHACK_TELEGRAM_BOT_TOKEN";
    public static final String BOT_TOKEN = System.getenv(BOT_TOKEN_ENV_VAR);
    public static final Integer CREATOR_ID = Integer.valueOf(System.getenv("REMOTEHACK_TELEGRAM_BOT_CREATOR_ID"));

    public RemotehackBot() {
        super(BOT_TOKEN, BOT_NAME);
    }

    @Override
    public int creatorId() {
        return CREATOR_ID;
    }

    public Ability start() {
        return Ability
                .builder()
                .name("start")
                .info("We're going on an adventure")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> silent.send("Hello world!", context.chatId()))
                .build();
    }

    public ReplyFlow directionFlow() {
        Reply saidLeft = Reply.of(upd -> silent.send("Sir, I have gone left.", getChatId(upd)),
                hasMessageWith("go left or else"));

        ReplyFlow leftflow = ReplyFlow.builder(db)
                .action(upd -> silent.send("I don't know how to go left.", getChatId(upd)))
                .onlyIf(hasMessageWith("left"))
                .next(saidLeft).build();

        Reply saidRight = Reply.of(upd -> silent.send("Sir, I have gone right.", getChatId(upd)),
                hasMessageWith("right"));

        return ReplyFlow.builder(db)
                .action(upd -> silent.send("Command me to go left or right!", getChatId(upd)))
                .onlyIf(hasMessageWith("wake up"))
                .next(leftflow)
                .next(saidRight)
                .build();
    }

    @NotNull
    private Predicate<Update> hasMessageWith(String msg) {
        return upd -> upd.getMessage().getText().equalsIgnoreCase(msg);
    }
}