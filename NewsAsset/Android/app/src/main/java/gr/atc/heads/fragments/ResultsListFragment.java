package gr.atc.heads.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gr.atc.common.fragments.CustomListFragment;
import gr.atc.heads.DetailsActivity;
import gr.atc.heads.R;
import gr.atc.heads.HeadsApplication;
import gr.atc.heads.adapters.HeadsPackageListAdapter;
import gr.atc.heads.model.HeadsPoint;

/**
 * Created by kGiannakakis on 10/6/2014.
 */
public class ResultsListFragment extends CustomListFragment {

    private static final String PACKAGES_PARAM = "packages";
    public static final String ALLOW_DELETE_PARAM = "isDeleteAllowed";

    private List<HeadsPoint> packages = new ArrayList<HeadsPoint>();

    private BaseAdapter la;

    private PackageFragment packageFragment;

    private boolean isDeleteAllowed;

    public boolean isDetailsViewVisible() {
        return packageFragment != null;
    }

    public static ResultsListFragment newInstance(List<HeadsPoint> packages, boolean isDeleteAllowed) {
        ResultsListFragment fragment = new ResultsListFragment();
        Bundle args = new Bundle();
        args.putSerializable(PACKAGES_PARAM, (Serializable) packages);
        args.putBoolean(ALLOW_DELETE_PARAM, isDeleteAllowed);
        fragment.setArguments(args);
        return fragment;
    }

    public ResultsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            packages = (List<HeadsPoint>) getArguments().getSerializable(PACKAGES_PARAM);
            isDeleteAllowed = getArguments().getBoolean(ALLOW_DELETE_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ResultsListFragment", "onCreateView");

        HeadsApplication app = (HeadsApplication) getActivity().getApplication();
        la = new HeadsPackageListAdapter(getActivity(), packages,
                                            false, app.getTags());
        setListAdapter(la);
        View view = inflater.inflate(R.layout.fragment_results, container, false);

        View detailsView = view.findViewById(R.id.details_container);
        if (detailsView != null && packages != null && packages.size() > 0) {
            packages.get(0).setSelected(true);
            packageFragment = PackageFragment.newInstance(packages.get(0), true);
            getChildFragmentManager().beginTransaction()
                    .add(R.id.details_container, packageFragment)
                    .commit();
        } else {
            packageFragment = null;
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (packageFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(packageFragment)
                    .commit();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (packageFragment == null) {
            HeadsPoint pkg = packages.get(position);
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
        else {
            selectPackage(position);
        }
    }

    private void selectPackage(int position) {
        for(HeadsPoint pkg: packages) {
            pkg.setSelected(false);
        }
        packages.get(position).setSelected(true);
        packageFragment.updateView(packages.get(position));
        la.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();
    }

    public boolean isUploadAllowed() {
        if (packageFragment != null) {
            return packageFragment.isUploadAllowed();
        }
        return false;
    }

    public void upload() {
        if (packageFragment != null) {
            packageFragment.upload();
        }
    }

    public void requestPackageDeletion() {
        if (packageFragment != null) {
            packageFragment.requestPackageDeletion();
        }
    }

    public void setPackages(List<HeadsPoint> packages) {
        this.packages = packages;
        if (la != null) {
            la.notifyDataSetChanged();
        }
        if (packageFragment != null) {
            if (packages != null && packages.size() > 0) {
                selectPackage(0);
            } else {
                packageFragment.hide();
            }
        }
    }
}
