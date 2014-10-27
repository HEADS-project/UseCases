package gr.atc.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

public final class Utils {
	
	public static final boolean LOG = true;
	
	/* Network Settings */
	// Milliseconds until a connection is established.
	public static final int NETWORK_CONNECTION_TIMEOUT = 60000;
	// Milliseconds to wait for data.
	public static final int NETWORK_SOCKET_TIMEOUT = 10000;
    
    public static byte[] scaleImage(Context context, String photoAbsolutePath, int maxDimension) throws IOException {
        //InputStream is = context.getContentResolver().openInputStream(photoUri);
        
        InputStream is = new FileInputStream(photoAbsolutePath);  
    	BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
       // int orientation = getOrientation(context, Uri.parse(photoUri));
        int orientation = getCameraPhotoOrientation(context,photoAbsolutePath);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        //is = context.getContentResolver().openInputStream(photoUri);
        is = new FileInputStream(photoAbsolutePath); 
        if (rotatedWidth > maxDimension || rotatedHeight > maxDimension) {
            float widthRatio = ((float) rotatedWidth) / ((float) maxDimension);
            float heightRatio = ((float) rotatedHeight) / ((float) maxDimension);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        //String type = context.getContentResolver(). getType(Uri.parse(photoUri));
        String type = photoAbsolutePath.substring(photoAbsolutePath.lastIndexOf(".")+1); 
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.equals("png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.equals("jpg") || type.equals("jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return bMapArray;
    }

    public static byte[] scaleImage(Context context, Uri photoUri, int maxDimension) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);

        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);
        //int orientation = getCameraPhotoOrientation(context, photoAbsolutePath);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        //is = new FileInputStream(photoAbsolutePath);
        if (rotatedWidth > maxDimension || rotatedHeight > maxDimension) {
            float widthRatio = ((float) rotatedWidth) / ((float) maxDimension);
            float heightRatio = ((float) rotatedHeight) / ((float) maxDimension);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        String type = context.getContentResolver().getType(photoUri);
        //String type = photoAbsolutePath.substring(photoAbsolutePath.lastIndexOf(".")+1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.contains("png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.contains("jpg") || type.contains("jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return bMapArray;
    }

    /*
     * Get orientation from gallery
     */
    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }
    
    /* Get orientation from file path using Exif Interface
     * 
     */
    public static int getCameraPhotoOrientation(Context context,String imagePath){
        int rotate = 0;
        try {
           
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

                      
            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            }
            //Log.v(TAG, "Exif orientation: " + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
       return rotate;
    }
    
    
    public static float convertDip(final Context ctx, int pixels) {
        Resources r = ctx.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, r.getDisplayMetrics());
    }
    
    public static void logNetworkError(Exception ex) {
    	if (LOG) Log.e("NETWORK ERROR", ex.toString());
    }
    
    public static void logNetworkRequest(String url) {    	
    	logNetworkRequest(url, "");
    }
    
    public static void logNetworkRequest(String url, String args) {
    	if (LOG) Log.d("NETWORK REQUEST", url + (args.equals("")?" [ARGS] " + args:""));
    }

	public static String getGeocodedAddress(List<Address> geocodedAddresses) {
		if (geocodedAddresses == null || geocodedAddresses.size() == 0) {
			return null;
		}
		
		Address address = geocodedAddresses.get(0);
		
		StringBuffer sb = new StringBuffer();
		sb.append(address.getAddressLine(0));
		
		String ps = address.getPostalCode();
		if (ps != null && ps.length() > 0) {
			sb.append(" ");
			sb.append(ps);
		}
		
		String locality = address.getLocality();
		if (locality != null && locality.length() > 0) {
			sb.append(", ");
			sb.append(locality);
		}
		
		String countryName = address.getCountryName();
		if (countryName != null && countryName.length() > 0) {
			sb.append(" ");
			sb.append(countryName);
		}
		
		String result = sb.toString(); 
		if (result.length() == 0) {
			return null;
		}
		return result;
	}
	
	
	public final static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
	}

}
