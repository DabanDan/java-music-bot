package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicBot;

import java.sql.SQLException;
import java.util.Optional;

public class PrefixCommand extends Command {
    public PrefixCommand(MusicBot bot) {
        super(bot, "prefix", "trigger");
        setDescription("Sets the command prefix");
    }

    @Override
    public void on(Context context) {
        if (!this.bot.getConfigs().config.patreon) {
            context.reply("**The prefix command is dabBot premium only!**" +
                    "\nDonate for the `Custom Prefix` tier on Patreon at https://patreon.com/dabbot to gain access.");
            return;
        }

        MessageReceivedEvent event = context.getEvent();
        Guild guild = event.getGuild();

        // todo prefix change role check
        // user is not a super supporter & there is not a super supporter with admin on the server
        if (!this.bot.getPermissionReader().allowedSuperSupporterPatronAccess(event.getAuthor())
                && !this.bot.getPermissionReader().allowedSuperSupporterPatronAccess(guild)) {
            context.reply("**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on Patreon at https://patreon.com/dabbot to gain access.");
            return;
        }

        bot.getDatabase().ifPresent(database -> {
            if (context.getArgs().length == 0) {
                try {
                    database.settingsSelectGuildSettings(guild.getIdLong()).thenAccept(settings -> {
                        if (!settings.isPresent()) {
                            context.reply("Error getting guild settings :(");
                            return;
                        }

                        context.reply("Current prefix: `%s`", Optional.ofNullable(settings.get().getPrefix()).orElse(bot.getConfigs().config.prefix)); // todo
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return;
            }

            try {
                database.settingsUpsertGuildSettings(guild.getIdLong(), context.getArgs()[0]).thenAccept(success -> {
                    if (success) {
                        context.reply("Successfully updated command prefix!");
                    } else {
                        context.reply("Error updating command prefix!");
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
