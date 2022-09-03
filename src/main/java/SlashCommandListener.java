import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {
    public SlashCommandListener() {

    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        String commandName = event.getName().toLowerCase();
        switch (commandName) {
            case("ping") : event.deferReply().queue(); CommandHandler.handleResponse(null,event,"PONG!"); break;
            case("play") : {
                event.deferReply().queue();
                try {
                    String song = event.getInteraction().getOption("song").getAsString();
                    CommandHandler.handlePlay(null, event, song);
                } catch (NullPointerException ex) {
                    CommandHandler.handleResponse(null, event, "You must tell me what to play!");
                }
            }
        }
    }
}
