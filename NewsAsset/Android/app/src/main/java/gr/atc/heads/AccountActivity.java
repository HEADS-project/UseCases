package gr.atc.heads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import gr.atc.common.location.LocationAwareActivity;


public class AccountActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
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
