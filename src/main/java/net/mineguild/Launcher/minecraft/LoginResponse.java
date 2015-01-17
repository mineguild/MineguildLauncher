package net.mineguild.Launcher.minecraft;

import lombok.Getter;
import lombok.Setter;

import com.mojang.authlib.UserAuthentication;

public class LoginResponse {

    @Getter private UserAuthentication auth;
    @Getter @Setter private boolean startedGame = false;

    /**
     * Constructor for LoginResponse class
     *
     * @param version  - the version from authlib
     * @param dlTicket - the ticket from authlib
     * @param username - the username from authlib
     * @param session  - the session ID from authlib
     * @param uniqueID - the user's uuid from authlib
     */
    public LoginResponse(String version, String dlTicket, String username, String session,
        String uniqueID, UserAuthentication userAuth) {
        this.latestVersion = version;
        this.downloadTicket = dlTicket;
        this.username = username;
        this.sessionID = session;
        this.uuid = uniqueID;
        this.auth = userAuth;
    }

    /**
     * Used to grab the latest version of minecraft from response string
     *
     * @return - the latest version of minecraft
     */
    @Getter private String latestVersion;

    /**
     * Used to grab the download ticket from response string
     *
     * @return - the download ticket for minecraft
     */
    @Getter private String downloadTicket;

    /**
     * Used to grab the username from response string
     *
     * @return - the username of the user
     */
    @Getter private String username;

    /**
     * Used to grab the session ID from response string
     *
     * @return - the session ID of the minecraft instance
     */
    @Getter private String sessionID;

    /**
     * Used to grab the user's uuid from response string
     *
     * @return - the uuid of the user
     */
    @Getter private String uuid;

}
