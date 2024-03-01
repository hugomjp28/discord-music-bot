import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.List;

public class Listener extends ListenerAdapter {
    private final char PREFIX;
    public Listener(char prefix) {
        this.PREFIX = prefix;
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

            if(messagePrefix == PREFIX) {
                switch(divided[0].toLowerCase()){
                    case("ping") :
                        CommandHandler.handleResponse(event,null,"PONG!");
                        break;
                    case("play") :
                        if(divided.length >= 2) {
                            CommandHandler.handlePlay(event, null,message.substring(5));
                        } else {
                            CommandHandler.handleResponse(event, null,"Give song RIGHT NOW or I'm die.");
                        }
                        break;
                    case("file") :
                        List<Message.Attachment> attachments = event.getMessage().getAttachments();
                        if(!attachments.isEmpty()) {
                            String fileExtension = attachments.getFirst().getFileExtension();
                            assert fileExtension != null;
                            if(fileExtension.equals("mp3") || fileExtension.equals("wav") || fileExtension.equals("ogg")) {
                                CommandHandler.handleFile(event, null, attachments.getFirst());
                            } else {
                                CommandHandler.handleResponse(event, null,"Girl, I can't read that!");
                            }
                        } else {
                            CommandHandler.handleResponse(event, null,"Give file RIGHT NOW or I'm die.");
                        }
                        break;
                    case("skip") :
                    case("next") :
                        CommandHandler.handleSkip(event, null);
                        break;
                    case("pause") :
                        CommandHandler.handlePause(event, null);
                        break;
                    case("resume") :
                        CommandHandler.handleResume(event, null);
                        break;
                    case("stop") :
                        CommandHandler.handleStop(event, null);
                        break;
                    case("queue") :
                        CommandHandler.handleQueue(event, null);
                        break;
                    case("clear"):
                        CommandHandler.handleClear(event, null);
                        break;
                    case("shuffle"):
                        CommandHandler.handleShuffle(event, null);
                        break;
                    case("remove") :
                        if(divided.length >= 2) {
                            CommandHandler.handleRemove(event, null, divided[1]);
                        } else {
                            CommandHandler.handleResponse(event, null,
                                    "Which number am I supposed to remove???? Your mom????");
                        }
                        break;
                    case("loop") :
                        CommandHandler.handleLoop(event, null);
                        break;
                    case("dc") :
                        CommandHandler.handleDisconnect(event, null);
                        break;
                    case("first") :
                        if(divided.length >= 2) {
                            CommandHandler.handleFirst(event, null, divided[1]);
                        } else {
                            CommandHandler.handleResponse(event, null,"Give song RIGHT NOW or I'm die.");
                        }
                        break;
                    case("undo") :
                    case("revert") :
                        CommandHandler.handleUndo(event, null);
                        break;
                }
            }
        }
    }
}
