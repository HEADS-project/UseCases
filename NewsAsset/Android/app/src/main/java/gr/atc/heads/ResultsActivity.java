package gr.atc.heads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.List;

import gr.atc.common.location.LocationAwareActivity;
import gr.atc.heads.fragments.ExtMapFragment;
import gr.atc.heads.fragments.PackageFragment;
import gr.atc.heads.fragments.ResultsListFragment;
import gr.atc.heads.model.HeadsPoint;

public class ResultsActivity extends ActionBarActivity implements PackageFragment.PackageFragmentListener {

    public static final String PACKAGES_PARAM = "packages";
    public static final String LOCATION_PARAM = "location";
    private static final String SHOW_MAP_PARAM = "showMap";

    private ResultsListFragment listFragment;

    private ExtMapFragment mapFragment;

    private Location location;

    private boolean isShowingMap;

    private List<HeadsPoint> packages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            packages = (List<HeadsPoint>) extras.getSerializable(PACKAGES_PARAM);
            location = (Location) extras.getParcelable(LOCATION_PARAM);
            isShowingMap = false;
        }
        else {
            packages = (List<HeadsPoint>) savedInstanceState.getSerializable(PACKAGES_PARAM);
            location = (Location) savedInstanceState.getParcelable(LOCATION_PARAM);
            isShowingMap = savedInstanceState.getBoolean(SHOW_MAP_PARAM);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PACKAGES_PARAM, (Serializable) packages);
        outState.putParcelable(LOCATION_PARAM, location);
        outState.putBoolean(SHOW_MAP_PARAM, isShowingMap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (((HeadsApplication) getApplication()).getUser() == null) {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);

            // remove the activity from the back stack
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else {
            listFragment = ResultsListFragment.newInstance(packages, false);
            mapFragment = ExtMapFragment.newInstance(packages, location, false);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, isShowingMap ? mapFragment : listFragment)
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportFragmentManager().beginTransaction()
                .remove(isShowingMap ? mapFragment : listFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.results, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_map).setVisible(!isShowingMap);
        menu.findItem(R.id.action_list).setVisible(isShowingMap);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_list) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, listFragment)
                    .commit();
            isShowingMap = false;
            supportInvalidateOptionsMenu();
            return true;
        }
        if (id == R.id.action_map) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mapFragment)
                    .commit();
            isShowingMap = true;
            supportInvalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getSupportParentActivityIntent () {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String updateIntervalStr = prefs.getString("location_update_frequency", "60");
        int updateInterval = 60;
        updateInterval = Integer.parseInt(updateIntervalStr);
        boolean tabbedNavigation = prefs.getBoolean("tabbed_navigation", true);

        Intent intent;

        if (tabbedNavigation) {
            intent  = new Intent(this, MainTabsActivity.class);
        }
        else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.putExtra(LocationAwareActivity.UPDATE_INTERVAL_PARAM, updateInterval);
        return intent;
    }

    @Override
    public void onFormIsBusy() {

    }

    @Override
    public void onPackageDeleted(HeadsPoint HeadsPackage) {

    }

    @Override
    public void onDeleteFailed(String message) {

    }

    @Override
    public void onLocalPackageUploadCompleted(HeadsPoint HeadsPackage) {

    }

    @Override
    public void onUploadFailed(String message) {

    }
}
