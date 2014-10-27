package gr.atc.heads.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import gr.atc.common.utils.Base64Coder;
import gr.atc.common.utils.Utils;
import gr.atc.heads.R;
import gr.atc.heads.HeadsApplication;
import gr.atc.heads.io.HeadsUploadListener;
import gr.atc.heads.model.HeadsPoint;

public class UploadFragment extends HeadsFormFragment implements HeadsUploadListener {
    private static final String IMAGE_PATH_PARAM = "imagePath";
    private static final String IMAGE_URI_PARAM = "uriPath";
    private static final String PACKAGE_PARAM = "package";
    public static final int MAX_PHOTO_DIMENSION = 800;

    private String imagePath;

    private Uri imageUri;

    private ImageView imagePreview;

    private byte[] imageBytes;

    private UploadFragmentListener uploadListener;

    private HeadsPoint newPoint;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location Current user's location
     * @param address Current user's address
     * @param imagePath The file path of the image to upload
     * @param uriPath The Uri of the image to upload
     * @return A new instance of fragment SearchFragment.
     */
    public static UploadFragment newInstance(Location location,
                                             String address,
                                             String imagePath,
                                             Uri uriPath) {
        UploadFragment fragment = new UploadFragment();
        Bundle args = getBundle(location, address);
        args.putString(IMAGE_PATH_PARAM, imagePath);
        args.putParcelable(IMAGE_URI_PARAM, uriPath);
        fragment.setArguments(args);
        return fragment;
    }

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString(IMAGE_PATH_PARAM);
            imageUri = savedInstanceState.getParcelable(IMAGE_URI_PARAM);
            newPoint = (HeadsPoint) savedInstanceState.getSerializable(PACKAGE_PARAM);
        }
        if (getArguments() != null) {
            imagePath = getArguments().getString(IMAGE_PATH_PARAM);
            imageUri = getArguments().getParcelable(IMAGE_URI_PARAM);
        }
        headsClient.setHeadsUploadListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(IMAGE_PATH_PARAM, imagePath);
        outState.putParcelable(IMAGE_URI_PARAM, imageUri);
        outState.putSerializable(PACKAGE_PARAM, newPoint);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);

        view.findViewById(R.id.when_spinner_title).setVisibility(View.GONE);
        view.findViewById(R.id.search_when).setVisibility(View.GONE);

        imagePreview = (ImageView) view.findViewById(R.id.imagePreview);
        imagePreview.setVisibility(View.VISIBLE);
        imagePreview.setImageResource(R.drawable.placeholder);
        imagePreview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                uploadListener.onPickImage();
            }
        });

        if (imagePath != null) {
            onImageSelected(imagePath);
        }

        if (imageUri != null) {
            onImageUriSelected(imageUri);
        }

        descriptionField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (descriptionField.length() > 0) {
                    descriptionField.setError(null);
                }
                else {
                    descriptionField.setError(getString(R.string.description_not_empty));
                }
            }
        });

        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (titleField.length() > 0) {
                    titleField.setError(null);
                }
                else {
                    descriptionField.setError(getString(R.string.description_not_empty));
                }
            }
        });

        Button upload = (Button) view.findViewById(R.id.submit);
        upload.setText(R.string.upload);
        upload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String errorMessage = "";

                boolean isValidated = true;

                String title = titleField.getText().toString();
                if (title.length() == 0) {
                    errorMessage = getString(R.string.title_not_empty);
                    titleField.setError(getString(R.string.title_not_empty));
                    isValidated = false;
                }
                else {
                    titleField.setError(null);
                }

                String description = descriptionField.getText().toString();
                if (description.length() == 0) {
                    errorMessage = getString(R.string.description_not_empty);
                    descriptionField.setError(getString(R.string.description_not_empty));
                    isValidated = false;
                }
                else {
                    descriptionField.setError(null);
                }

                /*String tagsString = tagsField.getText().toString();
                if (tagsString.length() == 0) {
                    if (errorMessage.length() == 0) {
                        errorMessage = getString(R.string.tags_cant_be_empty);
                    }
                    else {
                        errorMessage += "\n" + getString(R.string.tags_cant_be_empty);
                    }
                    tagsField.setError(getString(R.string.tags_cant_be_empty));
                    isValidated = false;
                }
                else {
                    tagsField.setError(null);
                }*/

                if ((imagePath == null || imagePath.length() == 0) && imageUri == null) {
                    if (errorMessage.length() == 0) {
                        errorMessage = getString(R.string.image_cant_be_empty);
                    }
                    else {
                        errorMessage += "\n" + getString(R.string.image_cant_be_empty);
                    }
                    isValidated = false;
                }

                if (errorMessage.length() > 0) {
                    Crouton.makeText(getActivity(), errorMessage, Style.ALERT).show();
                }

                if (location == null) {
                    locationField.setError(getString(R.string.location_cant_be_empty));
                    isValidated = false;
                }

                if (isValidated) {
                    newPoint = new HeadsPoint(
                            location.getLatitude(), location.getLongitude(),
                            System.currentTimeMillis(), keywordsTextView.getText().toString(),
                            title, getTagIds());
                    if (!listener.isNetworkAvailable(R.string.network_upload_warning)) {
                        storeImageLocally();
                        return;
                    }
                    if (!listener.isNetworkAvailable(R.string.network_warning)) {
                        return;
                    }
                    startImageUpload();
                }
            }
        });

        return view;
    }

    private void storeImageLocally() {
        if (imageUri != null) {
            newPoint.setImageUri(imageUri.toString());
        }
        newPoint.setImagePath(imagePath);

        ((HeadsApplication) getActivity().getApplication()).addLocalPackage(newPoint);
        clearView();
        uploadListener.onLocalPackageAdded(newPoint);
    }

    private void startImageUpload() {
        String encodedData = String.valueOf(Base64Coder.encode(imageBytes));
        newPoint.setImage(encodedData);

        String userId = ((HeadsApplication) getActivity().getApplication())
                        .getUser().getUserName();

        isBusy = true;
        uploadListener.onUploadStarted();
        headsClient.startUpload(userId, newPoint);
    }

    private void clearView() {
        imagePath = null;
        imageUri = null;
        imagePreview.setImageResource(R.drawable.placeholder);
        tagsField.setText("");
        titleField.setText("");
        tags = null;
        isLocationSelectedFromMap = false;
        keywordsTextView.setText("");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            uploadListener = (UploadFragmentListener) activity;
            if (isBusy) {
                uploadListener.onFormIsBusy();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUploadFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        uploadListener = null;
    }

    public boolean onImageSelected(String imagePath) {
        this.imagePath = imagePath;
        if (imagePath == null) {
            return false;
        }

        try {
            imageBytes = Utils.scaleImage(getActivity().getBaseContext(), imagePath, MAX_PHOTO_DIMENSION);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imagePreview.setImageBitmap(bitmap);
            return true;
        } catch (IOException e) {
            Log.i("UploadFragment", e.getMessage());
        }
        return false;
    }

    public boolean onImageUriSelected(Uri imageUri) {
        this.imageUri = imageUri;
        if (imageUri == null) {
            return false;
        }

        try {
            imageBytes = Utils.scaleImage(getActivity().getBaseContext(), imageUri, MAX_PHOTO_DIMENSION);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imagePreview.setImageBitmap(bitmap);
            return true;
        } catch (IOException e) {
            Log.i("UploadFragment", e.getMessage());
        }
        return false;
    }

    @Override
    public void uploadFailed(String message) {
        isBusy = false;
        uploadListener.onUploadFailed(message);
    }

    @Override
    public void uploadCompleted(String packageId) {
        isBusy = false;
        newPoint.setId(packageId);
        newPoint.setLargeImageUrl(headsClient.getImageUrl(packageId));
        newPoint.setImageURL(headsClient.getThumbnailUrl(packageId));
        clearView();
        if (uploadListener != null) {
            uploadListener.onUploadCompleted(newPoint);
        }
    }

    public interface UploadFragmentListener extends FormFragmentListener {
        void onPickImage();

        void onUploadStarted();
        void onUploadCompleted(HeadsPoint HeadsPackage);
        void onUploadFailed(String message);

        void onLocalPackageAdded(HeadsPoint localPackage);
    }
}
