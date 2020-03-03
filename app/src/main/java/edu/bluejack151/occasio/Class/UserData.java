package edu.bluejack151.occasio.Class;

import java.io.Serializable;

/**
 * Created by Domus on 12/23/2015.
 */
public class UserData implements Serializable {
    String userId;
    String provider;
    String username;
    String email;
    String profileImageURL;
    String imageString;
    String password;

    public UserData() {
    }

    public UserData(String userId, String provider, String username, String email, String profileImageURL, String imageString, String password) {
        this.userId = userId;
        this.provider = provider;
        this.username = username;
        this.email = email;
        this.profileImageURL = profileImageURL;
        this.imageString = imageString;
        this.password = password;
    }

    public String getUserId() {
        if (userId != null)
            return userId;
        else return "";
    }

    public String getProvider() {
        if (provider != null)
            return provider;
        else return "";
    }

    public String getUsername() {
        if (username != null)
            return username;
        else return "";
    }

    public String getEmail() {
        if (email != null)
            return email;
        else return "";
    }

    public String getProfileImageURL() {
        if (profileImageURL != null)
            return profileImageURL;
        else return "";
    }

    public String getImageString() {
        if (imageString != null)
            return imageString;
        else return "";
    }

    public String getPassword() {
        if (password != null)
            return password;
        else return "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public void setImageString(String imageString) {
        this.imageString = imageString;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
