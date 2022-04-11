import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class YoutubeAudioManager {
    AudioPlayerManager playerManager;
    AudioPlayer youtube;
    TrackScheduler trackScheduler;

    public YoutubeAudioManager() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        youtube = playerManager.createPlayer();
        trackScheduler = new TrackScheduler(youtube);
        youtube.addListener(trackScheduler);
    }

    public void play(String identifier, MessageReceivedEvent event, boolean isSpotify){
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
                    event.getChannel().sendMessage(track.getInfo().title + " added to queue.").queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(song.contains("ytsearch:")) {
                    trackScheduler.queue(playlist.getTracks().get(0), youtube);
                    if(!isSpotify) {
                        event.getChannel().sendMessage(playlist.getTracks().get(0).getInfo().title + " added to queue.")
                                .queue();
                    }
                } else {
                    for(AudioTrack track : playlist.getTracks()) {
                        trackScheduler.queue(track,youtube);
                    }
                    event.getChannel().sendMessage(playlist.getTracks().size() + " tracks added to queue.")
                            .queue();
                }
            }

            @Override
            public void noMatches() {
                System.out.println("no match");
                event.getChannel().sendMessage("No matches found.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                if(!isSpotify) {
                    event.getChannel().sendMessage("Failed to load track.").queue();
                }
            }
        });
    }

    public void playFile(Message.Attachment file, MessageReceivedEvent event) {
        playerManager.loadItem(file.getProxyUrl(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(track, youtube);
                event.getChannel().sendMessage(track.getInfo().title + " added to queue.").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                System.out.println("no playlist here");
            }

            @Override
            public void noMatches() {
                System.out.println("no match");
                event.getChannel().sendMessage("No matches found.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                event.getChannel().sendMessage("Failed to load track.").queue();
            }
        });
    }

    public void playSoundcloud(String identifier, MessageReceivedEvent event){
        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(track, youtube);
                event.getChannel().sendMessage(track.getInfo().title + " added to queue.").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {
                System.out.println("no match");
                event.getChannel().sendMessage("No matches found.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                event.getChannel().sendMessage("Failed to load track.").queue();
            }
        });
    }

    public void clean(MessageReceivedEvent event){
        stop(event);
    }

    public void skip(MessageReceivedEvent event) {
        if(trackScheduler.nextTrack()){
            event.getChannel().sendMessage("Now playing: " + youtube.getPlayingTrack().getInfo().title).queue();
        }
    }

    public void showQueue(MessageReceivedEvent event) {
        trackScheduler.showQueue(event);
    }

    public void clearQueue(MessageReceivedEvent event) {
        trackScheduler.clearQueue(event);
    }

    public void pause(MessageReceivedEvent event) {
        trackScheduler.pause(event);
    }

    public void resume(MessageReceivedEvent event) {
        trackScheduler.resume(event);
    }

    public void stop(MessageReceivedEvent event) {
        trackScheduler.stop(event);
    }

    public void shuffle(MessageReceivedEvent event) {
        trackScheduler.shuffle(event);
    }

    public void remove(MessageReceivedEvent event, int song) {
        trackScheduler.remove(event, song);
    }

    public void loop(MessageReceivedEvent event) {
        trackScheduler.setLoop(event);
    }
}
