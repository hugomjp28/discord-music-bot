import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class YoutubeAudioManager {
    AudioPlayerManager playerManager;
    AudioPlayer youtube;
    TrackScheduler trackScheduler;

    public YoutubeAudioManager() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        youtube = playerManager.createPlayer();
        trackScheduler = new TrackScheduler(youtube,playerManager);
        youtube.addListener(trackScheduler);
    }

    public void play(String identifier, MessageReceivedEvent event, SlashCommandInteractionEvent slash, boolean isSpotify){
        String song;
        if(!identifier.split("/")[0].contains("https")) {
            song = "ytsearch:" + identifier;
        } else {
            song = identifier;
        }
        playerManager.loadItem(song, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(track, youtube);
                if(!isSpotify) {
                    CommandHandler.handleResponse(event, slash,track.getInfo().title + " added to queue.");
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(song.contains("ytsearch:")) {
                    trackScheduler.queue(playlist.getTracks().get(0), youtube);
                    if(!isSpotify) {
                        CommandHandler.handleResponse(event,slash,
                                playlist.getTracks().get(0).getInfo().title + " added to queue.");
                    }
                } else {
                    for(AudioTrack track : playlist.getTracks()) {
                        trackScheduler.queue(track,youtube);
                    }
                    CommandHandler.handleResponse(event,slash,
                            playlist.getTracks().size() + " tracks added to queue.");
                }
            }

            @Override
            public void noMatches() {
                System.out.println("no match");
                CommandHandler.handleResponse(event,slash,"No matches found.");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                if(!isSpotify) {
                    CommandHandler.handleResponse(event,slash,"Failed to load track.");
                }
            }
        });
    }

    public void playFile(Message.Attachment file, MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        playerManager.loadItem(file.getProxyUrl(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(track, youtube);
                CommandHandler.handleResponse(event,slash, track.getInfo().title + " added to queue.");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                System.out.println("no playlist here");
            }

            @Override
            public void noMatches() {
                System.out.println("no match");
                CommandHandler.handleResponse(event,slash,"No matches found.");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                CommandHandler.handleResponse(event,slash,"Failed to load track.");
            }
        });
    }

    public void playSoundcloud(String identifier, MessageReceivedEvent event, SlashCommandInteractionEvent slash){
        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(track, youtube);
                CommandHandler.handleResponse(event,slash,track.getInfo().title + " added to queue.");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {
                System.out.println("no match");
                CommandHandler.handleResponse(event,slash,"No matches found.");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                CommandHandler.handleResponse(event,slash,"Failed to load track.");
            }
        });
    }

    public void clean(MessageReceivedEvent event, SlashCommandInteractionEvent slash){
        stop(event, slash);
    }

    public void skip(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        if(trackScheduler.nextTrack()){
            CommandHandler.handleResponse(event, slash,
                    "Now playing: " + youtube.getPlayingTrack().getInfo().title);
        } else {
            CommandHandler.handleResponse(event,slash,"Skipped.");
        }
    }

    public void showQueue(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        trackScheduler.showQueue(event,slash);
    }

    public void clearQueue(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        trackScheduler.clearQueue(event, slash);
    }

    public void pause(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        trackScheduler.pause(event, slash);
    }

    public void resume(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        trackScheduler.resume(event, slash);
    }

    public void stop(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        trackScheduler.stop(event, slash);
    }

    public void shuffle(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        trackScheduler.shuffle(event, slash);
    }

    public void remove(MessageReceivedEvent event, SlashCommandInteractionEvent slash, int song) {
        trackScheduler.remove(event, slash, song);
    }

    public void loop(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        trackScheduler.setLoop(event, slash);
    }
}
