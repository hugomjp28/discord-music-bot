import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final BlockingQueue<AudioTrack> queue;
    private boolean loop = false;
    private final AudioPlayer player;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // A track started playing
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            // Start next track
            if(loop) {
                player.startTrack(track.makeClone(), false);
            }
            else if(!queue.isEmpty()) {
                player.startTrack(queue.poll(), false);
            } else {
                player.startTrack(null, true);
            }
        } else {
            //do nothing?
            player.startTrack(null, true);
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // Start next track
        System.out.println("exception");
        player.startTrack(queue.poll(), true);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
        System.out.println("stuck");
        player.startTrack(queue.poll(), true);
    }

    public void queue(AudioTrack audio, AudioPlayer player) {
        if (!player.startTrack(audio, true)) {
            queue.add(audio);
        }
    }

    public boolean nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrack poll = queue.poll();
        if (poll != null) {
            return player.startTrack(poll, false);
        } else {
            return player.startTrack(null, false);
        }
    }

    public void showQueue(MessageReceivedEvent event) {
        StringBuilder response = new StringBuilder();
        AudioTrack current = player.getPlayingTrack();
        if(current == null) {
            event.getChannel().sendMessage("Queue is empty.").queue();
            return;
        }
        response.append("Now playing - " + current.getInfo().title)
                .append(" " + (current.getDuration() / 1000) / 60 + ":" +
                        (current.getDuration() / 1000) % 60 + "\n");
        Object[] aux = queue.toArray();
        for (int i = 0; i < aux.length && i <= 9; i++) {
            AudioTrack track = (AudioTrack) aux[i];
            response.append((i + 1) + " - " + track.getInfo().title + "\n");
        }
        if(queue.size() > 10) {
            response.append("...\n");
        }
        response.append("Songs in queue: " + queue.size() + "\n");
        String toSend = response.toString();
        event.getChannel().sendMessage(toSend).queue();
    }

    public void clearQueue(MessageReceivedEvent event) {
        if(!queue.isEmpty()) {
            queue.clear();
        }
        event.getChannel().sendMessage("Cleared Queue.").queue();
    }

    public void pause(MessageReceivedEvent event) {
        if(!player.isPaused()){
            player.setPaused(true);
            event.getChannel().sendMessage("Pausing.").queue();
        } else {
            event.getChannel().sendMessage("Player is already paused.").queue();
        }
    }

    public void resume(MessageReceivedEvent event) {
        if(player.isPaused()){
            player.setPaused(false);
            event.getChannel().sendMessage("Resuming.").queue();
        } else {
            event.getChannel().sendMessage("Player is already playing.").queue();
        }
    }

    public void stop(MessageReceivedEvent event) {
        player.stopTrack();
        clearQueue(event);
        event.getChannel().sendMessage("Player stopped.").queue();
    }

    public void shuffle(MessageReceivedEvent event) {
        if(!queue.isEmpty()) {
            LinkedList<AudioTrack> aux = new LinkedList<>();
            queue.drainTo(aux);
            Collections.shuffle(aux);
            queue.addAll(aux);
            event.getChannel().sendMessage("Playlist Shuffled.").queue();
            return;
        }
        event.getChannel().sendMessage("No tracks in queue.").queue();
    }

    public void remove(MessageReceivedEvent event, int song) {
        if(!queue.isEmpty() && song <= queue.size()) {
            LinkedList<AudioTrack> aux = new LinkedList<>();
            queue.drainTo(aux);
            aux.remove(song-1);
            queue.addAll(aux);
            event.getChannel().sendMessage("Removed song from queue.").queue();
            return;
        }
        event.getChannel().sendMessage("Queue is empty or invalid number.").queue();
    }

    public void setLoop(MessageReceivedEvent event){
        loop = !loop;
        event.getChannel().sendMessage("Looping is " + (loop ? "on" : "off")).queue();
    }
}