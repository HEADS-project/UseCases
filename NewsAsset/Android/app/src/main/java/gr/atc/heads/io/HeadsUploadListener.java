package gr.atc.heads.io;

/**
 * Created by kGiannakakis on 12/6/2014.
 */
public interface HeadsUploadListener {

    void uploadFailed(String message);
    void uploadCompleted(String packageId);

}
