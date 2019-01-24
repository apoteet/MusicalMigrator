package service;

import com.github.felixgail.gplaymusic.util.TokenProvider;
import svarzee.gps.gpsoauth.AuthToken;
import svarzee.gps.gpsoauth.Gpsoauth;

import java.io.IOException;

public class AuthTokenProviderService {
    private static AuthToken authToken;

    public static String getUsername() {
        return USERNAME;
    }

    public static String getAndroidId() {
        return ANDROID_ID;
    }

    private static String USERNAME;
    private static String PASSWORD;
    private static String ANDROID_ID;

    public static AuthToken getAuthToken() throws IOException {

        if (authToken == null || authToken.getExpiry() != -1) {
            try {
                authToken = TokenProvider.provideToken(USERNAME, PASSWORD, ANDROID_ID);
            } catch (Gpsoauth.TokenRequestFailed trf) {
                System.out.println(String.format("Token request failed. Error : %s", trf));
                return null;
            }
        }

        return authToken;
    }

    public static void setAuthParams(String username, String password, String androidId) {
        USERNAME = username;
        PASSWORD = password;
        ANDROID_ID = androidId;
    }
}
