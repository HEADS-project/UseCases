package gr.atc.heads.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import gr.atc.heads.R;
import gr.atc.heads.io.HeadsSearchListener;
import gr.atc.heads.model.HeadsPointQuery;
import gr.atc.heads.model.HeadsPoint;


public class SearchFragment extends HeadsFormFragment implements HeadsSearchListener {

    private SearchFragmentListener searchListener;

    private EditText whenField;

    private int fromYear;
    private int fromMonth;
    private int fromDay;
    private int toYear;
    private int toMonth;
    private int toDay;

    private boolean searchAnytime = true;

    private boolean showMap;
    private boolean isMapMovedToCurrentLocation;
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    private View helpWindow;

    public boolean isShowingMap() {
        return  showMap;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location Current user's location
     * @param address Current user's address
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance(Location location, String address) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = getBundle(location, address);
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        headsClient.setHeadsSearchListener(this);

        Calendar cal = new GregorianCalendar();
        fromYear = cal.get(Calendar.YEAR);
        fromMonth = cal.get(Calendar.MONTH);
        fromDay = cal.get(Calendar.DAY_OF_MONTH);
        toYear = cal.get(Calendar.YEAR);
        toMonth = cal.get(Calendar.MONTH);
        toDay = cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        View mapSection = view.findViewById(R.id.map_section);
        if (mapSection != null) {
            View imageSection = view.findViewById(R.id.image_section);
            imageSection.setVisibility(View.GONE);
            mapSection.setVisibility(View.VISIBLE);

            helpWindow = view.findViewById(R.id.help_window);
            locationField.setClickable(false);
            locationField.setOnClickListener(null);
        }
        showMap = (mapSection != null);
        isMapMovedToCurrentLocation = false;

        whenField = (EditText) view.findViewById(R.id.search_when);
        final String [] whenOptions = getResources().getStringArray(R.array.search_when_options);
        whenField.setFocusable(false);
        whenField.setClickable(true);
        whenField.setText(whenOptions[0]);
        whenField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.select_time).setItems(whenOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == whenOptions.length - 1) {
                            // Last option is range
                            searchListener.onSelectDateRange(fromYear, fromMonth, fromDay,
                                    toYear, toMonth, toDay);
                        } else {
                            whenField.setText(whenOptions[which]);
                        }
                        searchAnytime = which == 0;
                        Calendar cal = new GregorianCalendar();
                        toYear = cal.get(Calendar.YEAR);
                        toMonth = cal.get(Calendar.MONTH);
                        toDay = cal.get(Calendar.DAY_OF_MONTH);
                        if (which == 1) {
                            // 1 week
                            cal.add(Calendar.WEEK_OF_YEAR, -1);
                            fromYear = cal.get(Calendar.YEAR);
                            fromMonth = cal.get(Calendar.MONTH);
                            fromDay = cal.get(Calendar.DAY_OF_MONTH);
                        }
                        else if (which == 2) {
                            // 1 month
                            cal.add(Calendar.MONTH, -1);
                            fromYear = cal.get(Calendar.YEAR);
                            fromMonth = cal.get(Calendar.MONTH);
                            fromDay = cal.get(Calendar.DAY_OF_MONTH);
                        }
                        else if (which == 3) {
                            // 1 year
                            cal.add(Calendar.YEAR, -1);
                            fromYear = cal.get(Calendar.YEAR);
                            fromMonth = cal.get(Calendar.MONTH);
                            fromDay = cal.get(Calendar.DAY_OF_MONTH);
                        }
                    }
                });
                builder.show();
            }
        });

        view.findViewById(R.id.range_layout).setVisibility(View.VISIBLE);

        Button search = (Button) view.findViewById(R.id.submit);
        search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!listener.isNetworkAvailable(R.string.network_warning)) {
                    return;
                }

                HeadsPointQuery query = new HeadsPointQuery();

                double latitude = 37.976616;		//Metro Athens
                double longitude = 23.726317;
                if (location != null) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                }
                query.setLongitude(longitude);
                query.setLatitude(latitude);

                String rangeStr =
                        ((Spinner) view.findViewById(R.id.search_range_spinner)).getSelectedItem().toString();
                rangeStr = rangeStr.replace("Km", "");
                double range = Double.parseDouble(rangeStr);
                query.setRange(range);

                String title = ((EditText) view.findViewById(R.id.package_title)).getText().toString();
                String description = ((EditText) view.findViewById(R.id.description)).getText().toString();

                query.setTitle(title);
                query.setDescription(description);

                if (!searchAnytime) {
                    Calendar from = new GregorianCalendar();
                    from.set(fromYear, fromMonth, fromDay, 0, 0, 0);

                    Calendar to = new GregorianCalendar();
                    to.set(toYear, toMonth, toDay, 23, 59, 59);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    Log.d("SearchFragment", "Searching from " + sdf.format(from.getTime()) +
                                            " to " + sdf.format(to.getTime()));

                    query.setFrom(String.format("%d", from.getTimeInMillis()));
                    query.setTo(String.format("%d", to.getTimeInMillis()));
                }

                // Search request
                isBusy = true;
                searchListener.onSearchStarted();
                headsClient.startSearch(query);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (showMap) {
            FragmentManager fm = getChildFragmentManager();
            mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.map, mapFragment).commit();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showMap) {
            map = mapFragment.getMap();
            if (map != null) {
                map.setMyLocationEnabled(true);
                if (location != null) {
                    moveMapToCurrentLocation();
                }
                Animation anim = AnimationUtils.loadAnimation(getActivity(),
                                                            R.anim.window_fade);
                helpWindow.startAnimation(anim);
            }
        }
    }

    public void confirmMapNewLocation() {
        if (showMap) {
            Location newLocation;
            if (location != null) {
                newLocation = new Location(location);
            }
            else {
                newLocation = new Location("");
            }
            newLocation.setLatitude(map.getCameraPosition().target.latitude);
            newLocation.setLongitude(map.getCameraPosition().target.longitude);
            onLocationFromMapUpdated(newLocation);
        }
    }

    private void moveMapToCurrentLocation() {
        if (map != null) {
            float zoomLevel = 14.0f;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    zoomLevel));
            isMapMovedToCurrentLocation = true;
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        super.onLocationUpdated(location);

        if (showMap && location != null) {
            moveMapToCurrentLocation();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            searchListener = (SearchFragmentListener) activity;
            if (isBusy) {
                searchListener.onFormIsBusy();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        // See http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed/15656428#15656428
        // for an explanation
        super.onDetach();
        searchListener = null;

        if (!showMap) {
            return;
        }

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
    public void searchFailed(String message) {
        isBusy = false;
        searchListener.onSearchFailed(message);
    }

    @Override
    public void searchCompleted(List<HeadsPoint> results) {
        isBusy = false;
        searchListener.onSearchCompleted(results);
    }

    public void updateDateRange(int fromYear, int fromMonth, int fromDay,
                                int toYear, int toMonth, int toDay) {
        this.fromYear = fromYear;
        this.fromMonth = fromMonth;
        this.fromDay = fromDay;
        this.toYear = toYear;
        this.toMonth = toMonth;
        this.toDay = toDay;
        whenField.setText(String.format("%02d/%02d/%d - %02d/%02d/%02d",
                                        fromDay, fromMonth + 1, fromYear,
                                        toDay, toMonth + 1, toYear));
    }

    public interface SearchFragmentListener extends FormFragmentListener {
        void onSelectDateRange(int fromYear, int fromMonth, int fromDay,
                               int toYear, int toMonth, int toDay);
        void onSearchStarted();
        void onSearchCompleted(List<HeadsPoint> resultPackageList);
        void onSearchFailed(String message);
    }

}
