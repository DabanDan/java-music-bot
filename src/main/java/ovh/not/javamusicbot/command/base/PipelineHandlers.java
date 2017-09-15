package ovh.not.javamusicbot.command.base;

import me.bramhaag.owo.OwO;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static ovh.not.javamusicbot.MusicBot.JSON_MEDIA_TYPE;
import static ovh.not.javamusicbot.Utils.HASTEBIN_URL;

public class PipelineHandlers {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineHandlers.class);

    public static Function<CommandContext, Boolean> argumentCheckHandler(String message, int... requirements) {
        int min = requirements[0];
        int max = requirements.length > 1 ? requirements[0] : -1;

        return context -> {
            int args = context.getArgs().size();

            if (context.getArgs().size() < min) {
                context.reply(message);
                return false;
            }

            if (max != -1 && args > max) {
                context.reply(message);
                return false;
            }

            return true;
        };
    }

    public static Function<CommandContext, Boolean> requiresMusicHandler() {
        return context -> {
            MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

            if (!musicManager.isPlayingMusic()) {
                context.reply("No music is playing on this guild! This command requires a song to be playing. " +
                        "To play a song use `{{prefix}}play`");
                return false;
            } else {
                return true;
            }
        };
    }

    public static Function<CommandContext, Boolean> requiresUserInVoiceChannelHandler() {
        return context -> {
            VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
            if (channel == null) {
                context.reply("You must be in a voice channel to use this command!");
                return false;
            }
            return true;
        };
    }

    public static Function<CommandContext, Boolean> requiresNotInUseHandler() {
        return context -> {
            MessageReceivedEvent event = context.getEvent();
            MusicManager musicManager = GuildManager.getInstance().getMusicManager(event.getGuild());

            if (Utils.isBotInUse(musicManager, event.getMember())) {
                context.reply("dabBot is already playing music in %s so it cannot be moved. Members with the " +
                        "`Move Members` permission can do this.", musicManager.getVoiceChannel().get().getName());
                return false;
            } else {
                return true;
            }
        };
    }

    public static Function<CommandContext, Boolean> patronOnlyCommandHandler() {
        return context -> {
            if (!MusicBot.getConfigs().config.patreon) {
                context.reply("**The volume command is dabBot premium only!**\nDonate for the `Super supporter` " +
                        "tier on Patreon at https://patreon.com/dabbot to gain access.");
                return false;
            } else {
                return true;
            }
        };
    }

    public static Function<CommandContext, Boolean> requireSuperSupporterHandler() {
        return context -> {
            if (!Utils.allowedSuperSupporterPatronAccess(context.getEvent().getGuild())) {
                context.reply("**The volume command is dabBot premium only!**\nDonate for the `Super supporter` tier" +
                        " on Patreon at https://patreon.com/dabbot to gain access.");
                return false;
            } else {
                return true;
            }
        };
    }

    private static final OwO OWO = new OwO.Builder()
            .setKey(MusicBot.getConfigs().config.owoKey)
            .setUploadUrl("https://paste.dabbot.org")
            .setShortenUrl("https://paste.dabbot.org")
            .build();

    public static BiFunction<CommandContext, Object, Boolean> textUploadTransformer(BiConsumer<CommandContext, Optional<String>> callback) {
        return (context, result) -> {
            if (result == null) return false;

            OWO.upload(result.toString(), "text/plain").execute(file -> {
                callback.accept(context, Optional.of(file.getFullUrl()));
            }, throwable -> {
                LOGGER.error("error uploading to owo", throwable);

                RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, result.toString());

                Request request = new Request.Builder()
                        .url(HASTEBIN_URL)
                        .method("POST", body)
                        .build();

                MusicBot.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@Nonnull Call call, @Nonnull IOException e) {
                        LOGGER.error("error occurred posting to hastebin.com", e);
                        callback.accept(context, Optional.empty());
                    }

                    @Override
                    public void onResponse(@Nonnull Call call, @Nonnull Response response) throws IOException {
                        callback.accept(context, Optional.of(String.format("https://hastebin.com/%s.json",
                                new JSONObject(response.body().string()).getString("key"))));
                        response.close();
                    }
                });
            });

            return false;
        };
    }
}
