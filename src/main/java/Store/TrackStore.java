package Store;

import com.github.felixgail.gplaymusic.model.Playlist;
import com.github.felixgail.gplaymusic.model.Track;

import java.util.*;

public class TrackStore {
    private Map<String, Track> gplayTrackMap;
    private Map<String, Playlist> gplayPlaylistMap;
    private Map<String, Set<String>> playlistTrackAssociations;
    private Map<String, com.wrapper.spotify.model_objects.specification.Track> spotifyTrackMap;
    private Map<String, com.wrapper.spotify.model_objects.specification.Playlist> spotifyPlaylistMap;

    public TrackStore() {
        gplayTrackMap = new HashMap<>();
        gplayPlaylistMap = new HashMap<>();
        playlistTrackAssociations = new HashMap<>();
        spotifyTrackMap = new HashMap<>();
        spotifyPlaylistMap = new HashMap<>();
    }

    public List<Track> getAllTracks() {
        return new ArrayList<>(gplayTrackMap.values());
    }

    public void putTrack(Track track) {
        gplayTrackMap.put(makeTrackKey(track), track);
    }

    public List<Playlist> getAllPlaylists() {
        return new ArrayList<>(gplayPlaylistMap.values());
    }

    public void putPlaylist(Playlist playlist) {
        gplayPlaylistMap.put(playlist.getId(), playlist);
    }

    public Set<Track> getTracksForPlaylist(Playlist playlist) {
        Set<String> trackKeysInPlaylist = playlistTrackAssociations.get(playlist.getId());
        Set<Track> tracks = new HashSet<>();

        trackKeysInPlaylist.forEach(trackKey -> tracks.add(gplayTrackMap.get(trackKey)));

        return tracks;
    }

    public void putAssociation(Playlist playlist, Track track) {
        gplayTrackMap.put(makeTrackKey(track), track);
        gplayPlaylistMap.put(playlist.getId(), playlist);

        Set<String> tracksInPlaylist = playlistTrackAssociations.get(playlist.getId());
        if(tracksInPlaylist == null)
            tracksInPlaylist = new HashSet<>();

        tracksInPlaylist.add(makeTrackKey(track));
        playlistTrackAssociations.put(playlist.getId(), tracksInPlaylist);
    }

    private String makeTrackKey(Track track) {
        return (track.getTitle()+track.getArtist()).toLowerCase().replaceAll("\\s", "");
    }
}
