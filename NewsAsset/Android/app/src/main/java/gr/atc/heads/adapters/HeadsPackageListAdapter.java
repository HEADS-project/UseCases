package gr.atc.heads.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.Tag;
import gr.atc.heads.R;
import gr.atc.common.utils.Utils;

public class HeadsPackageListAdapter extends BaseAdapter {

    private final int THUMBSIZE = 70;

    private Activity activity;
    private List<HeadsPoint> points;
    private LayoutInflater mInflater = null;
    private boolean haveMore;
    private Context context;

    Bitmap bitmap;

    private List<Tag> tagList = new ArrayList<Tag>();

    private static LruCache<String, Bitmap> thumbnailCache = new LruCache<String, Bitmap>(128);

    private static DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                                        .showImageOnLoading(R.drawable.ic_launcher)
                                        .showImageForEmptyUri(R.drawable.ic_launcher)
                                        .showImageOnFail(R.drawable.ic_launcher)
                                        .cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .build();

    public HeadsPackageListAdapter(Activity a, List<HeadsPoint> list,
                                      boolean haveMoreReports, List<Tag> taglist) {

        tagList = taglist;
        points = list;
        haveMore = haveMoreReports;
        activity = a;
        context = a.getBaseContext();
        mInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public boolean isFooter(int position) {
        return ((haveMore) && (position == points.size()));
    }

    public int getCount() {
        if (points == null) {
            return 0;
        }
        if (haveMore)
            return points.size() + 1;
        else
            return points.size();
    }

    public Object getItem(int position) {
        return points.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public ImageView image;
        public TextView user;
        public TextView date;
        public TextView title;
        public ImageView uploadButton;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooter(position))
            return 2;
        else {
			/*
			 * Report report = packages.get(position); if
			 * (report.getDescription().equalsIgnoreCase("")) return 1; else
			 */
            return 0;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary
        // calls to findViewById() on each row.
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.package_list_row, parent,false);

            // Creates a ViewHolder and store references to the children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.package_imageThumb);
            holder.user = (TextView) convertView.findViewById(R.id.package_user);
            holder.date = (TextView) convertView.findViewById(R.id.package_date);
            holder.title = (TextView) convertView.findViewById(R.id.package_title);
            holder.uploadButton = (ImageView) convertView.findViewById(R.id.upload);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextViews
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        HeadsPoint point = points.get(position);
        if (point != null) {
            holder.user.setText(point.getUsername());
            holder.title.setText(point.getTitle());
            holder.date.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(point.getCaptureTime()));

            if (point.getId() == null) {
                holder.uploadButton.setVisibility(View.VISIBLE);
            }
            else {
                holder.uploadButton.setVisibility(View.GONE);
            }

            // Image downloading handling
            if (point.getImageURL() != null) {
                ImageLoader.getInstance().displayImage(point.getImageURL(), holder.image,
                                                       displayOptions);
            }
            else if (point.getImagePath() != null) {
                Bitmap thumbnail = thumbnailCache.get(point.getImagePath());
                if (thumbnail != null) {
                    holder.image.setImageBitmap(thumbnail);
                }
                else {
                    Log.e("HeadsPackageListAdapter", "Thumbnail for path " + point.getImagePath());
                    try {
                        byte[] imageBytes = Utils.scaleImage(context, point.getImagePath(), THUMBSIZE);
                        Bitmap thumbImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        holder.image.setImageBitmap(thumbImage);
                        thumbnailCache.put(point.getImagePath(), thumbImage);
                    } catch (Exception ex) {

                    }
                }
            }
            else if (point.getImageUri() != null) {
                Bitmap thumbnail = thumbnailCache.get(point.getImageUri());
                if (thumbnail != null) {
                    holder.image.setImageBitmap(thumbnail);
                }
                else {
                    try {
                        Uri imageUri = Uri.parse(point.getImageUri());
                        byte[] imageBytes = Utils.scaleImage(context, imageUri, THUMBSIZE);
                        Bitmap thumbImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        Log.e("HeadsPackageListAdapter", "Thumbnail (" + imageBytes.length +
                                ") for uri " + imageUri.toString());
                        holder.image.setImageBitmap(thumbImage);
                        thumbnailCache.put(point.getImageUri(), thumbImage);
                    } catch (Exception ex) {

                    }
                }
            }
        }

        if (point.isSelected()) {
            convertView.setBackgroundResource(R.drawable.tag_row_bg_selected);
        }
        else {
            convertView.setBackgroundResource(R.drawable.tag_row_bg);
        }


        return convertView;
    }

}
