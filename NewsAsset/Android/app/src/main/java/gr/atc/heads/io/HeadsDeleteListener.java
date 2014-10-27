package gr.atc.heads.io;

/**
 * Created by kGiannakakis on 24/6/2014.
 */
public interface HeadsDeleteListener {
    void deleteFailed(String message);

    void deleteSuccessful();
}
