import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final BlockingQueue<AudioTrack> queue;
    private boolean loop = false;
    private final AudioPlayer player;
    private final AudioPlayerManager playerManager;

    public TrackScheduler(AudioPlayer player, AudioPlayerManager playerManager) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.playerManager = playerManager;
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
        if (endReason.mayStartNext && endReason != AudioTrackEndReason.LOAD_FAILED) {
            if (loop) {
                player.startTrack(track.makeClone(), false);
            } else if (!queue.isEmpty()) {
                player.startTrack(queue.poll(), false);
            } else {
                player.startTrack(null, true);
            }
        } else if(endReason == AudioTrackEndReason.LOAD_FAILED) {
            //do nothing
        } else {
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
        //reloads track
        playerManager.loadItem(track.getIdentifier(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                player.startTrack(audioTrack,false);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                //do nothing
            }

            @Override
            public void noMatches() {
                //do nothing
            }

            @Override
            public void loadFailed(FriendlyException e) {
                //do nothing
            }
        });
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
        System.out.println("stuck");
        nextTrack();
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

    public void showQueue(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        StringBuilder response = new StringBuilder();
        AudioTrack current = player.getPlayingTrack();
        if(current == null) {
            CommandHandler.handleResponse(event, slash, "Queue is empty.");
            return;
        }
        response.append("Now playing - " + current.getInfo().title)
                .append(" " + String.format("%02d",(current.getDuration() / 1000) / 60) + ":" +
                        String.format("%02d",(current.getDuration() / 1000) % 60) + "\n");
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
        CommandHandler.handleResponse(event,slash,toSend);
    }

    public void clearQueue(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        if(!queue.isEmpty()) {
            queue.clear();
        }
        CommandHandler.handleResponse(event,slash,"Cleared Queue.");
    }

    public void pause(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        if(!player.isPaused()){
            player.setPaused(true);
            CommandHandler.handleResponse(event,slash,"Pausing.");
        } else {
            CommandHandler.handleResponse(event,slash,"Player is already paused.");
        }
    }

    public void resume(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        if(player.isPaused()){
            player.setPaused(false);
            CommandHandler.handleResponse(event,slash,"Resuming.");
        } else {
            CommandHandler.handleResponse(event,slash,"Player is already playing.");
        }
    }

    public void stop(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        clearQueue(event, slash);
        player.stopTrack();
        CommandHandler.handleResponse(event,slash,"Player stopped.");
    }

    public void shuffle(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        if(!queue.isEmpty()) {
            LinkedList<AudioTrack> aux = new LinkedList<>();
            queue.drainTo(aux);
            Collections.shuffle(aux);
            queue.addAll(aux);
            CommandHandler.handleResponse(event,slash,"Playlist Shuffled.");
            return;
        }
        CommandHandler.handleResponse(event,slash,"No tracks in queue.");
    }

    public void remove(MessageReceivedEvent event, SlashCommandInteractionEvent slash, int song) {
        if(!queue.isEmpty() && song <= queue.size()) {
            LinkedList<AudioTrack> aux = new LinkedList<>();
            queue.drainTo(aux);
            AudioTrack removed = aux.remove(song - 1);
            queue.addAll(aux);
            CommandHandler.handleResponse(event,slash,"Removed \"" + removed.getInfo().title + "\" from queue.");
            return;
        }
        CommandHandler.handleResponse(event,slash,"Queue is empty or invalid number.");
    }

    public void setLoop(MessageReceivedEvent event, SlashCommandInteractionEvent slash){
        loop = !loop;
        CommandHandler.handleResponse(event,slash,"Looping is " + (loop ? "on" : "off"));
    }

    public void first(AudioTrack track, AudioPlayer youtube) {
        if (!player.startTrack(track, true)) {
            LinkedList<AudioTrack> aux = new LinkedList<>();
            queue.drainTo(aux);
            aux.addFirst(track);
            queue.addAll(aux);
        }
    }
}