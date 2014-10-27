package gr.atc.heads;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import gr.atc.common.imagepicker.ImagePickerHelper;
import gr.atc.common.location.LocationAwareActivity;
import gr.atc.common.utils.Utils;
import gr.atc.heads.dialogs.DateRangeDialog;
import gr.atc.heads.fragments.AccountFragment;
import gr.atc.heads.fragments.PackageFragment;
import gr.atc.heads.fragments.SearchFragment;
import gr.atc.heads.fragments.UploadFragment;
import gr.atc.heads.fragments.UserPackagesFragment;
import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.TagModel;


public class MainActivity extends LocationAwareActivity implements
        SearchFragment.SearchFragmentListener,
        UploadFragment.UploadFragmentListener,
        PackageFragment.PackageFragmentListener,
        UserPackagesFragment.UserPackagesFragmentListener,
        DateRangeDialog.DateRangeDialogListener {

    private final int UPLOAD_FRAGMENT_INDEX = 0;
    private final int SEARCH_FRAGMENT_INDEX = 1;
    private final int USER_PACKAGES_FRAGMENT_INDEX = 2;
    private final int ACCOUNT_FRAGMENT_INDEX = 3;

    private final String POSITION_PARAM = "position";

    private final static String ALBUM_NAME = "Heads";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mNavigationTitles;

    private SearchFragment mSearchFragment;
    private UploadFragment mUploadFragment;
    private UserPackagesFragment mUserPackagesFragment;

    private Fragment currentFragment;

    private ImagePickerHelper mImagePickerHelper;

    private ProgressDialog mProgressDialog;
    private AlertDialog networkAlertDialog;
    private AlertDialog deleteWarningDialog;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImagePickerHelper = new ImagePickerHelper(this);
        mImagePickerHelper.setSelectSourceLabel(getString(R.string.select_image_source));

        if (savedInstanceState != null) {
            mImagePickerHelper.setImagePath(savedInstanceState.getString("imagePath"));
            mImagePickerHelper.setImageUri((Uri) savedInstanceState.getParcelable("imageUri"));
        }


        mTitle = mDrawerTitle = getTitle();
        mNavigationTitles = getResources().getStringArray(R.array.navigation_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mNavigationTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            position = 0;
            selectItem(0);
        }
        else {
            position = savedInstanceState.getInt(POSITION_PARAM);
            selectItem(position);
        }
    }

    @Override
    public void onDestroy() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(currentFragment).commitAllowingStateLoss();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (((HeadsApplication) getApplication()).getUser() == null) {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);

            // remove the activity from the back stack
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        if (deleteWarningDialog != null) {
            deleteWarningDialog.dismiss();
        }

        if (networkAlertDialog != null) {
            networkAlertDialog.dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("imagePath", mImagePickerHelper.getImagePath());
        bundle.putParcelable("imageUri", mImagePickerHelper.getImageUri());
        bundle.putInt(POSITION_PARAM, position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isDrawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        boolean isPackagesFragmentVisible = position == USER_PACKAGES_FRAGMENT_INDEX;
        boolean isShowingMap = false;

        menu.findItem(R.id.action_account).setVisible(false);

        List<HeadsPoint> userPackages = null;
        if (isPackagesFragmentVisible) {
            userPackages = mUserPackagesFragment.getPackages();
            isShowingMap = mUserPackagesFragment.isShowingMap();
        }

        menu.findItem(R.id.action_map).setVisible(!isDrawerOpen && isPackagesFragmentVisible
                            && !isShowingMap && userPackages != null);
        menu.findItem(R.id.action_list).setVisible(!isDrawerOpen && isPackagesFragmentVisible
                            && isShowingMap && userPackages != null);
        menu.findItem(R.id.action_delete).setVisible(!isDrawerOpen && isPackagesFragmentVisible
                && !isShowingMap && userPackages != null && mUserPackagesFragment.isDetailsViewVisible());
        menu.findItem(R.id.action_upload).setVisible(!isDrawerOpen && isPackagesFragmentVisible
                && !isShowingMap && userPackages != null && mUserPackagesFragment.isDetailsViewVisible()
                && mUserPackagesFragment.isUploadAllowed());
        menu.findItem(R.id.action_refresh).setVisible(!isDrawerOpen && isPackagesFragmentVisible);

        menu.findItem(R.id.action_ok).setVisible(!isDrawerOpen &&
                                                 position == SEARCH_FRAGMENT_INDEX &&
                                                 mSearchFragment.isShowingMap());
        menu.findItem(R.id.action_refresh).setVisible(isPackagesFragmentVisible);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent intent= new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_list:
                if (mUserPackagesFragment != null) {
                    mUserPackagesFragment.showList();
                    supportInvalidateOptionsMenu();
                }
                return true;
            case R.id.action_map:
                if (isNetworkAvailable(R.string.network_warning) && mUserPackagesFragment != null) {
                    mUserPackagesFragment.showMap();
                    supportInvalidateOptionsMenu();
                }
                return true;
            case R.id.action_ok:
                mSearchFragment.confirmMapNewLocation();
                return true;
            case R.id.action_delete:
                if (isNetworkAvailable(R.string.network_warning)) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                    alertDialogBuilder.setTitle(R.string.delete_item);
                    alertDialogBuilder
                            .setMessage(R.string.delete_warning_message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (mUserPackagesFragment != null) {
                                        onFormIsBusy();
                                        mUserPackagesFragment.requestPackageDeletion();
                                    }
                                    deleteWarningDialog = null;
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    deleteWarningDialog = null;
                                }
                            });
                    deleteWarningDialog = alertDialogBuilder.create();
                    deleteWarningDialog.show();
                }
                return true;
            case R.id.action_upload:
                if (isNetworkAvailable(R.string.network_warning) && mUserPackagesFragment != null) {
                    onFormIsBusy();
                    mUserPackagesFragment.upload();
                }
                return true;
            case R.id.action_refresh:
                if (mUserPackagesFragment != null) {
                    mUserPackagesFragment.refresh();
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        this.position = position;

        HeadsApplication app = (HeadsApplication) getApplication();
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case UPLOAD_FRAGMENT_INDEX:
                if (mUploadFragment ==  null) {
                    mUploadFragment = UploadFragment.newInstance(currentLocation,
                                                               currentAddress,
                                                               mImagePickerHelper.getImagePath(),
                                                               mImagePickerHelper.getImageUri());
                }
                fragment = mUploadFragment;
                break;
            case SEARCH_FRAGMENT_INDEX:
                if (mSearchFragment ==  null) {
                    mSearchFragment = SearchFragment.newInstance(currentLocation, currentAddress);
                }
                fragment = mSearchFragment;
                break;
            case USER_PACKAGES_FRAGMENT_INDEX:
                if (mUserPackagesFragment == null) {
                    mUserPackagesFragment = UserPackagesFragment.newInstance(null, currentLocation);
                }
                fragment = mUserPackagesFragment;
                break;
            case ACCOUNT_FRAGMENT_INDEX:
                fragment = AccountFragment.newInstance();
                break;
            default:
                break;
        }
        /*Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);*/

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            currentFragment = fragment;
        }

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mNavigationTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onLocationUpdated(Location location) {
        Log.d("MainActivity", location.getLatitude() + ", " + location.getLongitude());
        int position = mDrawerList.getCheckedItemPosition();
        if (position == UPLOAD_FRAGMENT_INDEX && mUploadFragment != null) {
            mUploadFragment.onLocationUpdated(location);
        }
        if (position == SEARCH_FRAGMENT_INDEX && mSearchFragment != null) {
            mSearchFragment.onLocationUpdated(location);
        }
    }

    @Override
    protected void onAddressFound(String address) {
        Log.d("MainActivity", address);
        int position = mDrawerList.getCheckedItemPosition();
        if (position == UPLOAD_FRAGMENT_INDEX && mUploadFragment != null) {
            mUploadFragment.onAddressUpdated(address);
        }
        if (position == SEARCH_FRAGMENT_INDEX && mSearchFragment != null) {
            mSearchFragment.onAddressUpdated(address);
        }
    }

    @Override
    public void onFormIsBusy() {
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.waiting));
    }

    @Override
    public boolean isNetworkAvailable(int warningMessageResId) {
        if (Utils.isNetworkAvailable(this)) {
            return true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getText(R.string.note))
                .setMessage(getText(warningMessageResId))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        networkAlertDialog = null;
                    }
                });

        networkAlertDialog = builder.create();
        networkAlertDialog.show();

        return false;
    }

    @Override
    public void onSelectDateRange(int fromYear, int fromMonth, int fromDay,
                                  int toYear, int toMonth, int toDay) {
        FragmentManager fm = getSupportFragmentManager();
        DateRangeDialog dateRangeDialog = DateRangeDialog.newInstance(fromYear, fromMonth, fromDay,
                toYear, toMonth, toDay);
        dateRangeDialog.show(fm, "fragment_date_range");
    }

    @Override
    public void onDateRangeSelected(int fromYear, int fromMonth, int fromDay,
                                    int toYear, int toMonth, int toDay) {
        if (mSearchFragment != null) {
            mSearchFragment.updateDateRange(fromYear, fromMonth, fromDay,
                    toYear, toMonth, toDay);
        }
    }

    @Override
    public void onPickImage() {
        if (!mImagePickerHelper.openImageIntent(ALBUM_NAME)) {
            Crouton.makeText(this, getString(R.string.an_error_occurred), Style.ALERT).show();
        }
    }

    @Override
    public void onSearchStarted() {
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.waiting));
    }

    @Override
    public void onSearchCompleted(List<HeadsPoint> resultPackageList) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        Intent results = new Intent(this, ResultsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ResultsActivity.PACKAGES_PARAM, (Serializable) resultPackageList);
        bundle.putParcelable(ResultsActivity.LOCATION_PARAM, currentLocation);
        results.putExtras(bundle);
        startActivity(results);
    }

    @Override
    public void onSearchFailed(String message) {
        onError(message);
    }

    @Override
    public void onUploadStarted() {
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.waiting));
    }

    @Override
    public void onLocalPackageAdded(HeadsPoint localPackage) {

    }

    @Override
    public void onUploadCompleted(HeadsPoint HeadsPackage) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mImagePickerHelper.setImagePath(null);
        mImagePickerHelper.setImageUri(null);

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.PACKAGE_PARAM, HeadsPackage);
        startActivity(intent);
    }

    @Override
    public void onLocalPackageUploadCompleted(HeadsPoint HeadsPackage) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        ((HeadsApplication) getApplication()).deleteLocalPackage(HeadsPackage);
        mUserPackagesFragment.updatePackage(HeadsPackage);
    }

    @Override
    public void onUploadFailed(String message) {
        onError(message);
    }

    @Override
    public void onPackageDeleted(HeadsPoint HeadsPackage) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        Log.d("MainActivity", "Package " + HeadsPackage.getId() + " deleted!");
        mUserPackagesFragment.removePackage(HeadsPackage);
    }

    @Override
    public void onDeleteFailed(String message) {
        onError(message);
    }

    @Override
    public void onLocationSelect(Location location) {
        Intent intent = new Intent(this, LocationSelectActivity.class);
        intent.putExtra(LocationSelectActivity.LOCATION_PARAM, currentLocation);
        startActivityForResult(intent, HeadsApplication.SELECT_LOCATION_REQUEST_CODE);
    }

    private void onError(String message) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (message == null) {
            message = getString(R.string.an_error_occurred);
        }
        Crouton.makeText(this, message, Style.ALERT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK) {

            if (requestCode == HeadsApplication.SELECT_LOCATION_REQUEST_CODE) {
                Location newLocation = data.getParcelableExtra(LocationSelectActivity.LOCATION_PARAM);
                int position = mDrawerList.getCheckedItemPosition();
                if (position == UPLOAD_FRAGMENT_INDEX && mUploadFragment != null) {
                    mUploadFragment.onLocationFromMapUpdated(newLocation);
                }
                if (position == SEARCH_FRAGMENT_INDEX && mSearchFragment != null) {
                    mSearchFragment.onLocationFromMapUpdated(newLocation);
                }
            }
            else if (requestCode == HeadsApplication.TAG_SELECT_REQUEST_CODE) {
                List<TagModel> tags = (List<TagModel>)
                        data.getSerializableExtra(TagSelectActivity.TAGS_PARAM);
                if (position == UPLOAD_FRAGMENT_INDEX && mUploadFragment != null) {
                    mUploadFragment.onTagsUpdated(tags);
                }
                if (position == SEARCH_FRAGMENT_INDEX && mSearchFragment != null) {
                    mSearchFragment.onTagsUpdated(tags);
                }
            }
            else if (requestCode == HeadsApplication.PACKAGES_EDIT_REQUEST_CODE) {
                HeadsPoint HeadsPackage = (HeadsPoint)
                        data.getSerializableExtra(DetailsActivity.PACKAGE_PARAM);

                int actionCode = data.getIntExtra(DetailsActivity.ACTION_CODE, 0);
                if (actionCode == DetailsActivity.PACKAGE_DELETED_CODE) {
                    onPackageDeleted(HeadsPackage);
                }
                else if (actionCode == DetailsActivity.PACKAGE_ADDED_CODE) {
                    onLocalPackageUploadCompleted(HeadsPackage);
                }
            }
            else {
                String imagePath = mImagePickerHelper.getImagePathFromActivityResult(requestCode, data);
                Uri imageUri = mImagePickerHelper.getImageUriFromActivityResult(requestCode, data);
                if (!mUploadFragment.onImageUriSelected(imageUri)) {
                    mUploadFragment.onImageSelected(imagePath);
                }
            }
        }
    }

}
