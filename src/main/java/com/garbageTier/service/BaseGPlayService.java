package com.garbageTier.service;

import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.api.PlaylistApi;
import com.github.felixgail.gplaymusic.api.TrackApi;
import svarzee.gps.gpsoauth.AuthToken;

import java.io.IOException;
import java.util.Locale;

public class BaseGPlayService {
    private GPlayMusic gPlayMusic;

    public BaseGPlayService() {
        String androidId = AuthTokenProviderService.getAndroidId();
        AuthToken token = null;
        try {
            token = AuthTokenProviderService.getAuthToken();
        } catch (IOException ioe) {
            System.out.println("Failed to get auth token");
        }

        GPlayMusic.Builder gplayBuilder = new GPlayMusic.Builder();
        gPlayMusic = gplayBuilder.setAuthToken(token)
                .setAndroidID(androidId)
                .setLocale(Locale.US)
                .build();

        gPlayMusic.getTrackApi().useCache(true);
    }

    public TrackApi getTrackApi() {
        return gPlayMusic.getTrackApi();
    }

    public PlaylistApi getPlaylistApi() {
        return gPlayMusic.getPlaylistApi();
    }

}
