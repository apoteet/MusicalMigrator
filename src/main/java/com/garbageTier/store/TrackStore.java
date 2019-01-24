package com.garbageTier.store;

import com.github.felixgail.gplaymusic.model.Playlist;
import com.github.felixgail.gplaymusic.model.Track;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;

import java.util.*;
import java.util.stream.Collectors;

public class TrackStore {
    //    private Map<String, Track> gplayTrackMap;
    private Map<String, Playlist> gplayPlaylistMap;
    private Map<String, Set<String>> playlistTrackAssociations;
    //    private Map<String, com.wrapper.spotify.model_objects.specification.Track> spotifyTrackMap;
    private Map<String, com.wrapper.spotify.model_objects.specification.Playlist> spotifyPlaylistMap;
    private Map<String, UnifiedEntry> unifiedMap;

    public TrackStore() {
//        gplayTrackMap = new HashMap<>();
        gplayPlaylistMap = new HashMap<>();
        playlistTrackAssociations = new HashMap<>();
//        spotifyTrackMap = new HashMap<>();
        spotifyPlaylistMap = new HashMap<>();
        unifiedMap = new HashMap<>();
    }

    public void putTrack(Track track) {
        mapGoogleToSpotify(track, null);
    }

    public void mapGoogleToSpotify(Track googleTrack, com.wrapper.spotify.model_objects.specification.Track spotifyTrack) {
        String searchKey = makeGoogleTrackKey(googleTrack);
        UnifiedEntry foundTrack = unifiedMap.get(searchKey);
        if (foundTrack == null) {
            foundTrack = new UnifiedEntry(googleTrack, spotifyTrack);
            unifiedMap.put(searchKey, foundTrack);
        } else {
            foundTrack.setGoogleTrack(googleTrack);
            foundTrack.setSpotifyTrack(spotifyTrack);
        }
    }

    public List<Track> getAllGoogleTracks() {
        return unifiedMap.values().stream().map(UnifiedEntry::getGoogleTrack).collect(Collectors.toList());
    }

    public List<com.wrapper.spotify.model_objects.specification.Track> getAllSpotifyTracks() {
        return unifiedMap.values().stream().map(UnifiedEntry::getSpotifyTrack).collect(Collectors.toList());
    }

    public List<com.wrapper.spotify.model_objects.specification.Track> getSpotifyTracksForGooglePlaylist(Playlist playlist) {
        Set<String> trackKeysInPlaylist = playlistTrackAssociations.get(playlist.getId());
        List<com.wrapper.spotify.model_objects.specification.Track> tracks = new ArrayList<>();

        trackKeysInPlaylist.forEach(trackKey -> tracks.add(unifiedMap.get(trackKey).getSpotifyTrack()));

        return tracks;
    }

    public void putAssociation(Playlist playlist, Track track) {
        putTrack(track);
        gplayPlaylistMap.put(playlist.getId(), playlist);

        Set<String> tracksInPlaylist = playlistTrackAssociations.get(playlist.getId());
        if (tracksInPlaylist == null)
            tracksInPlaylist = new HashSet<>();

        tracksInPlaylist.add(makeGoogleTrackKey(track));
        playlistTrackAssociations.put(playlist.getId(), tracksInPlaylist);
    }

    public List<Playlist> getAllGooglePlaylists() {
        return new ArrayList<>(gplayPlaylistMap.values());
    }

    public List<com.wrapper.spotify.model_objects.specification.Playlist> getAllSpotifyPlaylists() {
        return new ArrayList<>(spotifyPlaylistMap.values());
    }

    public void putPlaylist(Playlist playlist) {
        gplayPlaylistMap.put(playlist.getId(), playlist);
    }

    public void putPlaylist(com.wrapper.spotify.model_objects.specification.Playlist playlist) {
        spotifyPlaylistMap.put(playlist.getId(), playlist);
    }

    public int size() {
        return getAllGoogleTracks().size();
    }

    private String makeGoogleTrackKey(Track track) {
        return (track.getTitle() + track.getArtist()).toLowerCase().replaceAll("\\s", "");
    }
}
