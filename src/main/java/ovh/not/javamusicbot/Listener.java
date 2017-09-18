package ovh.not.javamusicbot;

import lavalink.client.io.Lavalink;
import lavalink.client.io.VoiceServerUpdateInterceptor;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ovh.not.javamusicbot.MusicBot.JSON_MEDIA_TYPE;

class Listener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    private static final String CARBON_DATA_URL = "https://www.carbonitex.net/discord/data/botdata.php";
    private static final String DBOTS_STATS_URL = "https://bots.discord.pw/api/bots/%s/stats";
    private static final String DBOTS_ORG_STATS_URL = "https://discordbots.org/api/bots/%s/stats";

    private final Pattern commandPattern = Pattern.compile(MusicBot.getConfigs().config.regex);
    private final CommandManager commandManager = new CommandManager();

    private Optional<Lavalink> lavalink = Optional.empty();

    Listener() {

    }

    @Override
    public void onReady(ReadyEvent event) {
        Config config = MusicBot.getConfigs().config;
        ShardManager shardManager = event.getJDA().asBot().getShardManager();

        Lavalink lavalink = new Lavalink(config.lavalinkUserId, shardManager.getShardsTotal(), shardManager::getShard);

        setLavalink(lavalink);
        GuildManager.getInstance().setLavalink(lavalink);

        for (Map.Entry<String, String> entry : config.lavalinkNodes.entrySet()) {
            try {
                lavalink.addNode(new URI(entry.getKey().substring(1, entry.getKey().length() - 1)), entry.getValue());
            } catch (URISyntaxException e) {
                logger.error("error parsing lavalink node server uri", e);
                return;
            }
        }

        try {
            Constructor<VoiceServerUpdateInterceptor> constructor = VoiceServerUpdateInterceptor.class.getDeclaredConstructor(Lavalink.class, JDAImpl.class);
            constructor.setAccessible(true);
            VoiceServerUpdateInterceptor interceptor = constructor.newInstance(lavalink, event.getJDA());
            ((JDAImpl) event.getJDA()).getClient().getHandlers().put("VOICE_SERVER_UPDATE", interceptor);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }

        System.out.println("READY! " + ((JDAImpl) event.getJDA()).getClient().getHandlers().get("VOICE_SERVER_UPDATE").getClass().getName());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!lavalink.isPresent()) {
            logger.info("received message before lavalink ready");
            return;
        }

        User author = event.getAuthor();
        if (author.isBot() || author.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
            return;
        }

        String content = event.getMessage().getContent();
        Matcher matcher = commandPattern.matcher(content.replace("\r", " ").replace("\n", " "));
        if (!matcher.find()) {
            return;
        }

        if (!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
            return;
        }

        String name = matcher.group(1).toLowerCase();
        Command command = commandManager.getCommand(name);
        if (command == null) {
            return;
        }

        Command.Context context = command.new Context();
        context.setEvent(event);
        if (matcher.groupCount() > 1) {
            String[] matches = matcher.group(2).split("\\s+");
            if (matches.length > 0 && matches[0].equals("")) {
                matches = new String[0];
            }
            context.setArgs(matches);
        }

        command.on(context);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        JDA jda = event.getJDA();
        Guild guild = event.getGuild();
        TextChannel defaultChannel = guild.getDefaultChannel();

        int guilds = jda.getGuilds().size();
        logger.info("Joined guild: {}", guild.getName());

        Config config = MusicBot.getConfigs().config;

        if (defaultChannel != null && defaultChannel.canTalk()) {
            defaultChannel.sendMessage(config.join).complete();
        }

        if (config.patreon) {
            if (Utils.allowedSupporterPatronAccess(guild)) {
                return;
            }

            if (defaultChannel != null && defaultChannel.canTalk()) {
                try {
                    event.getGuild().getDefaultChannel().sendMessage("**Sorry, this is the patreon only dabBot!**\nTo have this " +
                            "bot on your server, you must become a patreon at https://patreon.com/dabbot").complete();
                } catch (Exception ignored) {
                }
            }
            event.getGuild().leave().queue();
            return;
        }

        if (config.dev) {
            return;
        }

        JDA.ShardInfo shardInfo = event.getJDA().getShardInfo();
        int shardCount = shardInfo.getShardTotal();
        int shardId = shardInfo.getShardId();

        if (config.carbon != null && !config.carbon.isEmpty()) {
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, new JSONObject()
                    .put("key", config.carbon)
                    .put("servercount", guilds)
                    .put("shardcount", shardCount)
                    .put("shardid", shardId)
                    .toString());

            Request request = new Request.Builder()
                    .url(CARBON_DATA_URL)
                    .method("POST", body)
                    .build();

            try {
                MusicBot.HTTP_CLIENT.newCall(request).execute().close();
            } catch (IOException e) {
                logger.error("Error posting stats to carbonitex.net", e);
            }
        }

        if (config.dbots != null && !config.dbots.isEmpty()) {
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, new JSONObject()
                    .put("server_count", guilds)
                    .put("shard_count", shardCount)
                    .put("shard_id", shardId)
                    .toString());

            Request request = new Request.Builder()
                    .url(String.format(DBOTS_STATS_URL, event.getJDA().getSelfUser().getId()))
                    .method("POST", body)
                    .addHeader("Authorization", config.dbots)
                    .build();

            try {
                MusicBot.HTTP_CLIENT.newCall(request).execute().close();
            } catch (IOException e) {
                logger.error("Error posting stats to bots.discord.pw", e);
            }
        }

        if (config.dbotsOrg != null && !config.dbotsOrg.isEmpty()) {
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, new JSONObject()
                    .put("server_count", guilds)
                    .put("shard_count", shardCount)
                    .put("shard_id", shardId)
                    .toString());

            Request request = new Request.Builder()
                    .url(String.format(DBOTS_ORG_STATS_URL, event.getJDA().getSelfUser().getId()))
                    .method("POST", body)
                    .addHeader("Authorization", config.dbotsOrg)
                    .build();

            try {
                MusicBot.HTTP_CLIENT.newCall(request).execute().close();
            } catch (IOException e) {
                logger.error("Error posting stats to discordbots.org", e);
            }
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();

        // get the guild's music manager
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(guild);

        if (musicManager != null) {
            // clear the song queue, stop the current track
            musicManager.getTrackScheduler().getQueue().clear();
            musicManager.getPlayer().stopTrack();

            // close the voice connection
            musicManager.close();
        }
    }

    void setLavalink(Lavalink lavalink) {
        this.lavalink = Optional.of(lavalink);
    }
}
