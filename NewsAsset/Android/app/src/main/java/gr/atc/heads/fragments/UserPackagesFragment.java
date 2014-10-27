package gr.atc.heads.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import gr.atc.common.fragments.CustomFragment;
import gr.atc.common.utils.Utils;
import gr.atc.heads.R;
import gr.atc.heads.HeadsApplication;
import gr.atc.heads.io.IHeadsClient;
import gr.atc.heads.io.HeadsClientFactory;
import gr.atc.heads.io.HeadsSearchListener;
import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.User;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link gr.atc.heads.fragments.UserPackagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class UserPackagesFragment extends CustomFragment implements HeadsSearchListener {

    public static final String PACKAGES_PARAM = "packages";
    public static final String LOCATION_PARAM = "location";
    private static final String SHOW_MAP_PARAM = "showMap";

    private List<HeadsPoint> packages = new ArrayList<HeadsPoint>();

    private Location location;

    private boolean isShowingMap;

    private ResultsListFragment listFragment;

    private ExtMapFragment mapFragment;

    private View loadingView;

    public boolean isShowingMap() {
        return isShowingMap;
    }

    public List<HeadsPoint> getPackages() {
        return packages;
    }

    private boolean isFragmentAdded;

    private IHeadsClient HeadsClient;

    private UserPackagesFragmentListener listener;

    public boolean isDetailsViewVisible() {
        return getActivity() != null &&
                getActivity().getString(R.string.delailsViewVisible).equals("true");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserPackagesFragment.
     */
    public static UserPackagesFragment newInstance(List<HeadsPoint> packages, Location location) {
        UserPackagesFragment fragment = new UserPackagesFragment();
        Bundle args = new Bundle();
        args.putSerializable(PACKAGES_PARAM, (Serializable) packages);
        args.putParcelable(LOCATION_PARAM, location);
        fragment.setArguments(args);
        return fragment;
    }
    public UserPackagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        isFragmentAdded = false;

        if (getArguments() != null) {
            packages = (List<HeadsPoint>) getArguments().getSerializable(PACKAGES_PARAM);
            location =  getArguments().getParcelable(LOCATION_PARAM);
        }
        listFragment = ResultsListFragment.newInstance(packages, true);
        mapFragment = ExtMapFragment.newInstance(packages, location, true);
        isShowingMap = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("UserPackagesFragment", "onCreateView");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_packages, container, false);
        loadingView = view.findViewById(R.id.loading);

        if (savedInstanceState != null) {
            isShowingMap = savedInstanceState.getBoolean(SHOW_MAP_PARAM);
            packages = (List<HeadsPoint>) savedInstanceState.getSerializable(PACKAGES_PARAM);
            location = savedInstanceState.getParcelable(LOCATION_PARAM);
        }

        if (!Utils.isNetworkAvailable(getActivity())) {
            loadLocalPackages();
            listFragment = ResultsListFragment.newInstance(packages, true);
            mapFragment = ExtMapFragment.newInstance(packages, location, true);
            showList();
            if (getActivity() != null) {
                getActivity().supportInvalidateOptionsMenu();
            }
        }
        else if (packages == null) {
            loadUserPackages();
        }
        else if (!isShowingMap) {
            showList();
        }
        else  {
            showMap();
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PACKAGES_PARAM, (Serializable) packages);
        outState.putParcelable(LOCATION_PARAM, location);
        outState.putBoolean(SHOW_MAP_PARAM, isShowingMap);
    }

    @Override
    public void onPause() {
        Log.d("UserPackagesFragment", "onPause");
        super.onPause();
        if (loadingView.getVisibility() != View.VISIBLE) {
            isFragmentAdded = false;
            getChildFragmentManager().beginTransaction()
                    .remove(isShowingMap ? mapFragment : listFragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        Log.d("UserPackagesFragment", "onResume");
        super.onResume();
        if (loadingView.getVisibility() != View.VISIBLE && !isFragmentAdded) {
            isFragmentAdded = true;
            getChildFragmentManager().beginTransaction()
                    .add(R.id.container, isShowingMap ? mapFragment : listFragment)
                    .commit();
        }
    }

    public void refresh() {
        if (loadingView.getVisibility() == View.VISIBLE) {
            return;
        }

        if (isShowingMap) {
            getChildFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .commitAllowingStateLoss();
        }
        else {
            getChildFragmentManager().beginTransaction()
                    .remove(listFragment)
                    .commitAllowingStateLoss();
        }
        loadingView.setVisibility(View.VISIBLE);
        loadUserPackages();
    }

    public void showList() {
        Log.d("UserPackagesFragment", "showList");
        isShowingMap = false;

        if (getActivity() == null) {
            return;
        }

        isFragmentAdded = true;
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
            getChildFragmentManager().beginTransaction()
                    .add(R.id.container, listFragment)
                    .commitAllowingStateLoss();
        }
        else {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container, listFragment)
                    .commitAllowingStateLoss();
        }
    }

    public void showMap() {
        if (listener == null || !listener.isNetworkAvailable(R.string.network_warning)) {
            return;
        }
        Log.d("UserPackagesFragment", "showMap");
        isShowingMap = true;
        isFragmentAdded = true;
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
            getChildFragmentManager().beginTransaction()
                    .add(R.id.container, mapFragment)
                    .commitAllowingStateLoss();
        }
        else {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container, mapFragment)
                    .commitAllowingStateLoss();
        }
    }

    private void loadUserPackages() {
        if (HeadsClient == null) {
            HeadsClientFactory factory = new HeadsClientFactory(getActivity().getApplicationContext());
            HeadsClient = factory.createHeadsClient();
            HeadsClient.setHeadsSearchListener(this);
        }

        User user = ((HeadsApplication) getActivity().getApplication()).getUser();
        if (user != null) {
            String userId = user.getUserName();
            HeadsClient.requestUserPackages(userId);
        }
    }

    private void loadLocalPackages() {
        if (getActivity() == null) {
            return;
        }
        List<HeadsPoint> localPackages =
                ((HeadsApplication) (getActivity().getApplication())).getLocalPackages();
        if (localPackages != null) {
            if (packages == null) {
                packages = new ArrayList<HeadsPoint>();
            }
            for(HeadsPoint localPackage: localPackages) {
                if (!packages.contains(localPackage)) {
                    packages.add(localPackage);
                }
            }
        }
    }

    public boolean isUploadAllowed() {
        return listFragment.isUploadAllowed();
    }

    public void requestPackageDeletion() {
        listFragment.requestPackageDeletion();
    }

    public void upload() {
        listFragment.upload();
    }

    public void addPackages(List<HeadsPoint> newPackages) {
        if (newPackages == null) {
            return;
        }
        if (packages == null) {
            packages = new ArrayList<HeadsPoint>();
        }
        Map<String, HeadsPoint> packageMap = new HashMap<String, HeadsPoint>();
        for(HeadsPoint pkg: packages) {
            packageMap.put(pkg.getId(), pkg);
        }

        for (HeadsPoint newPackage: newPackages) {
            if (packageMap.containsKey(newPackage.getId())) {
                continue;
            }
            packages.add(newPackage);
        }
        if (listFragment != null) {
            listFragment.setPackages(packages);
        }
        if (mapFragment != null) {
            mapFragment.setPackages(packages);
        }
    }

    public void removePackage(HeadsPoint HeadsPackage) {
        if (packages == null || HeadsPackage == null) {
            return;
        }
        HeadsPoint deletedPackage = null;
        if (HeadsPackage.getId() == null) {
            for (HeadsPoint pkg : packages) {
                if (pkg.getImagePath() != null && pkg.getImagePath().equals(HeadsPackage.getImagePath())) {
                    deletedPackage = pkg;
                    break;
                }
                if (pkg.getImageUri() != null && pkg.getImageUri().equals(HeadsPackage.getImageUri())) {
                    deletedPackage = pkg;
                    break;
                }
            }
        }
        else {
            for (HeadsPoint pkg : packages) {
                if (pkg.getId() != null && pkg.getId().equals(HeadsPackage.getId())) {
                    deletedPackage = pkg;
                    break;
                }
            }
        }
        if (deletedPackage != null) {
            packages.remove(deletedPackage);
            listFragment.setPackages(packages);
            mapFragment.setPackages(packages);
        }
    }

    public void updatePackage(HeadsPoint HeadsPackage) {
        if (packages == null || HeadsPackage == null) {
            return;
        }
        HeadsPoint updatedPackage = null;

        for (HeadsPoint pkg : packages) {
            if (pkg.getImagePath() != null && pkg.getImagePath().equals(HeadsPackage.getImagePath())) {
                updatedPackage = pkg;
                break;
            }
            if (pkg.getImageUri() != null && pkg.getImageUri().equals(HeadsPackage.getImageUri())) {
                updatedPackage = pkg;
                break;
            }
        }

        if (updatedPackage == null) {
            for (HeadsPoint pkg : packages) {
                if (pkg.getId() != null && pkg.getId().equals(HeadsPackage.getId())) {
                    updatedPackage = pkg;
                    break;
                }
            }
        }
        if (updatedPackage != null) {
            updatedPackage.setId(HeadsPackage.getId());
            listFragment.setPackages(packages);
            mapFragment.setPackages(packages);
        }
    }

    @Override
    public void searchFailed(String message) {
        networkError(message);
    }

    @Override
    public void searchCompleted(List<HeadsPoint> results) {
        Log.d("UserPackagesFragment", "searchCompleted");
        packages = results;
        if (results == null) {
            packages = new ArrayList<HeadsPoint>();
        }
        loadLocalPackages();
        listFragment = ResultsListFragment.newInstance(packages, true);
        mapFragment = ExtMapFragment.newInstance(packages, location, true);
        showList();
        if (getActivity() != null) {
            getActivity().supportInvalidateOptionsMenu();
        }
    }

    private void networkError(String message) {
        if (message == null) {
            message = getString(R.string.an_error_occurred);
        }
        Crouton.makeText(getActivity(), message, Style.ALERT).show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (UserPackagesFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UserPackagesFragmentListener");
        }
    }

    public interface UserPackagesFragmentListener {
        boolean isNetworkAvailable(int warningMessageResId);
    }
}
