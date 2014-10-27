package gr.atc.heads.io;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.Tag;
import gr.atc.heads.model.User;

/**
 * Created by kGiannakakis on 6/6/2014.
 */
public class HeadsClientMessenger {

    public static final int FAILURE = -1;
    public static final int REGISTER_SUCCESSFUL = 0;
    public static final int LOGIN_SUCCESSFUL = 1;
    public static final int TAGS_RECEIVED = 2;
    public static final int SEARCH_COMPLETED = 3;
    public static final int UPLOAD_COMPLETED = 4;
    public static final int DELETE_COMPLETED = 5;

    private final String failureMessageKey = "message";
    private final String usernameKey = "username";
    private final String displayNameKey = "displayName";
    private final String emailKey = "email";

    private final String tagsKey = "user";

    private final String resultsKey = "results";

    private final String packageIdKey = "packageId";

    private Handler handler;

    public HeadsClientMessenger(Handler handler) {
        this.handler = handler;
    }

    public void sendFailureMessage(String message) {
        Message msg = new Message();
        msg.what = HeadsClientMessenger.FAILURE;
        Bundle bundle = new Bundle();
        bundle.putString(failureMessageKey, message);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public String getFailureMessage(Message msg) {
        return msg.getData().getString(failureMessageKey);
    }

    public void sendTagsMessage(List<Tag> tags) {
        sendMessage(HeadsClientMessenger.TAGS_RECEIVED, tagsKey, (Serializable) tags);
    }

    public List<Tag> getTags(Message msg) {
        return (List<Tag>) msg.getData().getSerializable(tagsKey);
    }

    public void sendResultsMessage(List<HeadsPoint> results) {
        sendMessage(HeadsClientMessenger.SEARCH_COMPLETED, resultsKey, (Serializable) results);
    }

    public List<HeadsPoint> getResults(Message msg) {
        return (List<HeadsPoint>) msg.getData().getSerializable(resultsKey);
    }

    public void sendUploadMessage(String packageId) {
        sendMessage(HeadsClientMessenger.UPLOAD_COMPLETED, packageIdKey, packageId);
    }

    public String getPackageId(Message msg) {
        return msg.getData().getString(packageIdKey);
    }

    public void sendRegisterSuccessMessage() {
        sendEmptyMessage(REGISTER_SUCCESSFUL);
    }

    public void sendLoginSuccessMessage(String username, String displayname, String email) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(usernameKey, username);
        params.put(displayNameKey, displayname);
        params.put(emailKey, email);

        sendMessage(LOGIN_SUCCESSFUL, params);
    }

    public User getUser(Message msg) {
        String username = msg.getData().getString(usernameKey);
        String displayName = msg.getData().getString(displayNameKey);
        String email = msg.getData().getString(emailKey);

        User user = new User(username);
        user.setEmail(email);
        user.setDisplayName(displayName);

        return user;
    }

    public void sendDeleteCompletedMessage() {
        sendEmptyMessage(DELETE_COMPLETED);
    }

    private void sendMessage(int code, Map<String, String> params) {
        Message msg = new Message();
        msg.what = code;
        Bundle bundle = new Bundle();
        for(String key: params.keySet()) {
            bundle.putString(key, params.get(key));
        }
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void sendMessage(int code, String key, Serializable object) {
        Message msg = new Message();
        msg.what = code;
        Bundle bundle = new Bundle();
        bundle.putSerializable(key, object);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void sendEmptyMessage(int code) {
        Message msg = new Message();
        msg.what = code;
        handler.sendMessage(msg);
    }
}
