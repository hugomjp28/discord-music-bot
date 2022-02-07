import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.DecodedTrackHolder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.*;
import java.util.concurrent.ExecutionException;

public class FileAudioManager {
    AudioPlayerManager playerManager;
    AudioPlayer player;
    TrackScheduler trackScheduler;

    public FileAudioManager() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer();
        trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
    }

    public void play(Message.Attachment file, MessageReceivedEvent event){
        InputStream toPlay = null;
        try {
            toPlay = file.retrieveInputStream().get();
        } catch (Exception e) {
            System.out.println("Stinky");
        }
        MessageInput aux = new MessageInput(toPlay);
        try {
            DecodedTrackHolder decodedTrackHolder = playerManager.decodeTrack(aux);
            player.playTrack(decodedTrackHolder.decodedTrack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void skip(MessageReceivedEvent event) {
        trackScheduler.nextTrack();
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
}
