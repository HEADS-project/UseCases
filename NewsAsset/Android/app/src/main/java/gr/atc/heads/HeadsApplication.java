package gr.atc.heads;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.Tag;
import gr.atc.heads.model.User;

/**
 * Created by kGiannakakis on 6/6/2014.
 */
@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        formUri = "http://hestia.atc.gr/heads_acra.php",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)
public class HeadsApplication extends Application {

    private final static String LOCAL_PACKAGES_KEY = "LocalPackages";

    public final static int SELECT_LOCATION_REQUEST_CODE = 2202;
    public final static int TAG_SELECT_REQUEST_CODE = 30020;
    public final static int PACKAGES_EDIT_REQUEST_CODE = 22222;

    private User user;

    private List<Tag> tags;

    private List<HeadsPoint> localPackages;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Tag> getTags() {
        if (tags == null) {
            return new ArrayList<Tag>();
        }
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<HeadsPoint> getLocalPackages() {
        return localPackages;
    }

    public void setLocalPackages(List<HeadsPoint> localPackages) {
        this.localPackages = localPackages;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        ACRA.init(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String localPackagesStr = prefs.getString(LOCAL_PACKAGES_KEY, null);
        if (localPackagesStr != null) {
            try {
                Type packagesListType = new TypeToken<ArrayList<HeadsPoint>>() {
                }.getType();
                Gson gson = new GsonBuilder()
                        .create();
                localPackages = gson.fromJson(localPackagesStr, packagesListType);
            }
            catch (Exception ex) {
                Log.w("HeadsApplication", ex.getMessage());
            }
        }

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .build();
        ImageLoader.getInstance().init(config);
    }

    public void addLocalPackage(HeadsPoint localPackage) {
        if (localPackages == null) {
            localPackages = new ArrayList<HeadsPoint>();
        }
        localPackages.add(localPackage);
        storePackages();
    }

    public void deleteLocalPackage(HeadsPoint localPackage) {
        if (localPackages != null) {
            HeadsPoint deletedPackage = null;
            for(HeadsPoint pkg: localPackages) {
                if (pkg.getImagePath() != null && pkg.getImagePath().equals(localPackage.getImagePath())) {
                    deletedPackage = pkg;
                    break;
                }
                if (pkg.getImageUri() != null && pkg.getImageUri().equals(localPackage.getImageUri())) {
                    deletedPackage = pkg;
                    break;
                }
            }
            if (deletedPackage != null) {
                localPackages.remove(deletedPackage);
                storePackages();
            }
        }
    }

    private void storePackages() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new GsonBuilder()
                .create();
        editor.putString(LOCAL_PACKAGES_KEY,  gson.toJson(localPackages));
        editor.commit();
    }
}
