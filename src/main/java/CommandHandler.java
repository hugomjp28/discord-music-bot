import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {
    private static final SpotifyAPI spotifyApi = new SpotifyAPI();
    private static Map<String,YoutubeAudioManager> audioManagers = new HashMap<>();
    private static Map<String,FileAudioManager> fileManagers = new HashMap<>();
    public static void handleResponse(MessageReceivedEvent event, String response) {
        event.getChannel().sendMessage(response).queue();
    }
    public static void handlePlay(MessageReceivedEvent event, String song) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        AudioManager audioManager = event.getGuild().getAudioManager();
        if(audioManager.isConnected() && !audioManager.getConnectedChannel().equals(connectedChannel)) {
            handleResponse(event,"The bot is already connected to a voice channel.");
            return;
        } else {
            audioManager.openAudioConnection(connectedChannel);
            audioManager.setSendingHandler(new AudioPlayerSendHandler(youtube.youtube));
        }
        if(song.contains("open.spotify.com")) {
            String[] removeQuery = song.split("\\?");
            String[] uriParts = removeQuery[0].split("/");
            if(uriParts[3].compareTo("track") == 0) {
                GetTrackRequest getTrackRequest = spotifyApi.spotifyApi.getTrack(uriParts[4]).build();
                try{
                    Track track = getTrackRequest.execute();
                    StringBuilder toSearch = new StringBuilder(track.getName());
                    for(ArtistSimplified artist : track.getArtists()) {
                        toSearch.append(" ").append(artist.getName());
                    }
                    youtube.play(toSearch.toString(),event);
                } catch (Exception e) {
                    spotifyApi.refreshCredentials();
                    handlePlay(event,song);
                }
            } else if(uriParts[3].compareTo("playlist") == 0) {
                GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.spotifyApi
                        .getPlaylistsItems(uriParts[4])
                        .build();
                try {
                    PlaylistTrack[] playlistTracks = getPlaylistsItemsRequest.execute().getItems();
                    for (PlaylistTrack playlistTrack : playlistTracks) {
                        GetTrackRequest getTrackRequest = spotifyApi.spotifyApi
                                .getTrack(playlistTrack.getTrack().getId())
                                .build();
                        Track track = getTrackRequest.execute();
                        StringBuilder toSearch = new StringBuilder(track.getName());
                        for (ArtistSimplified artist : track.getArtists()) {
                            toSearch.append(" ").append(artist.getName());
                        }
                        youtube.play(toSearch.toString(), event);
                    }
                }catch (Exception e) {
                    spotifyApi.refreshCredentials();
                    handlePlay(event,song);
                }
            } else if(uriParts[3].compareTo("album") == 0) {
                 GetAlbumsTracksRequest getAlbumItemsRequest = spotifyApi.spotifyApi
                        .getAlbumsTracks(uriParts[4])
                        .build();
                try {
                    TrackSimplified[] albumTracks = getAlbumItemsRequest.execute().getItems();
                    for (TrackSimplified albumTrack : albumTracks) {
                        StringBuilder toSearch = new StringBuilder(albumTrack.getName());
                        for (ArtistSimplified artist : albumTrack.getArtists()) {
                            toSearch.append(" ").append(artist.getName());
                        }
                        youtube.play(toSearch.toString(), event);
                    }
                } catch (Exception e) {
                    spotifyApi.refreshCredentials();
                    handlePlay(event,song);
                }
            } else {
                handleResponse(event,"Not Supported");
            }
        } else {
            youtube.play(song, event);
        }
    }

    public static void handleFile(MessageReceivedEvent event, Message.Attachment attachment) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        AudioManager audioManager = event.getGuild().getAudioManager();
        if(audioManager.isConnected() && !audioManager.getConnectedChannel().equals(connectedChannel)) {
            handleResponse(event,"The bot is already connected to a voice channel.");
            return;
        } else {
            audioManager.openAudioConnection(connectedChannel);
            audioManager.setSendingHandler(new AudioPlayerSendHandler(youtube.youtube));
        }
        youtube.playFile(attachment,event);
    }

    public static void handleDisconnect(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        AudioManager audioManager = event.getGuild().getAudioManager();
        if(!audioManager.isConnected() && audioManager.getConnectedChannel() != connectedChannel) {
            handleResponse(event,"The bot is not connected to a voice channel.");
            return;
        }
        // Connects to the channel.
        youtube.clean(event);
        audioManager.closeAudioConnection();
        handleResponse(event,"Bot disconnected!");
    }

    public static void handleSkip(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        youtube.skip(event);
    }

    public static void handleQueue(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        youtube.showQueue(event);
    }

    public static void handleClear(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        youtube.clearQueue(event);
    }

    public static void handlePause(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        youtube.pause(event);
    }

    public static void handleResume(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        youtube.resume(event);
    }

    public static void handleStop(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        youtube.stop(event);
    }

    public static void handleShuffle(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        AudioManager audioManager = event.getGuild().getAudioManager();
        if(!audioManager.isConnected() && audioManager.getConnectedChannel() != connectedChannel) {
            handleResponse(event,"The bot is not connected to a voice channel.");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        youtube.shuffle(event);
    }

    public static void handleRemove(MessageReceivedEvent event, String toRemove) {
        int song = 0;
        try{
            song = Integer.parseInt(toRemove);
        } catch (NumberFormatException ex) {
            handleResponse(event, "Input a number.");
        }
        if(song < 1 || song > 10) {
            handleResponse(event, "Input a number between 1-10.");
            return;
        }
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            handleResponse(event,"You are not in a voice channel!");
            return;
        }
        if(!audioManagers.containsKey(event.getGuild().getId())) {
            audioManagers.put(event.getGuild().getId(), new YoutubeAudioManager());
        }
        YoutubeAudioManager youtube = audioManagers.get(event.getGuild().getId());
        AudioManager audioManager = event.getGuild().getAudioManager();
        if(!audioManager.isConnected() && audioManager.getConnectedChannel() != connectedChannel) {
            handleResponse(event,"The bot is not connected to a voice channel.");
            return;
        }
        youtube.remove(event, song);
    }
}
