package gr.atc.heads.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gr.atc.heads.R;
import gr.atc.heads.TagSelectActivity;
import gr.atc.heads.HeadsApplication;
import gr.atc.heads.io.IHeadsClient;
import gr.atc.heads.io.HeadsClientFactory;
import gr.atc.heads.model.TagModel;

abstract class HeadsFormFragment extends Fragment {

    protected static final String LOCATION_PARAM = "location";
    protected static final String ADDRESS_PARAM = "address";

    private final String LOCATION_SELECTED_FROM_MAP = "location_from_map";

    protected Location location;
    protected String address;

    protected EditText keywordsTextView;

    protected EditText tagsField;

    protected EditText locationField;

    protected EditText titleField;

    protected EditText descriptionField;

    protected IHeadsClient headsClient;

    protected FormFragmentListener listener;

    protected boolean isLocationSelectedFromMap;

    protected List<TagModel> tags;

    protected boolean isBusy;

    protected static Bundle getBundle(Location location, String address) {
        Bundle args = new Bundle();
        args.putParcelable(LOCATION_PARAM, location);
        args.putString(ADDRESS_PARAM, address);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        isBusy = false;

        if (getArguments() != null) {
            location = getArguments().getParcelable(LOCATION_PARAM);
            address = getArguments().getString(ADDRESS_PARAM);
        }
        if (savedInstanceState != null) {
            isLocationSelectedFromMap = savedInstanceState.getBoolean(LOCATION_SELECTED_FROM_MAP);
            location = savedInstanceState.getParcelable(LOCATION_PARAM);
            address = savedInstanceState.getString(ADDRESS_PARAM);
        }
        else {
            isLocationSelectedFromMap = false;
        }

        HeadsClientFactory factory = new HeadsClientFactory(getActivity().getApplicationContext());
        headsClient = factory.createHeadsClient();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(LOCATION_SELECTED_FROM_MAP, isLocationSelectedFromMap);
        outState.putParcelable(LOCATION_PARAM, location);
        outState.putString(ADDRESS_PARAM, address);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_form, container, false);

        tagsField = (EditText) view.findViewById(R.id.search_tags);
        tagsField.setFocusable(false);
        tagsField.setClickable(true);
        tagsField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TagSelectActivity.class);
                intent.putExtra(TagSelectActivity.TAGS_PARAM, (Serializable) tags);
                getActivity().startActivityForResult(intent, HeadsApplication.TAG_SELECT_REQUEST_CODE);
            }
        });

        titleField = (EditText) view.findViewById(R.id.package_title);

        descriptionField = (EditText) view.findViewById(R.id.description);

        keywordsTextView = (EditText) view.findViewById(R.id.description);

        locationField = (EditText) view.findViewById(R.id.search_where);
        locationField.setFocusable(false);
        locationField.setClickable(true);
        locationField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                listener.onLocationSelect(location);
            }
        });

        if (address != null) {
            locationField.setText(address);
        } else if (location != null) {
            locationField.setText(String.format("%f, %f",
                    location.getLatitude(), location.getLongitude()));
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (FormFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFormFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    protected List<Integer> getTagIds() {
        List<Integer> tagIds = new ArrayList<Integer>();

        if (tags != null) {
            for(TagModel tag: tags) {
                if (tag.isSelected()) {
                    tagIds.add((int) tag.getId());
                }
            }
        }
        return tagIds;
    }

    protected List<String> getTagIdsAsString() {
        List<String> tagIds = new ArrayList<String>();

        if (tags != null) {
            for(TagModel tag: tags) {
                if (tag.isSelected()) {
                    tagIds.add(String.valueOf(tag.getId()));
                }
            }
        }
        return tagIds;
    }

    public void onLocationUpdated(Location location) {
        if (!isLocationSelectedFromMap) {
            this.location = location;
            if (address == null && location != null && locationField != null) {
                locationField.setText(String.format("%f, %f",
                        location.getLatitude(), location.getLongitude()));
            }
        }
    }

    public void onAddressUpdated(String address) {
        if (!isLocationSelectedFromMap && locationField != null) {
            this.address = address;
            locationField.setText(address);
        }
    }

    public void onLocationFromMapUpdated(Location location) {
        isLocationSelectedFromMap = true;
        this.location = location;
        this.address = null;
        if (location != null && locationField != null) {
            locationField.setText(String.format("%f, %f",
                    location.getLatitude(), location.getLongitude()));
            (new GetAddressFromNetworkTask(getActivity())).execute(location);
        }
    }

    public void onTagsUpdated(List<TagModel> tags) {
        this.tags = tags;

        if (tags != null && tags.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (TagModel tag : tags) {
                if (tag.isSelected()) {
                    if (builder.length() > 0) {
                        builder.append(", ");
                    }
                    builder.append(tag.getName());
                }
            }
            tagsField.setText(builder.toString());
        }
    }

    public interface FormFragmentListener {
        void onLocationSelect(Location location);
        void onFormIsBusy();
        boolean isNetworkAvailable(int warningMessageResId);
    }

    protected class GetAddressFromNetworkTask extends AsyncTask<Location, Void, String> {

        Context localContext;

        public GetAddressFromNetworkTask(Context context) {

            super();

            localContext = context;
        }


        @Override
        protected String doInBackground(Location... params) {
            gr.atc.common.location.Geocoder geocoder =
                    new gr.atc.common.location.Geocoder(localContext);

            Location location = (Location) params[0];

            try {
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(), 2);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    return address.getFeatureName();
                }
            } catch (Exception ex) {
                Log.e("HeadsFormFragment", ex.getMessage());
            }

            return getString(R.string.no_address_found);
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(String address) {
            locationField.setText(address);
        }
    }

}
