import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Listener extends ListenerAdapter {
    private char prefix;
    public Listener(char prefix) {
        this.prefix = prefix;
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) {
            return;
        }
        String message = event.getMessage().getContentRaw();
        if(!message.isEmpty()) {
            char messagePrefix = message.charAt(0);
            message = message.substring(1);
            String[] divided = message.split(" ");

            if(messagePrefix == prefix) {
                switch(divided[0].toLowerCase()){
                    case("ping") :
                        CommandHandler.handleResponse(event,"PONG");
                        break;
                    case("prefix") :
                        this.prefix = divided[1].charAt(0);
                        CommandHandler.handleResponse(event,"Prefix changed to " + prefix);
                        break;
                    case("play") :
                        if(divided.length >= 2) {
                            CommandHandler.handlePlay(event, message.substring(5));
                        } else {
                            CommandHandler.handleResponse(event, "No song detected.");
                        }
                        break;
                    case("file") :
                        List<Message.Attachment> attachments = event.getMessage().getAttachments();
                        if(attachments.size() > 0) {
                            String fileExtension = attachments.get(0).getFileExtension();
                            assert fileExtension != null;
                            if(fileExtension.equals("mp3") || fileExtension.equals("wav") || fileExtension.equals("ogg")) {
                                CommandHandler.handleFile(event, attachments.get(0));
                            } else {
                                CommandHandler.handleResponse(event, "No valid file attached.");
                            }
                        } else {
                            CommandHandler.handleResponse(event, "No file attached.");
                        }
                        break;
                    case("skip") :
                        CommandHandler.handleSkip(event);
                        break;
                    case("next") :
                        CommandHandler.handleSkip(event);
                        break;
                    case("pause") :
                        CommandHandler.handlePause(event);
                        break;
                    case("resume") :
                        CommandHandler.handleResume(event);
                        break;
                    case("stop") :
                        CommandHandler.handleStop(event);
                        break;
                    case("queue") :
                        CommandHandler.handleQueue(event);
                        break;
                    case("clear"):
                        CommandHandler.handleClear(event);
                        break;
                    case("shuffle"):
                        CommandHandler.handleShuffle(event);
                        break;
                    case("remove") :
                        if(divided.length >= 2) {
                            CommandHandler.handleRemove(event, message.substring(7));
                        } else {
                            CommandHandler.handleResponse(event, "Which song to remove?");
                        }
                        break;
                    case("loop") :
                        CommandHandler.handleLoop(event);
                        break;
                    case("dc") :
                        CommandHandler.handleDisconnect(event);
                        break;
                }
            }
        }
    }
}
