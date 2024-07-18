import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.BalancingIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

public class YoutubeAudioManager {
    AudioPlayerManager playerManager;
    AudioPlayer youtube;
    TrackScheduler trackScheduler;

    public YoutubeAudioManager() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager,
                com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
        YoutubeAudioSourceManager ytSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager(true, true, true);

        ArrayList<IpBlock> ipBlocks = new ArrayList<>();
        //Ipv4Block test = new Ipv4Block("161.35.69.0/24");
        Ipv4Block test2 = new Ipv4Block("192.168.1.0/24");
        Ipv6Block ipv6Block = new Ipv6Block("2a03:b0c0:3:d0::10de:4001/120");
        //ipBlocks.add(test);
        ipBlocks.add(ipv6Block);
        RotatingIpRoutePlanner routePlanner = new RotatingIpRoutePlanner(ipBlocks);
        BalancingIpRoutePlanner balancingIpRoutePlanner = new BalancingIpRoutePlanner(ipBlocks);

        YoutubeIpRotatorSetup rotator = new YoutubeIpRotatorSetup(balancingIpRoutePlanner);
        rotator.forConfiguration(ytSourceManager.getHttpInterfaceManager(), true)
                .withMainDelegateFilter(null) // This is important, otherwise you may get NullPointerExceptions.
                .withRetryLimit(255)
                .setup();
        playerManager.registerSourceManager(ytSourceManager);
        youtube = playerManager.createPlayer();
        trackScheduler = new TrackScheduler(youtube,playerManager);
        youtube.addListener(trackScheduler);
    }

    public void play(String identifier, MessageReceivedEvent event, SlashCommandInteractionEvent slash, boolean isSpotify){
        String song;
        if(!identifier.split("/")[0].contains("https")) {
            song = "ytsearch:" + identifier;
        } else {
            if (identifier.contains("watch?v=")){
                if (identifier.split("=")[1].contains("list")){
                    song = identifier.split("&")[1].split("=")[1];
                } else {
                    song = identifier.split("=")[1];
                }
            } else {
                song = identifier.split("/")[3];
            }
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
                CommandHandler.handleResponse(event,slash,"THE FUCK IS THAT?");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                exception.printStackTrace();
                if(!isSpotify) {
                    CommandHandler.handleResponse(event,slash,"Sorry can't sing that.");
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
                CommandHandler.handleResponse(event,slash,"THE FUCK IS THAT?");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                CommandHandler.handleResponse(event,slash,"Sorry can't sing that.");
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
                CommandHandler.handleResponse(event,slash,"THE FUCK IS THAT?");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                CommandHandler.handleResponse(event,slash,"Sorry can't sing that.");
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
            CommandHandler.handleResponse(event,slash,"Thank you, next.");
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

    public void first(String identifier, MessageReceivedEvent event, SlashCommandInteractionEvent slash, boolean isSpotify) {
        String song;
        if(!identifier.split("/")[0].contains("https")) {
            song = "ytsearch:" + identifier;
        } else {
            if (identifier.contains("watch?v=")){
                if (identifier.split("=")[1].contains("list")){
                    song = identifier.split("&")[1].split("=")[1];
                } else {
                    song = identifier.split("=")[1];
                }
            } else {
                song = identifier.split("/")[3];
            }
        }
        playerManager.loadItem(song, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.first(track, youtube);
                if(!isSpotify) {
                    CommandHandler.handleResponse(event, slash,track.getInfo().title + " is now the main character.");
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(song.contains("ytsearch:")) {
                    trackScheduler.first(playlist.getTracks().get(0), youtube);
                    if(!isSpotify) {
                        CommandHandler.handleResponse(event,slash,
                                playlist.getTracks().get(0).getInfo().title + " is now the main character.");
                    }
                } else {
                    CommandHandler.handleResponse(event,slash, "ONE AT A TIME!!!!!!");
                }
            }

            @Override
            public void noMatches() {
                System.out.println("no match");
                CommandHandler.handleResponse(event,slash,"THE FUCK IS THAT?");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("load failed");
                if(!isSpotify) {
                    CommandHandler.handleResponse(event,slash,"Sorry can't sing that.");
                }
            }
        });
    }

    public void undo(MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        trackScheduler.undo(event, slash);
    }
}
