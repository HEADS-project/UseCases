package gr.atc.heads.fragments;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.MapView;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.atc.heads.DetailsActivity;
import gr.atc.heads.R;
import gr.atc.heads.HeadsApplication;
import gr.atc.heads.adapters.HeadsPackageListAdapter;
import gr.atc.heads.io.IHeadsClient;
import gr.atc.heads.io.HeadsClientFactory;
import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.Tag;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link gr.atc.heads.fragments.ExtMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ExtMapFragment extends Fragment implements
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final String PACKAGES_PARAM = "packages";
    private static final String LOCATION_PARAM = "location";
    public static final String ALLOW_DELETE_PARAM = "isDeleteAllowed";

    private Location location;

    private List<HeadsPoint> packages;

    private Context context;

    private boolean isDeleteAllowed;

    private IHeadsClient HeadsClient;

    private MapView mapView;
    private GoogleMap googleMap;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect bounds = new Rect();
    private LruCache<Integer, BitmapDescriptor> cache = new LruCache<Integer, BitmapDescriptor>(128);
    private ClusterOptions clusterOptions = new ClusterOptions().anchor(0.5f, 0.5f);

    private List<Marker> markers = new ArrayList<Marker>();
    private Map<String, HeadsPoint> markerRecordInfo = new HashMap<String, HeadsPoint>();
    private Map<String, String> markerImages = new HashMap<String, String>();

    private String tappedRecordID;

    private AlertDialog multipleRecordsAlert;

    private DisplayImageOptions displayOptions;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param packages List of packages to renden on the map
     * @return A new instance of fragment MapFragment.
     */
    public static ExtMapFragment newInstance(List<HeadsPoint> packages, Location location, boolean isDeleteAllowed) {
        ExtMapFragment fragment = new ExtMapFragment();
        Bundle args = new Bundle();
        args.putSerializable(PACKAGES_PARAM, (Serializable) packages);
        args.putParcelable(LOCATION_PARAM, location);
        args.putBoolean(ALLOW_DELETE_PARAM, isDeleteAllowed);
        fragment.setArguments(args);
        return fragment;
    }
    public ExtMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            packages = (List<HeadsPoint>) getArguments().getSerializable(PACKAGES_PARAM);
            location = getArguments().getParcelable(LOCATION_PARAM);
            isDeleteAllowed = getArguments().getBoolean(ALLOW_DELETE_PARAM);
        }

        displayOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.dashboard_upload)
                .showImageForEmptyUri(R.drawable.dashboard_upload)
                .showImageOnFail(R.drawable.dashboard_upload)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_ext_map, container, false);

        try {
            MapsInitializer.initialize(getActivity());
        }
        finally {
        }

        mapView = (MapView) inflatedView.findViewById(R.id.map);
        mapView.onCreate(null);

        googleMap = ((MapView) inflatedView.findViewById(R.id.map)).getExtendedMap();

        return inflatedView;
    }

    @Override
    public void onAttach(Activity activity) {
        context = activity.getBaseContext();
        HeadsClientFactory factory = new HeadsClientFactory(context);
        HeadsClient = factory.createHeadsClient();
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
        if (googleMap != null) {
            googleMap.clear();
            markers.clear();
            markerRecordInfo.clear();
            markerImages.clear();
            setUpMap();
            fitMapToPins();
        }
        else {
            Log.w("ExtMapFragment", "Map is null!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (multipleRecordsAlert != null) {
            multipleRecordsAlert.dismiss();
        }
    }

    public void setPackages(List<HeadsPoint> packages) {
        this.packages = packages;
        if (markers != null) {
            for (Marker marker: markers) {
                marker.remove();
            }
        }
        //googleMap.clear();
        markers.clear();
        markerRecordInfo.clear();
        markerImages.clear();

        addMarkersToMap();
    }

    private void setUpMap() {

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        googleMap.setOnMarkerClickListener(this);

        //Clustering
        googleMap.setClustering(new ClusteringSettings().clusterOptionsProvider(new ClusterOptionsProvider() {

            @Override
            public ClusterOptions getClusterOptions(List<Marker> markers) {

                //Custom cluster image with number of cluster markers on it as text.
                //https://code.google.com/p/android-maps-extensions/source/browse/android-maps-extensions-demo/src/pl/mg6/android/maps/extensions/demo/DemoClusterOptionsProvider.java?r=9654a927b4b1186d40dd6542151c11275c7d4cf1
                int markersCount = markers.size();

                BitmapDescriptor cachedIcon = cache.get(markersCount);
                if (cachedIcon != null) {
                    return clusterOptions.icon(cachedIcon);
                }

                Bitmap base = BitmapFactory.decodeResource(getResources(), R.drawable.cluster);

                Bitmap bitmap = base.copy(Bitmap.Config.ARGB_8888, true);

                String text = String.valueOf(markersCount);

                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);

                int textClusterSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

                paint.setTextSize(textClusterSize);
                paint.getTextBounds(text, 0, text.length(), bounds);

                float x = bitmap.getWidth() / 2.0f - bitmap.getHeight()/30;
                float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top - bitmap.getHeight()/9;

                Canvas canvas = new Canvas(bitmap);
                canvas.drawText(text, x, y, paint);

                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
                cache.put(markersCount, icon);

                return clusterOptions.icon(icon);
            }
        }));

        //Populate map with markers
        addMarkersToMap();
    }

    private void addMarkersToMap() {

        if (packages == null) {
            return;
        }

        if (getActivity() == null) {
            return;
        }

        List<Tag> tags = ((HeadsApplication) getActivity().getApplication()).getTags();

        //Marker to be added to the map
        Marker marker;

        for (HeadsPoint record : packages) {
            BitmapDescriptor icon;

            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

            String tagsTitle = "";
            if (record.getTags() != null) {
                for (Integer tagId : record.getTags()) {
                    for (Tag tag : tags) {
                        if (tag.getId() == tagId) {
                            tagsTitle += tag.getName() + " ";
                        }
                    }
                }
            }

            marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(record.getLatitude(), record.getLongitude()))
                    .title(record.getTitle())
                    .snippet(record.getDescription() + " " + tagsTitle)
                    .icon(icon));

            markers.add(marker);

            //Add the POI as extra info on a HashMap for the specific marker
            markerRecordInfo.put(marker.getId(), record);

            markerImages.put(marker.getId(), record.getId());

            //Show info window if marker was previously tapped
            if (record.getId() == tappedRecordID) {
                marker.showInfoWindow();
            }
        }
    }

    public void zoomIn(double lat, double lng, float zoom) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        HeadsPoint HeadsPackage = markerRecordInfo.get(marker.getId());
        onPackageSelected(HeadsPackage);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //The marker list in the cluster
        List<Marker> markerList = marker.getMarkers();

        //If this is a cluster
        if (markerList != null) {
            float zoom = googleMap.getCameraPosition().zoom;

            if (zoom < 16) {
                zoom += 2;
                zoomIn(marker.getPosition().latitude, marker.getPosition().longitude, zoom);
                return false;
            } else {
                //Max zoom achieved. Show records inside cluster in a list
                final CharSequence[] items = new CharSequence[markerList.size()];
                final HeadsPoint[] multipleRecords = new HeadsPoint[markerList.size()];

                for (int i=0 ; i<markerList.size() ; i++) {
                    Marker mark = markerList.get(i);
                    HeadsPoint record = markerRecordInfo.get(mark.getId());
                    items[i] = record.getTitle();
                    multipleRecords[i] = record;
                }

                List<Tag> tags = ((HeadsApplication) getActivity().getApplication()).getTags();
                HeadsPackageListAdapter adapter = new HeadsPackageListAdapter(getActivity(),
                        new ArrayList<HeadsPoint>(Arrays.asList(multipleRecords)),
                        false, tags);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                alertBuilder.setTitle(getText(R.string.multiplerecords))
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                multipleRecordsAlert = null;
                                HeadsPoint HeadsPackage = multipleRecords[item];
                                onPackageSelected(HeadsPackage);
                            }
                        });

                multipleRecordsAlert = alertBuilder.create();
                multipleRecordsAlert.show();
                return true;
            }
        } else    //Single marker
            return false;
    }

    static class VersionHelper {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public static void removeOnGlobalLayoutListener(View mapView,
                                                        ViewTreeObserver.OnGlobalLayoutListener listener) {
            mapView.getViewTreeObserver()
                    .removeOnGlobalLayoutListener(listener);
        }
    }

    private void fitMapToPins() {

        if (mapView.getViewTreeObserver().isAlive()) {

            mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        LatLngBounds.Builder bc = new LatLngBounds.Builder();

                        for (Marker item : markers) {
                            bc.include(item.getPosition());
                        }

                        if (markers.size() > 0) {
                            LatLngBounds bounds = bc.build();

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                mapView.getViewTreeObserver()
                                        .removeGlobalOnLayoutListener(this);
                            } else {
                                VersionHelper.removeOnGlobalLayoutListener(mapView, this);
                            }

                            if (googleMap != null)
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                        }
                    }
                });
        }
    }

    private void onPackageSelected(HeadsPoint HeadsPackage) {
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.PACKAGE_PARAM, HeadsPackage);
        intent.putExtra(DetailsActivity.ALLOW_DELETE_PARAM, isDeleteAllowed);
        if (isDeleteAllowed) {
            getActivity().startActivityForResult(intent, HeadsApplication.PACKAGES_EDIT_REQUEST_CODE);
        }
        else {
            getActivity().startActivity(intent);
        }
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter, ImageLoadingListener {

        private final View mWindow;
        private Marker mMarker;

        private String packageId;

        CustomInfoWindowAdapter() {
            mWindow = getActivity().getLayoutInflater()
                    .inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            if (marker.isCluster()) {
                return null;
            }

            mMarker = marker;
            packageId = markerImages.get(marker.getId());
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, View view) {

            // Image downloading handling
            ImageView thumbImageView = ((ImageView) view.findViewById(R.id.badge));
            thumbImageView.setImageResource(R.drawable.dashboard_upload);
            String thumbUrl = HeadsClient.getThumbnailUrl(packageId);

            List<Bitmap> cachedBitmaps = MemoryCacheUtils.findCachedBitmapsForImageUri(thumbUrl,
                                                                ImageLoader.getInstance().getMemoryCache());
            if (cachedBitmaps.size() > 0) {
                thumbImageView.setImageBitmap(cachedBitmaps.get(0));
            }
            else {
                ImageLoader.getInstance().displayImage(thumbUrl, thumbImageView, displayOptions, this);
            }

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the
                // text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
                        titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0,
                        1, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 2,
                        snippetText.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            // Show window again. Modifying existing view may have no effect
            mMarker.showInfoWindow();
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

}
