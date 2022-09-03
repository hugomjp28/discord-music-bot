import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class SlashCommandBuilder {
    /**
     * Adds the supported slash commands to the JDA instance
     * @param jda JDA instance
     */
    public static void buildCommands(JDA jda) {
        jda.updateCommands().addCommands(
                Commands.slash("ping", "Replies with 'PONG!' if the bot is online"),
                Commands.slash("play", "Plays a song from Youtube, Soundcloud or Spotify")
                        .addOption(OptionType.STRING,"song","Text for Youtube search or the link to the song")
                //...

        ).queue();
    }
}
