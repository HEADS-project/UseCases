package gr.atc.heads.fragments;



import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import gr.atc.common.fragments.CustomFragment;
import gr.atc.common.utils.Base64Coder;
import gr.atc.common.utils.Utils;
import gr.atc.heads.R;
import gr.atc.heads.HeadsApplication;
import gr.atc.heads.io.IHeadsClient;
import gr.atc.heads.io.HeadsClientFactory;
import gr.atc.heads.io.HeadsDeleteListener;
import gr.atc.heads.io.HeadsUploadListener;
import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.Tag;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link gr.atc.heads.fragments.PackageFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PackageFragment extends CustomFragment
        implements HeadsDeleteListener, HeadsUploadListener {
    public static final String PACKAGE_PARAM = "package";
    public static final String TITLE_VISIBLE_PARAM = "titleVisible";

    private HeadsPoint HeadsPackage;
    private boolean isNested;

    private TextView packageTitleTextView;
    private TextView packageCommentsTextView;
    private TextView packageTagsTextView;
    private TextView packageDateTextView;
    private ImageView packageImageView;

    private IHeadsClient headsClient;

    private DisplayImageOptions displayOptions;

    private PackageFragmentListener listener;

    private boolean isBusy;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param HeadsPackage The package to display
     * @return A new instance of fragment PackageFragment.
     */
    public static PackageFragment newInstance(HeadsPoint HeadsPackage, boolean isNested) {
        PackageFragment fragment = new PackageFragment();
        Bundle args = new Bundle();
        args.putSerializable(PACKAGE_PARAM, HeadsPackage);
        args.putBoolean(TITLE_VISIBLE_PARAM, isNested);
        fragment.setArguments(args);
        return fragment;
    }
    public PackageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isBusy = false;

        if (getArguments() != null) {
            HeadsPackage = (HeadsPoint) getArguments().getSerializable(PACKAGE_PARAM);
            isNested = getArguments().getBoolean(TITLE_VISIBLE_PARAM);
        }

        // Retain this fragment across configuration changes.
        setRetainInstance(!isNested);

        displayOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.header_logo)
                .showImageForEmptyUri(R.drawable.header_logo)
                .showImageOnFail(R.drawable.header_logo).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_package, container, false);

        packageImageView = (ImageView) view.findViewById(R.id.bioPackageImg);
        packageTitleTextView = (TextView) view.findViewById(R.id.title);
        packageCommentsTextView = (TextView) view.findViewById(R.id.description);
        packageTagsTextView = (TextView) view.findViewById(R.id.tags);
        packageDateTextView = (TextView) view.findViewById(R.id.date);

        updateView(HeadsPackage);

        return view;
    }

    public void hide() {
        packageImageView.setVisibility(View.GONE);
        packageTitleTextView.setVisibility(View.GONE);
        packageCommentsTextView.setVisibility(View.GONE);
        packageTagsTextView.setVisibility(View.GONE);
        packageDateTextView.setVisibility(View.GONE);
    }

    public void updateView(HeadsPoint HeadsPackage) {
        this.HeadsPackage = HeadsPackage;

        if (packageImageView == null || packageTitleTextView == null ||
                packageCommentsTextView == null ||  packageTagsTextView == null ||
                packageDateTextView == null || getActivity() == null ||
                HeadsPackage == null) {
            return;
        }

        //Load image
        if (HeadsPackage.getLargeImageURL() != null) {
            ImageLoader.getInstance().displayImage(HeadsPackage.getLargeImageURL(),
                                                   packageImageView, displayOptions);
        }
        else if (HeadsPackage.getImagePath() != null) {
            //Bitmap bmp = BitmapFactory.decodeFile(HeadsPackage.getImagePath());
            //packageImageView.setImageBitmap(bmp);
            ImageLoader.getInstance().displayImage("file://" + HeadsPackage.getImagePath(),
                    packageImageView, displayOptions);
        }
        else if (HeadsPackage.getImageUri() != null) {
            //String imageUriString = HeadsPackage.getImageUri();
            //packageImageView.setImageURI(Uri.parse(imageUriString));
            ImageLoader.getInstance().displayImage(HeadsPackage.getImageUri(),
                    packageImageView, displayOptions);
        }
        packageImageView.setVisibility(View.VISIBLE);

        // Title
        packageTitleTextView.setVisibility(isNested ? View.VISIBLE : View.GONE);
        packageTitleTextView.setText(HeadsPackage.getTitle());

        //Comments
        if (HeadsPackage.getDescription().isEmpty()) {
            packageCommentsTextView.setVisibility(View.GONE);
            //packageCommentsTextView.setText(getText(R.string.nodescription));
        }
        else {
            packageCommentsTextView.setVisibility(View.VISIBLE);
            packageCommentsTextView.setText(HeadsPackage.getDescription());
        }

        //Tags
        List<Tag> tags = ((HeadsApplication) getActivity().getApplication()).getTags();
        String tagString = "";
        if (HeadsPackage.getTags()!=null) {
            for (int tagId : HeadsPackage.getTags()) {
                for (Tag tag : tags) {
                    if(tag.getId() == tagId)
                        tagString += tag.getName() + ", ";
                }
            }
        }

        //Remove last character (, )
        if (tagString!=null && !tagString.isEmpty())
            tagString = tagString.substring(0, tagString.length()-2);

        packageTagsTextView.setText(tagString);
        packageTagsTextView.setVisibility(View.VISIBLE);

        //Date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",
                                    getResources().getConfiguration().locale);
        packageDateTextView.setText(sdf.format(HeadsPackage.getCaptureTime()));
        packageDateTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (PackageFragmentListener) activity;
            if (isBusy) {
                listener.onFormIsBusy();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PackageFragmentListener");
        }
    }

    public boolean isUploadAllowed() {
        return HeadsPackage.getId() == null;
    }

    public void upload() {
        HeadsPoint bioImage = new HeadsPoint();
        byte[] imageBytes = null;

        try {
            if (HeadsPackage.getImagePath() != null) {
                imageBytes = Utils.scaleImage(getActivity().getBaseContext(),
                        HeadsPackage.getImagePath(),
                        UploadFragment.MAX_PHOTO_DIMENSION);
            }
            else if (HeadsPackage.getImageUri() != null) {
                Uri imageUri = Uri.parse(HeadsPackage.getImageUri());
                imageBytes = Utils.scaleImage(getActivity().getBaseContext(),
                                              imageUri,
                        UploadFragment.MAX_PHOTO_DIMENSION);
            }
            else {
                listener.onUploadFailed(getResources().getString(R.string.an_error_occurred));
                return;
            }
        } catch (IOException e) {
            Log.i("PackageFragment", e.getMessage());
            listener.onUploadFailed(getResources().getString(R.string.an_error_occurred));
        }

        String encodedData = String.valueOf(Base64Coder.encode(imageBytes));
        //bioImage.setData(encodedData);
        //bioImage.setMetadata(HeadsPackage);

        String userId = ((HeadsApplication) getActivity().getApplication())
                .getUser().getUserName();

        isBusy = true;
        getHeadsClient().startUpload(userId, bioImage);
    }

    public void requestPackageDeletion() {
        if (HeadsPackage.getId() == null) {
            // Local package, delete it
            ((HeadsApplication) getActivity().getApplication()).deleteLocalPackage(HeadsPackage);
            listener.onPackageDeleted(HeadsPackage);
        }
        else {
            isBusy = true;
            getHeadsClient().requestPackageDeletion(HeadsPackage.getId());
        }
    }

    private IHeadsClient getHeadsClient() {
        if (headsClient == null) {
            HeadsClientFactory factory = new HeadsClientFactory(getActivity().getApplicationContext());
            headsClient = factory.createHeadsClient();
            headsClient.setHeadsDeleteListener(this);
            headsClient.setHeadsUploadListener(this);
        }
        return headsClient;
    }

    @Override
    public void deleteFailed(String message) {
        isBusy = false;
        if (listener != null) {
            listener.onDeleteFailed(message);
        }
    }

    @Override
    public void deleteSuccessful() {
        isBusy = false;
        if (listener != null) {
            listener.onPackageDeleted(HeadsPackage);
        }
    }

    @Override
    public void uploadFailed(String message) {
        isBusy = false;
        if (listener != null) {
            listener.onUploadFailed(message);
        }
    }

    @Override
    public void uploadCompleted(String packageId) {
        isBusy = false;
        HeadsPackage.setId(packageId);
        HeadsPackage.setLargeImageUrl(getHeadsClient().getImageUrl(packageId));
        HeadsPackage.setImageURL(getHeadsClient().getThumbnailUrl(packageId));
        if (listener != null) {
            listener.onLocalPackageUploadCompleted(HeadsPackage);
        }
    }

    public interface PackageFragmentListener {
        void onFormIsBusy();
        void onPackageDeleted(HeadsPoint HeadsPackage);
        void onDeleteFailed(String message);
        void onLocalPackageUploadCompleted(HeadsPoint HeadsPackage);
        void onUploadFailed(String message);
    }

}
