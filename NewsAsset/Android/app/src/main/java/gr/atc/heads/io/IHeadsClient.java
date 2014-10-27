package gr.atc.heads.io;

import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.HeadsPointQuery;

/**
 * Created by kGiannakakis on 20/6/2014.
 */
public interface IHeadsClient {

    void setHeadsLoginListener(HeadsLoginListener HeadsLoginListener);

    void setHeadsSearchListener(HeadsSearchListener HeadsSearchListener);

    void setHeadsUploadListener(HeadsUploadListener HeadsUploadListener);

    void setHeadsDeleteListener(HeadsDeleteListener HeadsDeleteListener);

    String getThumbnailUrl(String id);

    String getImageUrl(String id);

    void startLogin(String username, String password);

    void startRegister(String username, String password);

    void requestTags();

    void startUpload(String userId, HeadsPoint point);

    void startSearch(HeadsPointQuery query);

    void requestUserPackages(String userId);

    void requestPackageDeletion(String packageId);
}
