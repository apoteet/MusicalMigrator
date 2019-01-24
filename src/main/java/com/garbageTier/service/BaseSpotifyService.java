package com.garbageTier.service;

import com.garbageTier.store.PagingList;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.garbageTier.util.ProgressTracker;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class BaseSpotifyService {
    private SpotifyApi spotifyApi;
    private AuthorizationCodeCredentials authCred;
    private String authRequestCode;
    private User userAccount;

    private static final String BS_REDIRECT = "https://www.google.com";
    private static final String CLIENT_ID = "161fba73b49f4089b4c593ea4fb5ece4";
    private static final String CLIENT_SECRET = "6635ed07a0ea4ea784de5aa232b86a53";


    public BaseSpotifyService() {
        try {
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(CLIENT_ID)
                    .setClientSecret(CLIENT_SECRET)
                    .setRedirectUri(SpotifyHttpManager.makeUri(BS_REDIRECT))
                    .build();

            refreshAuthToken();

            userAccount = spotifyApi.getCurrentUsersProfile().build().execute();
        } catch (Exception e) {
            System.out.println(e.toString());
            System.exit(2);
        }

    }

    public Playlist createPlaylist(com.github.felixgail.gplaymusic.model.Playlist gplaylist) {
        try {
            return spotifyApi.createPlaylist(userAccount.getId(), gplaylist.getName())
                    .collaborative(false)
                    .description(gplaylist.getDescription())
                    .build()
                    .execute();
        } catch (IOException | SpotifyWebApiException ex) {
            System.out.println(ex.toString());
            System.out.println("Error creating playlist " + gplaylist.getName());
            return null;
        }
    }

    private Track searchTrack(com.github.felixgail.gplaymusic.model.Track track) throws SpotifyWebApiException, IOException {
        int attempt = 1;
        String searchTerm = track.getTitle();
        String artistTerm = " artist:" + track.getArtist();
        while (true) {
            Paging<Track> searchResults = spotifyApi.searchTracks(searchTerm + artistTerm)
                    .market(CountryCode.US)
                    .limit(1)
                    .build()
                    .execute();
            if (searchResults.getTotal() < 1 && attempt == 1) {
                //first try failed, strip extra shit
                searchTerm = searchTerm.replaceAll("\\[.*]|\\(.*\\)", "");
                artistTerm = artistTerm.replaceAll("'", "");
            } else if (searchResults.getTotal() < 1 && attempt == 2) {
                //second try failed. Wildcard bitches!
                searchTerm = "*" + searchTerm + "*";
                artistTerm = artistTerm.replaceAll("[.:,]", " ");
            } else if (searchResults.getTotal() < 1 && attempt > 2) {
                //i give up
                System.out.println("Couldn't find any results for: " + track.getTitle() + " - " + track.getArtist() + " - " + track.getAlbum());
                return null;
            } else {
                //oh thank fuck it worked
                //Call out if wildcards were used because it could give very wrong results
                if (attempt > 2) {
                    System.out.println("Wildcard search used for " + track.getTitle() + " - " + track.getArtist());
                }
                return searchResults.getItems()[0];
            }
            attempt++;
        }
    }

    public Track addTrackToLibrary(com.github.felixgail.gplaymusic.model.Track track) {
        try {
            Track addedTrack = searchTrack(track);
            if (track == null)
                return null;

            spotifyApi.saveTracksForUser(addedTrack.getId());

            return addedTrack;
        } catch (IOException | SpotifyWebApiException ex) {
            System.out.println(ex.toString());
            System.out.println("Error adding track " + track.getTitle());
            return null;
        }
    }

    public List<Track> addTracksToLibrary(Collection<com.github.felixgail.gplaymusic.model.Track> tracks) {
        List<Track> addedTracks = new ArrayList<>();
        List<String> trackIds = new ArrayList<>();

        ProgressTracker progressWorker = new ProgressTracker(System.out, tracks.size());
        Thread progressThread = new Thread(progressWorker);
        System.out.println("Converting google track ids to spotify ids.");

        progressThread.start();
        tracks.forEach(track -> {
            try {
                Track spotifyTrack = searchTrack(track);

                progressWorker.increment();
                Thread.sleep(50);

                if (spotifyTrack == null) {
                    return;
                }
                addedTracks.add(spotifyTrack);
                trackIds.add(spotifyTrack.getId());
            } catch (SpotifyWebApiException ex) {
                System.out.println(ex.toString());
                try {
                    //API Rate exceeded; add current track back to the queue & wait 5 sec
                    tracks.add(track);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("Holy crap interrupted");
                }
            } catch (IOException | InterruptedException e) {
                System.out.println(e.toString());
            }
        });
        progressWorker.stop();

        PagingList<String> idsToAdd = new PagingList<>(trackIds, 50);

        int progress = 0;
        System.out.println("Adding track ids to library...");
        while (idsToAdd.hasNext()) {
            try {
                List ids = idsToAdd.nextPage();

                String[] input = new String[ids.size()];
                ids.toArray(input);

                spotifyApi.saveTracksForUser(input)
                        .build()
                        .execute();
                progress += input.length;
                System.out.println("Progress: " + progress + " / " + idsToAdd.size());
            } catch (IOException | SpotifyWebApiException ex) {
                System.out.println("Failed to add batch to library");
                System.out.println(ex.toString());
            }
        }
        System.out.println("All done");
        return addedTracks;
    }

    private void refreshAuthToken() {
        try {
            if (authRequestCode == null) {
                AuthorizationCodeUriRequest authCodeUriRequest = spotifyApi.authorizationCodeUri()
                        .scope("user-library-modify,playlist-modify-public,user-library-read")
                        .show_dialog(false)
                        .build();
                URI authCodeUri = authCodeUriRequest.execute();
                System.out.println(authCodeUri.toString());
                System.out.println("Please navigate to the above URL, and paste everything after \"/?code\" here");
                Scanner scanner = new Scanner(System.in);
                authRequestCode = scanner.next();
            }
            authCred = spotifyApi.authorizationCode(authRequestCode).build().execute();
            spotifyApi.setAccessToken(authCred.getAccessToken());
            spotifyApi.setRefreshToken(authCred.getRefreshToken());
        } catch (SpotifyWebApiException | IOException swae) {
            System.out.println("Exception getting auth token");
            System.out.println(swae.toString());
            System.exit(3);
        }
    }

}
