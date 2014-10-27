package gr.atc.common.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;

import java.lang.reflect.Field;

//http://stackoverflow.com/questions/14929907/causing-a-java-illegalstateexception-error-no-activity-only-when-navigating-to
/**
 * Created by kGiannakakis on 4/7/2014.
 */
public class CustomListFragment extends ListFragment {
    private static final Field sChildFragmentManagerField;

    static {
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mChildFragmentManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.e("CustomListFragment", "Error getting mChildFragmentManager field", e);
        }
        sChildFragmentManagerField = f;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (sChildFragmentManagerField != null) {
            try {
                sChildFragmentManagerField.set(this, null);
            } catch (Exception e) {
                Log.e("CustomListFragment", "Error setting mChildFragmentManager field", e);
            }
        }
    }

}


