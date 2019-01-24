package com.garbageTier.main;

import com.garbageTier.store.TrackStore;
import com.github.felixgail.gplaymusic.model.Playlist;
import com.github.felixgail.gplaymusic.model.PlaylistEntry;
import com.github.felixgail.gplaymusic.model.Track;

import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import javafx.util.Pair;
import com.garbageTier.service.AuthTokenProviderService;
import com.garbageTier.service.BaseGPlayService;
import com.garbageTier.service.BaseSpotifyService;
import com.garbageTier.util.StupidDotPrinter;

import java.io.*;
import java.util.*;

public class MusicalMigrator {
    public static void main(String[] args) {
        printIntro();

        //login
        promptAndSetCredentials();

        //get base data to work with
        TrackStore store = new TrackStore();
        fetchAllGplaySongs(store);

        debugResults(store.getAllGoogleTracks(), "Tracks pulled from google play:");

        BaseSpotifyService spotifyApi = new BaseSpotifyService();

        List<Track> tracksNotAdded = spotifyApi.addTracksToLibrary(store);
        if(tracksNotAdded.size() > 0) {
            debugResults(tracksNotAdded, "Couldn't find spotify equivalents for the following tracks:");
        }

        store.getAllGooglePlaylists().forEach(playlist -> {
            System.out.println("Creating playlist " + playlist.getName());
            com.wrapper.spotify.model_objects.specification.Playlist newPlaylist = spotifyApi.createPlaylist(playlist);
            store.putPlaylist(newPlaylist);
            System.out.println("Playlist created. Adding songs to playlist");
            List<com.wrapper.spotify.model_objects.specification.Track> playlistTracks = store.getSpotifyTracksForGooglePlaylist(playlist);

            List<String> skippedTracks = spotifyApi.addTracksToPlaylist(newPlaylist, playlistTracks);
            if(skippedTracks.size() > 0) {
                System.out.println("Skipped adding the following tracks to playlist " + newPlaylist.getName());
                skippedTracks.forEach(System.out::println);
            }
        });
    }
    
    private static boolean promptAndSetCredentials() {
        Scanner scanner = new Scanner(System.in);
        String username = "";
        String imei = "";
        String password;
        boolean credsPreloaded = false;

        Pair<String, String> userImei = readUserImeiPair();
        if (userImei.getKey() != null && userImei.getValue() != null) {
            System.out.println("Credentials read from authData file");

            username = userImei.getKey();
            imei = userImei.getValue();
            credsPreloaded = true;
        }

        if (!credsPreloaded) {
            System.out.print("Gmail account: ");
            username = scanner.next();

            System.out.println("Your device IMEI can be found by opening the dialer on your phone, and dialing *#06#");
            System.out.println("It should show up right away. Other ways if that doesn't work: https://www.wikihow.com/Find-the-IMEI-or-MEID-Number-on-a-Mobile-Phone");
            System.out.print("IMEI (should be 15 characters): ");
            imei = scanner.next();
            if (imei.length() != 15) {
                System.out.println("Not 15 characters but whatever, maybe you're special.");
            }
        }

        Console secureConsole = System.console();
        if (secureConsole == null) {
            System.out.println("Secure password terminal couldn't initialize, sorry bub.");
            System.out.print("Please enter your password in cleartext: ");
            password = scanner.next();
        } else {
            char[] passwordData = System.console().readPassword("Password: ");
            password = new String(passwordData);
        }

        AuthTokenProviderService.setAuthParams(username, password, imei);
        System.out.println("Credentials Set");
        return true;
    }

    private static void fetchAllGplaySongs(TrackStore store) {
        try {
            BaseGPlayService googleApi = new BaseGPlayService();
            StupidDotPrinter dots = new StupidDotPrinter(System.out, 1000);
            Thread dotThread = new Thread(dots);
            System.out.print("Fetching library");
            dotThread.start();

            List<Track> myOwnedSongs = googleApi.getTrackApi().getLibraryTracks();
            myOwnedSongs.forEach(store::putTrack);

            List<Playlist> playlistlist = googleApi.getPlaylistApi().listPlaylists();
            playlistlist.forEach(playlist -> {
                try {
                    store.putPlaylist(playlist);
                    List<PlaylistEntry> playlistSongs = playlist.getContents(999);
                    playlistSongs.forEach(entry -> {
                        try {
                            Track thisTrack = entry.getTrack();
                            store.putAssociation(playlist, thisTrack);
                        } catch (IllegalArgumentException | IOException iae) {
                            System.out.println(iae.toString());
                        }
                    });
                } catch (IOException ieo) {
                    System.out.println(ieo.toString());
                }
            });

            dots.stop();
        } catch (Exception e) {
            System.out.println("Error fetching music info from google play:");
            System.out.println(e.toString());
        }
    }

    private static Pair<String, String> readUserImeiPair() {
        String filename = "authData";
        BufferedReader buffReader = null;

        try (FileReader reader = new FileReader(filename)) {
            buffReader = new BufferedReader(reader);

            String username = buffReader.readLine();
            String imei = buffReader.readLine();

            return new Pair<>(username, imei);
        } catch (IOException ioe) {
//            System.out.println("authData file not found. In the future, you can create a " +
//                    "file in the same directory as this jarfile and call it \"authData\". ");
//            System.out.println("The file is just two lines, and looks like: ");
//            System.out.println("<email>");
//            System.out.println("<IMEI>");
        }
        return new Pair<>(null, null);
    }

    private static void saveUserImei(String username, String imei) {
        System.out.println("Saving new email");

        try (FileWriter writer = new FileWriter("authData")) {
            writer.write(username);
            writer.write(imei);
        } catch (IOException ioe) {
            System.out.println("Error writing to file");
            System.out.print(ioe.toString());
        }
    }

    private static void printIntro() {
        System.out.println("MusicalMigrator - @Alek Poteet");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("This tool will automatically pull your songs from Google Play Music library and playlists, " +
                "and search those songs to add to Spotify. To get started, your google account username (email), password, " +
                "and the IMEI of an android device that had GooglePlayMusic installed.\n ");
        System.out.println("If you have 2 factor authentication, you'll also need to do some extra stuff. " +
                "The quickest solution is to temporarily allow \"less secure apps\" to access your account. " +
                "I would follow the link below but then re-enable security after using this tool\n");
        System.out.println("https://support.google.com/accounts/answer/6010255");
        System.out.println("-----------------------------------------------------------------------------------");
    }

    private static void debugResults(List<Track> trackList, String header) {
        System.out.println(header);
        //Debug results
        trackList.sort(new Comparator<>() {
            @Override
            public int compare(Track o1, Track o2) {
                return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
            }
        });
        trackList.forEach(track -> System.out.println(String.format("%s - %s - %s", track.getTitle(), track.getArtist(), track.getAlbum())));
        System.out.println("Total count: " + trackList.size());
    }
}
