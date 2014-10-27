package gr.atc.heads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import gr.atc.common.location.LocationAwareActivity;

public class LocationSelectActivity extends ActionBarActivity {

    public final static String LOCATION_PARAM = "location";

    private GoogleMap map;
    private SupportMapFragment mapFragment;

    private Location location;

    private String formType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_select);

        FragmentManager fm = getSupportFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            location = extras.getParcelable(LOCATION_PARAM);
        }
        else {
            location = savedInstanceState.getParcelable(LOCATION_PARAM);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LOCATION_PARAM, location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            Intent returnIntent = new Intent();

            Location newLocation;
            if (location != null) {
                newLocation= new Location(location);
            }
            else {
                newLocation = new Location("");
            }
            if (map == null || map.getCameraPosition() == null ||
                    map.getCameraPosition().target == null) {
                finish();
                return true;
            }

            newLocation.setLatitude(map.getCameraPosition().target.latitude);
            newLocation.setLongitude(map.getCameraPosition().target.longitude);
            returnIntent.putExtra(LOCATION_PARAM, newLocation);
            setResult(RESULT_OK, returnIntent);
            finish();
            return true;
        }
        else if (id == R.id.action_cancel) {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        map = mapFragment.getMap();
        if (map != null) {
            map.setMyLocationEnabled(true);

            if (location != null) {
                float zoomLevel = 14.0f;
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()),
                        zoomLevel));
            }
        }

        Animation anim = AnimationUtils.loadAnimation(this,
                                 R.anim.window_fade);
        findViewById(R.id.help_window).startAnimation(anim);
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
}
