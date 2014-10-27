package gr.atc.heads.io;

import java.util.List;

import gr.atc.heads.model.Tag;
import gr.atc.heads.model.User;

/**
 * Created by kGiannakakis on 6/6/2014.
 */
public interface HeadsLoginListener {

    void loginFailed(String message);

    void loginSuccessful(User user);

    void registerSuccessful();

    void tagsReceived(List<Tag> tags);
}
