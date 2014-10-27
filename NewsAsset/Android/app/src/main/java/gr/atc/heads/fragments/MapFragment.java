package gr.atc.heads.fragments;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.atc.heads.DetailsActivity;
import gr.atc.heads.R;
import gr.atc.heads.HeadsApplication;
import gr.atc.heads.io.IHeadsClient;
import gr.atc.heads.io.HeadsClientFactory;
import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.Tag;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link gr.atc.heads.fragments.MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerDragListener {

    private static final String PACKAGES_PARAM = "packages";
    private static final String LOCATION_PARAM = "location";
    public static final String ALLOW_DELETE_PARAM = "isDeleteAllowed";

    private Location location;

    private SupportMapFragment mapFragment;

    private GoogleMap map;

    private List<Marker> markers;
    private List<HeadsPoint> packages;
    private Map<String, HeadsPoint> markerPackages;
    private Map<String, String> markerImages;

    private Context context;

    private boolean isDeleteAllowed;

    private IHeadsClient HeadsClient;

    private DisplayImageOptions displayOptions;

    /*public void setPackages(List<HeadsPackage> packages) {
        this.packages = packages;
    }*/

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param packages List of packages to renden on the map
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment newInstance(List<HeadsPoint> packages, Location location, boolean isDeleteAllowed) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putSerializable(PACKAGES_PARAM, (Serializable) packages);
        args.putParcelable(LOCATION_PARAM, location);
        args.putBoolean(ALLOW_DELETE_PARAM, isDeleteAllowed);
        fragment.setArguments(args);
        return fragment;
    }
    public MapFragment() {
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

        markers = new ArrayList<Marker>();
        markerPackages= new HashMap<String, HeadsPoint>();
        markerImages= new HashMap<String, String>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        context = activity.getBaseContext();
        HeadsClientFactory factory = new HeadsClientFactory(context);
        HeadsClient = factory.createHeadsClient();
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        // See http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed/15656428#15656428
        // for an explanation
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map = mapFragment.getMap();
        if (map != null) {
            setUpMap();
        }
    }

    public void setPackages(List<HeadsPoint> packages) {
        this.packages = packages;
        if (markers != null) {
            for (Marker marker: markers) {
                marker.remove();
            }
        }
        markers = new ArrayList<Marker>();
        markerPackages= new HashMap<String, HeadsPoint>();
        markerImages= new HashMap<String, String>();

        addMarkersToMap();
    }

    static class VersionHelper {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public static void removeOnGlobalLayoutListener(View mapView,
                                                 ViewTreeObserver.OnGlobalLayoutListener listener) {
            mapView.getViewTreeObserver()
                    .removeOnGlobalLayoutListener(listener);
        }
    }

    private void setUpMap() {

        /*if (location != null) {
            LatLng coordinate = new LatLng(location.getLatitude(),
                                           location.getLongitude());
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            map.animateCamera(yourLocation);
        }*/

        map.setMyLocationEnabled(true);

        addMarkersToMap();

        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMarkerDragListener(this);

        // Pan to see all markers in view.
        final View mapView = mapFragment.getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @SuppressWarnings("deprecation")
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
                                map.moveCamera(CameraUpdateFactory
                                        .newLatLngBounds(bounds, 50));
                            }
                        }
                    });
        }
    }

    private void addMarkersToMap() {

        if (packages == null) {
            return;
        }

        if (getActivity() == null) {
            return;
        }

        List<Tag> tags = ((HeadsApplication) getActivity().getApplication()).getTags();

        for(HeadsPoint HeadsPackage: packages) {
            String tagsTitle = "";
            if (HeadsPackage.getTags()!=null) {
                for (Integer tagId : HeadsPackage.getTags()) {
                    for (Tag tag : tags) {
                        if(tag.getId() == tagId) {
                            tagsTitle += tag.getName() + " ";
                        }
                    }
                }
            }

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(HeadsPackage.getLatitude(), HeadsPackage.getLongitude()))
                    .title(HeadsPackage.getTitle())
                    .snippet(HeadsPackage.getDescription() + " " + tagsTitle)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            markers.add(marker);

            Log.d("MapFragment", "Added marker " + marker.getTitle());

            // Add the marked-id, package to the hash-table so it can be referenced from
            // the CustomInfoWindowAdapter
            markerPackages.put(marker.getId(), HeadsPackage);

            markerImages.put(marker.getId(), HeadsPackage.getId());
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        HeadsPoint pkg = markerPackages.get(marker.getId());
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.PACKAGE_PARAM, pkg);
        intent.putExtra(DetailsActivity.ALLOW_DELETE_PARAM, isDeleteAllowed);
        if (isDeleteAllowed) {
            getActivity().startActivityForResult(intent, HeadsApplication.PACKAGES_EDIT_REQUEST_CODE);
        }
        else {
            getActivity().startActivity(intent);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter,
            ImageLoadingListener {

        private final View mWindow;
        private Marker mMarker;

        private String packageId;

        CustomInfoWindowAdapter() {
            mWindow = getActivity().getLayoutInflater()
                    .inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            mMarker = marker;
            // biopackage = (HeadsPackage) markerPackages.get(marker.getId());
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
