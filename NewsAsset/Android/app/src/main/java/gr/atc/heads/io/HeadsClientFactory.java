package gr.atc.heads.io;

import android.content.Context;

import gr.atc.heads.R;

/**
 * Created by kGiannakakis on 20/6/2014.
 */
public class HeadsClientFactory {

    private Context context;

    public HeadsClientFactory(Context context) {
        this.context = context;
    }

    public IHeadsClient createHeadsClient() {
        return new HeadsClient(context, context.getString(R.string.services_url));
    }
}
