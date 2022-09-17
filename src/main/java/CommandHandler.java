import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {
    private static final SpotifyAPI spotifyApi = new SpotifyAPI();
    private static final Map<String,YoutubeAudioManager> audioManagers = new HashMap<>();

    private static AudioManager getGuildAudioManager(MessageReceivedEvent event, SlashCommandInteractionEvent slash, AudioChannel connectedChannel, YoutubeAudioManager youtube) {
        AudioManager audioManager = event != null ?
                event.getGuild().getAudioManager() : slash.getGuild().getAudioManager();
        if(audioManager.isConnected() && !audioManager.getConnectedChannel().equals(connectedChannel)) {
            handleResponse(event, slash,"The bot is already connected to a voice channel.");
            return null;
        } else if(!audioManager.isConnected()){
            audioManager.openAudioConnection(connectedChannel);
            audioManager.setSendingHandler(new AudioPlayerSendHandler(youtube.youtube));
        }
        return audioManager;
    }

    private static YoutubeAudioManager getYoutubeAudioManager(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        String id = event != null ? event.getGuild().getId() : slash.getGuild().getId();
        if(!audioManagers.containsKey(id)) {
            audioManagers.put(id, new YoutubeAudioManager());
        }
        return audioManagers.get(id);
    }

    //checks if you're connected to a voice channel
    @Nullable
    private static AudioChannel getAudioChannel(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = event != null ?
                event.getMember().getVoiceState().getChannel() : slash.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event, slash,"You are not in a voice channel!");
            return null;
        }
        return connectedChannel;
    }

    public static void handleResponse(MessageReceivedEvent event, SlashCommandInteractionEvent slash, String response) {
        if(slash != null) {
            slash.getHook().editOriginal(response).queue();
        } else {
            event.getChannel().sendMessage(response).queue();
        }
    }

    public static void handlePlay(MessageReceivedEvent event,SlashCommandInteractionEvent slash, String song) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        if (getGuildAudioManager(event, slash, connectedChannel, youtube) == null) return;
        if(song.contains("open.spotify.com")) {
            String[] removeQuery = song.split("\\?");
            String[] uriParts = removeQuery[0].split("/");
            if(uriParts[3].compareTo("track") == 0) {
                spotifyApi.getTrack(uriParts[4], youtube, event, slash,true);
            } else if(uriParts[3].compareTo("playlist") == 0) {
                spotifyApi.getPlaylist(uriParts[4],youtube,event,slash);
            } else if(uriParts[3].compareTo("album") == 0) {
                 spotifyApi.getAlbum(uriParts[4],youtube,event,slash);
            } else {
                handleResponse(event,slash,"Not Supported");
            }
        }
        else if(song.contains("https://soundcloud.com")) {
            youtube.playSoundcloud(song,event,slash);
        }
        else {
            youtube.play(song, event, slash,false);
        }
    }

    public static void handleFile(MessageReceivedEvent event, SlashCommandInteractionEvent slash, Message.Attachment attachment) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        if (getGuildAudioManager(event, slash, connectedChannel, youtube) == null) return;
        youtube.playFile(attachment,event, slash);
    }

    public static void handleDisconnect(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        AudioManager audioManager = getGuildAudioManager(event, slash, connectedChannel, youtube);
        if(audioManager == null) return;
        // Connects to the channel.
        youtube.clean(event,slash);
        audioManager.closeAudioConnection();
        handleResponse(event,slash,"**Storms off** I did not appreciate your tone.");
    }

    public static void handleSkip(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        youtube.skip(event,slash);
    }

    public static void handleQueue(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        youtube.showQueue(event,slash);
    }

    public static void handleClear(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        youtube.clearQueue(event, slash);
    }

    public static void handlePause(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        youtube.pause(event, slash);
    }

    public static void handleResume(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        youtube.resume(event, slash);
    }

    public static void handleStop(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        youtube.stop(event,slash);
    }

    public static void handleShuffle(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        if (getGuildAudioManager(event, slash, connectedChannel, youtube) == null) return;
        youtube.shuffle(event, slash);
    }

    public static void handleRemove(MessageReceivedEvent event, SlashCommandInteractionEvent slash, String toRemove) {
        int song = 0;
        try{
            song = Integer.parseInt(toRemove);
        } catch (NumberFormatException ex) {
            handleResponse(event, slash,"Which number am I supposed to remove???? Your mom????");
        }
        if(song < 1 || song > 10) {
            handleResponse(event, slash,"Do you even know basic math? It has to be between 1 and 10...");
            return;
        }
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        if (getGuildAudioManager(event, slash, connectedChannel, youtube) == null) return;
        youtube.remove(event, slash, song);
    }

    public static void handleLoop(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        youtube.loop(event, slash);
    }

    public static void handleFirst(MessageReceivedEvent event, SlashCommandInteractionEvent slash, String song) {
        AudioChannel connectedChannel = getAudioChannel(event, slash);
        if (connectedChannel == null) return;
        YoutubeAudioManager youtube = getYoutubeAudioManager(event, slash);
        if (getGuildAudioManager(event, slash, connectedChannel, youtube) == null) return;
        if(song.contains("open.spotify.com")) {
            String[] removeQuery = song.split("\\?");
            String[] uriParts = removeQuery[0].split("/");
            if(uriParts[3].compareTo("track") == 0) {
                spotifyApi.getFirst(uriParts[4], youtube, event, slash,true);
            } else {
                handleResponse(event,slash,"ONE AT A TIME!!!!!!");
            }
        } else {
            youtube.first(song, event,slash,false);
        }
    }
}
