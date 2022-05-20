import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

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

    public void getTrack(String uri, YoutubeAudioManager youtube, MessageReceivedEvent event, boolean print) {
        GetTrackRequest getTrackRequest = spotifyApi.getTrack(uri).build();
        try{
            Track track = getTrackRequest.execute();
            StringBuilder toSearch = new StringBuilder(track.getName());
            for(ArtistSimplified artist : track.getArtists()) {
                toSearch.append(" ").append(artist.getName());
            }
            youtube.play(toSearch.toString(),event, true);
            if(print) {
                event.getChannel().sendMessage(track.getName() + " added to queue.").queue();
            }
        } catch (Exception e) {
            refreshCredentials();
            getTrack(uri,youtube,event,print);
        }
    }

    public void getPlaylist(String uri, YoutubeAudioManager youtube, MessageReceivedEvent event) {
        GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                .getPlaylistsItems(uri)
                .build();
        try {
            Paging<PlaylistTrack> execute = getPlaylistsItemsRequest.execute();
            String next = execute.getNext();
            int total = 0;
            int i = 0;
            while (next != null) {
                next = execute.getNext();
                PlaylistTrack[] playlistTracks = execute.getItems();
                for (PlaylistTrack playlistTrack : playlistTracks) {
                    if(playlistTrack.getTrack() != null) getTrack(playlistTrack.getTrack().getId(),youtube,event,false);
                }
                total += playlistTracks.length;
                i+= 100;
                GetPlaylistsItemsRequest getting = spotifyApi
                        .getPlaylistsItems(uri)
                        .offset(i)
                        .build();
                execute = getting.execute();
            }
            event.getChannel().sendMessage(total + " songs added to queue.").queue();
            /*PlaylistTrack[] playlistTracks = getPlaylistsItemsRequest.execute().getItems();
            for (PlaylistTrack playlistTrack : playlistTracks) {
                if(playlistTrack.getTrack() != null) getTrack(playlistTrack.getTrack().getId(),youtube,event,false);
            }
            event.getChannel().sendMessage(playlistTracks.length + " songs added to queue.").queue();*/
        }catch (Exception e) {
            refreshCredentials();
        }
    }

    public void getAlbum(String uri, YoutubeAudioManager youtube, MessageReceivedEvent event){
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
                youtube.play(toSearch.toString(), event, true);
            }
            event.getChannel().sendMessage(albumTracks.length + " songs added to queue.").queue();
        } catch (Exception e) {
            refreshCredentials();
            getAlbum(uri,youtube,event);
        }
    }
}
