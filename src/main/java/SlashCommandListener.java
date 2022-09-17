import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {
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

            } break;
            case("file") : {
                event.deferReply().queue();
                try {
                    Message.Attachment attachment = event.getOption("file").getAsAttachment();
                    String fileExtension = attachment.getFileExtension();
                    assert fileExtension != null;
                    if(fileExtension.equals("mp3") || fileExtension.equals("wav") || fileExtension.equals("ogg")) {
                        CommandHandler.handleFile(null, event, attachment);
                    } else {
                        CommandHandler.handleResponse(null, event,"No valid file attached.");
                    }
                } catch (NullPointerException ex) {
                    CommandHandler.handleResponse(null, event,"No file attached.");
                }
            } break;
            case("skip") :
            case("next") : {
                event.deferReply().queue();
                CommandHandler.handleSkip(null,event);
            } break;
            case("dc") : {
                event.deferReply().queue();
                CommandHandler.handleDisconnect(null,event);
            } break;
            case("queue") : {
                event.deferReply().queue();
                CommandHandler.handleQueue(null,event);
            } break;
            case("clear") : {
                event.deferReply().queue();
                CommandHandler.handleClear(null,event);
            } break;
            case("pause") : {
                event.deferReply().queue();
                CommandHandler.handlePause(null,event);
            } break;
            case("resume") : {
                event.deferReply().queue();
                CommandHandler.handleResume(null,event);
            } break;
            case("stop") : {
                event.deferReply().queue();
                CommandHandler.handleStop(null,event);
            } break;
            case("shuffle") : {
                event.deferReply().queue();
                CommandHandler.handleShuffle(null,event);
            } break;
            case("remove") : {
                event.deferReply().queue();
                try {
                    String index = event.getOption("position").getAsString();
                    CommandHandler.handleRemove(null,event,index);
                }catch (NullPointerException ex) {
                    CommandHandler.handleResponse(null,event,"Which song to remove?");
                }
            } break;
            case("loop") : {
                event.deferReply().queue();
                CommandHandler.handleLoop(null,event);
            } break;
            case("first") : {
                event.deferReply().queue();
                try {
                    String song = event.getInteraction().getOption("song").getAsString();
                    CommandHandler.handleFirst(null,event,song);
                } catch (NullPointerException ex) {
                    CommandHandler.handleResponse(null, event, "You must tell me what to play!");
                }
            }
        }
    }
}
