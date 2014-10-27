package gr.atc.heads;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import gr.atc.common.imagepicker.ImagePickerHelper;
import gr.atc.common.location.LocationAwareActivity;
import gr.atc.common.utils.Utils;
import gr.atc.heads.dialogs.DateRangeDialog;
import gr.atc.heads.fragments.PackageFragment;
import gr.atc.heads.fragments.SearchFragment;
import gr.atc.heads.fragments.UploadFragment;
import gr.atc.heads.fragments.UserPackagesFragment;
import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.TagModel;

public class MainTabsActivity extends LocationAwareActivity implements
        ActionBar.TabListener,
        SearchFragment.SearchFragmentListener,
        UploadFragment.UploadFragmentListener,
        UserPackagesFragment.UserPackagesFragmentListener,
        PackageFragment.PackageFragmentListener,
        DateRangeDialog.DateRangeDialogListener{

    private static final String IMAGE_PATH_PARAM = "imagePath";
    private static final String IMAGE_URI_PARAM = "imageUri";
    private static final String UPLOADED_PACKAGES_PARAM = "uploadedPackages";

    private final int UPLOAD_FRAGMENT_INDEX = 0;
    private final int SEARCH_FRAGMENT_INDEX = 1;
    private final int USER_PACKAGES_FRAGMENT_INDEX = 2;

    private final int TABS_COUNT = 3;

    private final static String ALBUM_NAME = "Heads";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private HeadsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private ProgressDialog mProgressDialog;

    private AlertDialog deleteWarningDialog;
    private AlertDialog networkAlertDialog;

    private ImagePickerHelper mImagePickerHelper;

    private Location location;
    private Location uploadLocationFromMap;
    private Location searchLocationFromMap;
    private String address;
    private List<TagModel> searchTags;
    private List<TagModel> uploadTags;

    private HeadsPoint deletedPackage;
    private HeadsPoint localPackageUploaded;
    private List<HeadsPoint> uploadedPackages;

    private NetworkDialogListener networkDialogListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs_main);

        mImagePickerHelper = new ImagePickerHelper(this);
        mImagePickerHelper.setSelectSourceLabel(getString(R.string.select_image_source));

        if (savedInstanceState != null) {
            mImagePickerHelper.setImagePath(savedInstanceState.getString(IMAGE_PATH_PARAM));
            mImagePickerHelper.setImageUri((Uri) savedInstanceState.getParcelable("imageUri"));
            uploadedPackages = (List<HeadsPoint>) savedInstanceState.getSerializable(UPLOADED_PACKAGES_PARAM);
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        FragmentManager fragmentManager = getSupportFragmentManager();
        mSectionsPagerAdapter = new HeadsPagerAdapter(fragmentManager);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
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

        Log.d("MainTabsActivity", "onResume");
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
        bundle.putString(IMAGE_PATH_PARAM, mImagePickerHelper.getImagePath());
        bundle.putParcelable(IMAGE_URI_PARAM, mImagePickerHelper.getImageUri());
        bundle.putSerializable(UPLOADED_PACKAGES_PARAM, (Serializable) uploadedPackages);
    }

    @Override
    protected void onLocationUpdated(Location location) {
        this.location = location;
        SearchFragment searchFragment = (SearchFragment)
                mSectionsPagerAdapter.getRegisteredFragment(SEARCH_FRAGMENT_INDEX);
        if (searchFragment != null) {
            searchFragment.onLocationUpdated(location);
        }

        UploadFragment uploadFragment = (UploadFragment)
                mSectionsPagerAdapter.getRegisteredFragment(UPLOAD_FRAGMENT_INDEX);
        if (uploadFragment != null) {
            uploadFragment.onLocationUpdated(location);
        }
        if (location != null) {
            Log.d("MainTabsActivity", location.getLatitude() + ", " + location.getLongitude());
        }
    }

    @Override
    protected void  onAddressFound(String address) {
        this.address = address;
        Log.d("MainTabsActivity", address);
        SearchFragment searchFragment = (SearchFragment)
                mSectionsPagerAdapter.getRegisteredFragment(SEARCH_FRAGMENT_INDEX);
        if (searchFragment != null) {
            searchFragment.onAddressUpdated(address);
        }

        UploadFragment uploadFragment = (UploadFragment)
                mSectionsPagerAdapter.getRegisteredFragment(UPLOAD_FRAGMENT_INDEX);
        if (uploadFragment != null) {
            uploadFragment.onAddressUpdated(address);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        UserPackagesFragment userPackagesFragment = (UserPackagesFragment)
                mSectionsPagerAdapter.getRegisteredFragment(USER_PACKAGES_FRAGMENT_INDEX);

        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_account:
                Intent intent= new Intent(this, AccountActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_list:
                userPackagesFragment.showList();
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_map:
                if (isNetworkAvailable(R.string.network_warning)) {
                    userPackagesFragment.showMap();
                    supportInvalidateOptionsMenu();
                }
                return true;
            case R.id.action_ok:
                SearchFragment searchFragment = (SearchFragment)
                        mSectionsPagerAdapter.getRegisteredFragment(SEARCH_FRAGMENT_INDEX);
                if (searchFragment != null) {
                    searchFragment.confirmMapNewLocation();
                }
                return true;
            case R.id.action_delete:
                if (!isNetworkAvailable(R.string.network_warning)) {
                    return true;
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                alertDialogBuilder.setTitle(R.string.delete_item);
                alertDialogBuilder
                        .setMessage(R.string.delete_warning_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                UserPackagesFragment userPackagesFragment = (UserPackagesFragment)
                                        mSectionsPagerAdapter.getRegisteredFragment(USER_PACKAGES_FRAGMENT_INDEX);
                                if (userPackagesFragment != null) {
                                    onFormIsBusy();
                                    userPackagesFragment.requestPackageDeletion();
                                }
                                deleteWarningDialog = null;
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                deleteWarningDialog = null;
                            }
                        });
                deleteWarningDialog = alertDialogBuilder.create();
                deleteWarningDialog.show();
                return true;
            case R.id.action_upload:
                if (isNetworkAvailable(R.string.network_warning)) {
                    onFormIsBusy();
                    UserPackagesFragment userFragment = (UserPackagesFragment)
                            mSectionsPagerAdapter.getRegisteredFragment(USER_PACKAGES_FRAGMENT_INDEX);
                    if (userFragment != null) {
                        userFragment.upload();
                    }
                }
                return true;
            case R.id.action_refresh:
                userPackagesFragment.refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d("MainTabsActivity", "Prepare options menu.");

        UserPackagesFragment userPackagesFragment = (UserPackagesFragment)
            mSectionsPagerAdapter.getRegisteredFragment(USER_PACKAGES_FRAGMENT_INDEX);

        Log.d("MainTabsActivity", "userPackagesFragment is " +
                (userPackagesFragment == null ? "null" : " not null"));

        boolean isMineFragmentVisible = userPackagesFragment != null &&
                        mViewPager.getCurrentItem() == USER_PACKAGES_FRAGMENT_INDEX;
        boolean isShowingMap = isMineFragmentVisible && userPackagesFragment != null &&
                               userPackagesFragment.isShowingMap();
        List<HeadsPoint> userPackages = null;
        if (isMineFragmentVisible) {
            if (userPackagesFragment != null) {
                userPackages = userPackagesFragment.getPackages();
            }
        }
        boolean isMapActionVisible = isMineFragmentVisible && !isShowingMap && userPackages != null;
        boolean isListActionVisible = isMineFragmentVisible && isShowingMap  && userPackages != null;
        boolean isDeleteActionVisible = false;
        if (isMapActionVisible) {
            isDeleteActionVisible = userPackagesFragment.isDetailsViewVisible();
        }
        boolean isUploadActionVisible = isDeleteActionVisible && userPackagesFragment.isUploadAllowed();

        menu.findItem(R.id.action_map).setVisible(isMapActionVisible);
        menu.findItem(R.id.action_list).setVisible(isListActionVisible);
        menu.findItem(R.id.action_delete).setVisible(isDeleteActionVisible);
        menu.findItem(R.id.action_upload).setVisible(isUploadActionVisible);
        menu.findItem(R.id.action_refresh).setVisible(isMineFragmentVisible);

        menu.findItem(R.id.action_ok).setVisible(false);
        if (mViewPager.getCurrentItem() == SEARCH_FRAGMENT_INDEX) {
            SearchFragment searchFragment = (SearchFragment)
                    mSectionsPagerAdapter.getRegisteredFragment(SEARCH_FRAGMENT_INDEX);
            if (searchFragment != null && searchFragment.isShowingMap()) {
                menu.findItem(R.id.action_ok).setVisible(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.

        Log.d("MainTabsActivity", "Tab " + tab.getPosition() + " selected.");

        mViewPager.setCurrentItem(tab.getPosition());
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        Log.d("MainTabsActivity", "Tab " + tab.getPosition() + " reselected.");

        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPickImage() {
        if (!mImagePickerHelper.openImageIntent(ALBUM_NAME)) {
            Crouton.makeText(this, getString(R.string.an_error_occurred), Style.ALERT).show();
        }
    }

    @Override
    public void onLocationSelect(Location location) {
        Intent intent = new Intent(this, LocationSelectActivity.class);
        intent.putExtra(LocationSelectActivity.LOCATION_PARAM, location);
        startActivityForResult(intent, HeadsApplication.SELECT_LOCATION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainTabsActivity", "onActivityResult");
        if(resultCode == RESULT_OK) {

            if (requestCode == HeadsApplication.SELECT_LOCATION_REQUEST_CODE) {
                Location locationFromMap = data.getParcelableExtra(LocationSelectActivity.LOCATION_PARAM);
                if (mViewPager.getCurrentItem() == UPLOAD_FRAGMENT_INDEX) {
                    // onActivityResult will be called after fragments get instantiated in case of
                    // an orientation change. In this case fragment instances will be null. Results
                    // are stored to be passed in the fragment later
                    UploadFragment uploadFragment = (UploadFragment)
                            mSectionsPagerAdapter.getRegisteredFragment(UPLOAD_FRAGMENT_INDEX);
                    if (uploadFragment != null) {
                        uploadFragment.onLocationFromMapUpdated(locationFromMap);
                    }
                    else {
                        uploadLocationFromMap = locationFromMap;
                    }
                }
                else if (mViewPager.getCurrentItem() == SEARCH_FRAGMENT_INDEX) {
                    SearchFragment searchFragment = (SearchFragment)
                            mSectionsPagerAdapter.getRegisteredFragment(SEARCH_FRAGMENT_INDEX);
                    if (searchFragment != null) {
                        searchFragment.onLocationFromMapUpdated(locationFromMap);
                    }
                    else {
                        searchLocationFromMap = locationFromMap;
                    }
                }
            }
            else if (requestCode == HeadsApplication.TAG_SELECT_REQUEST_CODE) {
                List<TagModel> tags = (List<TagModel>)
                    data.getSerializableExtra(TagSelectActivity.TAGS_PARAM);

                if (mViewPager.getCurrentItem() == SEARCH_FRAGMENT_INDEX) {
                    SearchFragment searchFragment = (SearchFragment)
                            mSectionsPagerAdapter.getRegisteredFragment(SEARCH_FRAGMENT_INDEX);
                    if (searchFragment != null) {
                        searchFragment.onTagsUpdated(tags);
                    }
                    else {
                        searchTags = tags;
                    }
                }
                else if (mViewPager.getCurrentItem() == UPLOAD_FRAGMENT_INDEX) {
                    UploadFragment uploadFragment = (UploadFragment)
                            mSectionsPagerAdapter.getRegisteredFragment(UPLOAD_FRAGMENT_INDEX);
                    if (uploadFragment != null) {
                        uploadFragment.onTagsUpdated(tags);
                    }
                    else {
                        uploadTags = tags;
                    }
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
                // This is needed for Android 4.4
                // http://stackoverflow.com/questions/19985286/convert-content-uri-to-actual-path-in-android-4-4
                Uri imageUri = mImagePickerHelper.getImageUriFromActivityResult(requestCode, data);
                UploadFragment uploadFragment = (UploadFragment)
                        mSectionsPagerAdapter.getRegisteredFragment(UPLOAD_FRAGMENT_INDEX);
                if (uploadFragment != null) {
                    if (!uploadFragment.onImageUriSelected(imageUri)) {
                        uploadFragment.onImageSelected(imagePath);
                    }
                }
            }
        }
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
        SearchFragment searchFragment = (SearchFragment)
                mSectionsPagerAdapter.getRegisteredFragment(SEARCH_FRAGMENT_INDEX);
        searchFragment.updateDateRange(fromYear, fromMonth, fromDay,
                toYear, toMonth, toDay);
    }

    @Override
    public void onPackageDeleted(HeadsPoint HeadsPackage) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        Log.d("MainTabsActivity", "Package " + HeadsPackage.getId() + " deleted!");
        UserPackagesFragment userPackagesFragment = (UserPackagesFragment)
                mSectionsPagerAdapter.getRegisteredFragment(USER_PACKAGES_FRAGMENT_INDEX);
        if (userPackagesFragment != null) {
            userPackagesFragment.removePackage(HeadsPackage);
        }
        else {
            Log.w("MainTabsActivity", "Can't delete now - UserPackageFragment is null");
            deletedPackage = HeadsPackage;
        }
    }

    @Override
         public void onDeleteFailed(String message) {
        onError(message);
    }

    @Override
    public void onFormIsBusy() {
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.waiting));
    }

    @Override
    public void onSearchStarted() {
        onFormIsBusy();
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
    public void onUploadStarted() {
        onFormIsBusy();
    }

    @Override
    public void onLocalPackageAdded(final HeadsPoint localPackage) {
        mImagePickerHelper.setImagePath(null);
        mImagePickerHelper.setImageUri(null);
        addToUserPackagesFragment(localPackage);
        networkDialogListener = new NetworkDialogListener() {
            @Override
            public void onOkPressed() {
                Intent intent = new Intent(MainTabsActivity.this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.PACKAGE_PARAM, localPackage);
                startActivity(intent);
                networkDialogListener = null;
            }
        };
    }

    @Override
    public void onUploadCompleted(HeadsPoint HeadsPackage) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mImagePickerHelper.setImagePath(null);
        mImagePickerHelper.setImageUri(null);
        addToUserPackagesFragment(HeadsPackage);

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

        Log.d("MainTabsActivity", "Package " + HeadsPackage.getId() + " uploaded!");
        UserPackagesFragment userPackagesFragment = (UserPackagesFragment)
                mSectionsPagerAdapter.getRegisteredFragment(USER_PACKAGES_FRAGMENT_INDEX);
        if (userPackagesFragment != null) {
            userPackagesFragment.updatePackage(HeadsPackage);
        }
        else {
            Log.w("MainTabsActivity", "Can't update package now - UserPackageFragment is null");
            localPackageUploaded = HeadsPackage;
        }
    }

    @Override
    public void onUploadFailed(String message) {
        onError(message);
    }

    private void addToUserPackagesFragment(HeadsPoint HeadsPackage) {
        //Add the new package to the userPackagesFragment
        UserPackagesFragment userPackagesFragment = (UserPackagesFragment)
                mSectionsPagerAdapter.getRegisteredFragment(USER_PACKAGES_FRAGMENT_INDEX);
        if (userPackagesFragment != null) {
            List<HeadsPoint> HeadsPackages = new ArrayList<HeadsPoint>();
            HeadsPackages.add(HeadsPackage);
            userPackagesFragment.addPackages(HeadsPackages);
        }
        else {
            if (uploadedPackages == null) {
                uploadedPackages = new ArrayList<HeadsPoint>();
            }
            uploadedPackages.add(HeadsPackage);
        }
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
                        if (networkDialogListener != null) {
                            networkDialogListener.onOkPressed();
                        }
                    }
                });

        networkAlertDialog = builder.create();
        networkAlertDialog.show();

        return false;
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class HeadsPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public HeadsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == UPLOAD_FRAGMENT_INDEX) {
                return UploadFragment.newInstance(currentLocation, currentAddress,
                                                  mImagePickerHelper.getImagePath(),
                                                  mImagePickerHelper.getImageUri());
            }
            else if (position == SEARCH_FRAGMENT_INDEX) {
                return SearchFragment.newInstance(currentLocation, currentAddress);
            }
            else if (position == USER_PACKAGES_FRAGMENT_INDEX) {
                return UserPackagesFragment.newInstance(null, currentLocation);
            }
            return null;
        }

        @Override
        public int getCount() {
            return TABS_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case UPLOAD_FRAGMENT_INDEX:
                    return getString(R.string.upload).toUpperCase(l);
                case SEARCH_FRAGMENT_INDEX:
                    return getString(R.string.search).toUpperCase(l);
                case USER_PACKAGES_FRAGMENT_INDEX:
                    return getString(R.string.mine).toUpperCase(l);
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d("MainTabsActivity", "Instantiating fragment " + position);

            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);

            String imagePath = mImagePickerHelper.getImagePath();
            Uri imageUri = mImagePickerHelper.getImageUri();
            if (fragment instanceof UploadFragment) {
                if (!((UploadFragment) fragment).onImageUriSelected(imageUri)) {
                    ((UploadFragment) fragment).onImageSelected(imagePath);
                }
                if (location != null) {
                    ((UploadFragment) fragment).onLocationUpdated(location);
                }
                if (address != null) {
                    ((UploadFragment) fragment).onAddressUpdated(address);
                }
                if (uploadLocationFromMap != null) {
                    ((UploadFragment) fragment).onLocationFromMapUpdated(uploadLocationFromMap);
                }
                if (uploadTags != null) {
                    ((UploadFragment) fragment).onTagsUpdated(uploadTags);
                }
            }
            else if (fragment instanceof SearchFragment) {
                if (location != null) {
                    ((SearchFragment) fragment).onLocationUpdated(location);
                }
                if (address != null) {
                    ((SearchFragment) fragment).onAddressUpdated(address);
                }
                if (searchLocationFromMap != null) {
                    ((SearchFragment) fragment).onLocationFromMapUpdated(searchLocationFromMap);
                }
                if (searchTags != null) {
                    ((SearchFragment) fragment).onTagsUpdated(searchTags);
                }
            }
            else if (fragment instanceof UserPackagesFragment) {
                if (deletedPackage != null) {

                    deletedPackage = null;
                }
                if (localPackageUploaded != null) {
                    Log.w("MainTabsActivity", "Updating package now!");
                    ((UserPackagesFragment) fragment).updatePackage(localPackageUploaded);
                    localPackageUploaded = null;
                }
                if (uploadedPackages != null && uploadedPackages.size() > 0) {
                    ((UserPackagesFragment) fragment).addPackages(uploadedPackages);
                    uploadedPackages.clear();
                }
            }

            // This is needed in case of screen orientation
            supportInvalidateOptionsMenu();

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    private interface NetworkDialogListener {
        void onOkPressed();
    }

}
