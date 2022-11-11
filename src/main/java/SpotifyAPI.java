import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.CompletableFuture;

public class SpotifyAPI {
    public SpotifyApi spotifyApi;

    public SpotifyAPI(){
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId("14a437699f2e4e26828082c6c8be5ec7")
                .setClientSecret("d81c6d44e19a4de1950ceeaf0ce3adae")
                .build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();
        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (Exception e) {
            //oopsie
            System.out.println("bad");
        }
    }

    public void refreshCredentials() {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();
        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (Exception e) {
            //oopsie
            System.out.println("bad");
        }
    }

    public void getTrack(String uri, YoutubeAudioManager youtube, MessageReceivedEvent event,
                         SlashCommandInteractionEvent slash, boolean print) {
        GetTrackRequest getTrackRequest = spotifyApi.getTrack(uri).build();
        try{
            Track track = getTrackRequest.execute();
            StringBuilder toSearch = new StringBuilder(track.getName());
            for(ArtistSimplified artist : track.getArtists()) {
                toSearch.append(" ").append(artist.getName());
            }
            youtube.play(toSearch.toString(),event, slash,true);
            if(print) {
                CommandHandler.handleResponse(event,slash,track.getName() + " added to queue.");
            }
        } catch (Exception e) {
            refreshCredentials();
            getTrack(uri,youtube,event,slash,print);
        }
    }

    public void getPlaylist(String uri, YoutubeAudioManager youtube,
                            MessageReceivedEvent event, SlashCommandInteractionEvent slash) {
        refreshCredentials();
        GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                .getPlaylistsItems(uri)
                .build();
        try {
            Paging<PlaylistTrack> execute = getPlaylistsItemsRequest.execute();
            String next = "start";
            int total = 0;
            int i = 0;
            CompletableFuture<Void> task = CompletableFuture.runAsync(()->{});
            while (next != null) {
                next = execute.getNext();
                PlaylistTrack[] playlistTracks = execute.getItems();
                task.thenRunAsync(()->{
                    for (PlaylistTrack playlistTrack: playlistTracks) {
                        if(playlistTrack.getTrack() != null) {
                            getTrack(playlistTrack.getTrack().getId(), youtube, event, slash, false);
                        }
                    }
                });
                total += playlistTracks.length;
                i+= 100;
                GetPlaylistsItemsRequest getting = spotifyApi
                        .getPlaylistsItems(uri)
                        .offset(i)
                        .build();
                execute = getting.execute();
            }
            CommandHandler.handleResponse(event,slash,total + " songs added to queue.");
        }catch (Exception e) {
            refreshCredentials();
            CommandHandler.handleResponse(event,slash,"Oops I fucked up, please try again!");
        }
    }

    public void getAlbum(String uri, YoutubeAudioManager youtube,
                         MessageReceivedEvent event, SlashCommandInteractionEvent slash){
        GetAlbumsTracksRequest getAlbumItemsRequest = spotifyApi
                .getAlbumsTracks(uri)
                .build();
        try {
            TrackSimplified[] albumTracks = getAlbumItemsRequest.execute().getItems();
            for (TrackSimplified albumTrack : albumTracks) {
                StringBuilder toSearch = new StringBuilder(albumTrack.getName());
                for (ArtistSimplified artist : albumTrack.getArtists()) {
                    toSearch.append(" ").append(artist.getName());
                }
                youtube.play(toSearch.toString(), event, slash,true);
            }
            CommandHandler.handleResponse(event,slash,albumTracks.length + " songs added to queue.");
        } catch (Exception e) {
            refreshCredentials();
            getAlbum(uri,youtube,event,slash);
        }
    }

    public void getFirst(String uri, YoutubeAudioManager youtube, MessageReceivedEvent event,
                         SlashCommandInteractionEvent slash, boolean print) {
        GetTrackRequest getTrackRequest = spotifyApi.getTrack(uri).build();
        try{
            Track track = getTrackRequest.execute();
            StringBuilder toSearch = new StringBuilder(track.getName());
            for(ArtistSimplified artist : track.getArtists()) {
                toSearch.append(" ").append(artist.getName());
            }
            youtube.first(toSearch.toString(),event, slash,true);
            if(print) {
                CommandHandler.handleResponse(event,slash,track.getName() + " is now the main character.");
            }
        } catch (Exception e) {
            refreshCredentials();
            getFirst(uri,youtube,event,slash,print);
        }
    }
}
