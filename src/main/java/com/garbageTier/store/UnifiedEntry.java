package com.garbageTier.store;

import com.github.felixgail.gplaymusic.model.Track;

public class UnifiedEntry {
    private Track googleTrack;
    private com.wrapper.spotify.model_objects.specification.Track spotifyTrack;

    public UnifiedEntry(Track gtrack, com.wrapper.spotify.model_objects.specification.Track strack) {
        this.googleTrack = gtrack;
        this.spotifyTrack = strack;
    }

    public Track getGoogleTrack() {
        return googleTrack;
    }

    public void setGoogleTrack(Track googleTrack) {
        this.googleTrack = googleTrack;
    }

    public com.wrapper.spotify.model_objects.specification.Track getSpotifyTrack() {
        return spotifyTrack;
    }

    public void setSpotifyTrack(com.wrapper.spotify.model_objects.specification.Track spotifyTrack) {
        this.spotifyTrack = spotifyTrack;
    }
}
