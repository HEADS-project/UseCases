package gr.atc.common.imagepicker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kGiannakakis on 13/6/2014.
 */
public class ImagePickerHelper {

    private Activity activity;

    private final int PICTURE_REQUEST_CODE = 1001;

    private Uri outputFileUri;

    private String imagePath;

    private Uri imageUri;

    private String selectSourceLabel = "Select Source";

    public void setSelectSourceLabel(String selectSourceLabel) {
        this.selectSourceLabel = selectSourceLabel;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public ImagePickerHelper(Activity activity) {
        this.activity = activity;
    }

    // Source: http://stackoverflow.com/questions/4455558/allow-user-to-select-camera-or-gallery-for-image
    public boolean openImageIntent(String albumName) {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return false;
        } else {
            return false;
        }

        final File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ), albumName
        );

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return false;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String sec =  new SimpleDateFormat("HHmmss").format(new Date());
        final String fname = timeStamp + albumName + sec + ".jpg";

        final File imageFile = new File(storageDir, fname);
        imagePath = imageFile.getAbsolutePath();
        outputFileUri = Uri.fromFile(imageFile);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

        activity.startActivityForResult(chooserIntent, PICTURE_REQUEST_CODE);

        return true;
    }

    private void addPictureToGallery(Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = uri != null ? uri : Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.MediaColumns.DATA };
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        Cursor cursor = activity.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null)
            return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String realPath = cursor.getString(column_index);
        cursor.close();
        return realPath;
    }

    public String getImagePathFromActivityResult(int requestCode, Intent data) {
        if(requestCode == PICTURE_REQUEST_CODE) {
            final boolean isCamera;
            if(data == null) {
                isCamera = true;
            }
            else {
                final String action = data.getAction();
                if(action == null) {
                    isCamera = false;
                }
                else {
                    isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                }
            }

            if(isCamera) {
                addPictureToGallery(outputFileUri);
            }
            else {
                Uri selectedImageUri = data == null ? null : data.getData();
                imagePath = getRealPathFromURI(selectedImageUri); // from Gallery

                if (imagePath == null)
                    imagePath = selectedImageUri.getPath(); // from File Manager
            }

            return imagePath;
        }
        return null;
    }

    public Uri getImageUriFromActivityResult(int requestCode, Intent data) {
        if (requestCode == PICTURE_REQUEST_CODE) {
            imageUri =  data == null ? null : data.getData();
        }
        return imageUri;
    }

}
