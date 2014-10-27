package gr.atc.heads.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import gr.atc.heads.LoginActivity;
import gr.atc.heads.R;
import gr.atc.heads.HeadsApplication;

/**
 * Created by kGiannakakis on 16/6/2014.
 */
public class AccountFragment extends Fragment {

    static class VersionHelper {

        public static void clearBackStack(Intent intent) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
    }


    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        final HeadsApplication app = (HeadsApplication) getActivity().getApplication();

        ((TextView) view.findViewById(R.id.username_text)).setText(app.getUser().getDisplayName());
        ((TextView) view.findViewById(R.id.email_text)).setText(app.getUser().getEmail());

        Button btn = (Button) view.findViewById(R.id.button_logout);
        btn.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       app.setUser(null);

                                       LoginActivity.logout(getActivity());

                                       Intent intent = new Intent(getActivity(), LoginActivity.class);
                                       if (Build.VERSION.SDK_INT >= 11) {
                                           VersionHelper.clearBackStack(intent);
                                       }
                                       startActivity(intent);

                                       // remove the activity from the back stack
                                       getActivity().finish();
                                       getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                   }
                               }
        );

        return view;
    }

}
