package gr.atc.heads.model;

import com.google.gson.Gson;

/**
*
* @author Manolis
*/
public class User 
{
    String userName;
    String password;
    String email;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    String displayName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User(String username) {
        this.userName = username;
    }

    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    static public User create(String serializedData) {
        Gson gson = new Gson();
        return gson.fromJson(serializedData, User.class);
    }
}
