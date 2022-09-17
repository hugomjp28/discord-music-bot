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
                Commands.slash("ping", "Replies with 'PONG!' if the bot is online."),
                Commands.slash("play", "Plays a song from Youtube, Soundcloud or Spotify.")
                        .addOption(OptionType.STRING,"song",
                                "Text for Youtube search or the link to the song."),
                Commands.slash("file","Plays the .wav, .mp3 or .ogg file attached.")
                        .addOption(OptionType.ATTACHMENT,"file","The file to play."),
                Commands.slash("dc","Disconnects the bot from the voice channel."),
                Commands.slash("skip",
                        "Skips the current playing song and plays the next one, if any."),
                Commands.slash("queue",
                        "Shows the playing song and the first 10 songs of the queue."),
                Commands.slash("clear", "Clears the queue."),
                Commands.slash("pause", "Pauses the player."),
                Commands.slash("resume", "Resumes the player."),
                Commands.slash("stop", "Stops the playing song and clears the queue."),
                Commands.slash("shuffle", "Shuffles the queue."),
                Commands.slash("remove","Removes a specific song fom the queue.")
                        .addOption(OptionType.STRING,"position",
                                "The number of the song in the queue. Must be within 1 and 10."),
                Commands.slash("loop","Bot enters loop mode, " +
                        "repeating the first track until it turned off by calling this command again."),
                Commands.slash("next", "Same as the \"skip\" command. " +
                        "Skips the current playing song and plays the next one, if any."),
                Commands.slash("first","Puts a song in the front of the queue.")
                        .addOption(OptionType.STRING, "song",
                                "The song to put in the front of the queue.")
        ).queue();
    }
}
