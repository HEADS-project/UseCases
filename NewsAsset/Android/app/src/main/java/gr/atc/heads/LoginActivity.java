package gr.atc.heads;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import gr.atc.common.location.LocationAwareActivity;
import gr.atc.common.utils.Utils;
import gr.atc.heads.io.IHeadsClient;
import gr.atc.heads.io.HeadsClientFactory;
import gr.atc.heads.io.HeadsLoginListener;
import gr.atc.heads.model.Tag;
import gr.atc.heads.model.User;

public class LoginActivity extends Activity implements HeadsLoginListener{

    private final static String USER_KEY = "User";
    private final static String TAGS_KEY = "Tags";

    private IHeadsClient headsClient;

    private ProgressDialog progressDialog;

    private boolean isRegistering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isRegistering = false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userStr = prefs.getString(USER_KEY, null);

        if (userStr != null) {
            User user = User.create(userStr);
            Type listType = new TypeToken<ArrayList<Tag>>(){}.getType();

            ((HeadsApplication) getApplication()).setUser(user);

            gotoMainActivity();
            return;
        }

        //If there is no Internet connection alert the user and kill the application
        if (!Utils.isNetworkAvailable(this))
        {
            //Show message
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.note))
                    .setMessage(getText(R.string.nointernet))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Kill the application process
                            int pid = android.os.Process.myPid();
                            android.os.Process.killProcess(pid);
                        }
                    });

            final AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        HeadsClientFactory factory = new HeadsClientFactory(getApplicationContext());
        headsClient = factory.createHeadsClient();
        headsClient.setHeadsLoginListener(this);

        final Button login = (Button) findViewById(R.id.button_login);

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isRegistering) {
                    doRegister();
                }
                else {
                    doLogin();
                }
            }
        });

        final TextView registerLink = (TextView) findViewById(R.id.login_register);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRegistering = !isRegistering;
                if (isRegistering) {
                    findViewById(R.id.confirm_pass_label).setVisibility(View.VISIBLE);
                    findViewById(R.id.confirm_pass_text).setVisibility(View.VISIBLE);
                    registerLink.setText(R.string.loginLinkMessasge);
                    login.setText(R.string.register);
                }
                else {
                    findViewById(R.id.confirm_pass_label).setVisibility(View.GONE);
                    findViewById(R.id.confirm_pass_text).setVisibility(View.GONE);
                    registerLink.setText(R.string.registerLinkMessasge);
                    login.setText(R.string.login);
                }
            }
        });

    }

    private void doLogin() {
        EditText usernameEditText = ((EditText) findViewById(R.id.user_text));
        EditText passwordEditText = ((EditText) findViewById(R.id.pass_text));

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        usernameEditText.setError(null);
        passwordEditText.setError(null);

        if (username.length()!=0 && password.length()!=0) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.waiting));
            headsClient.startLogin(username, password);
        }
        else {
            if (username.length()==0) {
                usernameEditText.setError(getString(R.string.username_not_empty));
                usernameEditText.requestFocus();
            }
            else if (password.length()==0) {
                passwordEditText.setError(getString(R.string.password_not_empty));
                passwordEditText.requestFocus();
            }
        }
    }

    private void doRegister() {
        EditText usernameEditText = ((EditText) findViewById(R.id.user_text));
        EditText passwordEditText = ((EditText) findViewById(R.id.pass_text));
        EditText confirmPasswordEditText = ((EditText) findViewById(R.id.confirm_pass_text));

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        usernameEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);

        if (username.length()!=0 && password.length()!=0 && confirmPassword.length()!=0 &&
                confirmPassword.equals(password)) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.waiting));
            headsClient.startRegister(username, password);
        }
        else {
            if (username.length()==0) {
                usernameEditText.setError(getString(R.string.username_not_empty));
                usernameEditText.requestFocus();
            }
            else if (password.length()==0) {
                passwordEditText.setError(getString(R.string.password_not_empty));
                passwordEditText.requestFocus();
            }
            else if (confirmPassword.length()==0) {
                confirmPasswordEditText.setError(getString(R.string.password_not_empty));
                confirmPasswordEditText.requestFocus();
            }
            else if (!confirmPassword.equals(password)) {
                Crouton.makeText(LoginActivity.this, R.string.passwords_not_match, Style.ALERT).show();
            }
        }
    }

    @Override
    public void loginFailed(String message) {
        if (progressDialog != null)
            progressDialog.dismiss();

        if (message == null) {
            message = getString(R.string.an_error_occurred);
        }
        Crouton.makeText(LoginActivity.this, message, Style.ALERT).show();
    }

    @Override
    public void loginSuccessful(User user) {
        ((HeadsApplication) getApplication()).setUser(user);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_KEY, user.serialize());
        editor.commit();

        //headsClient.requestTags();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        gotoMainActivity();
    }

    @Override
    public void registerSuccessful() {
        EditText usernameEditText = ((EditText) findViewById(R.id.user_text));
        EditText passwordEditText = ((EditText) findViewById(R.id.pass_text));

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        headsClient.startLogin(username, password);
    }

    @Override
    public void tagsReceived(List<Tag> tags) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        for(Tag tag: tags) {
            Log.d("LoginActivity", tag.getId() + " " + tag.getName());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TAGS_KEY, new Gson().toJson(tags));
        editor.commit();

        ((HeadsApplication) getApplication()).setTags(tags);

        gotoMainActivity();
    }

    private void gotoMainActivity() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String updateIntervalStr = prefs.getString("location_update_frequency", "60");
        int updateInterval = 60;
        updateInterval = Integer.parseInt(updateIntervalStr);
        boolean tabbedNavigation = prefs.getBoolean("tabbed_navigation", true);

        Intent intent;

        if (tabbedNavigation) {
            intent  = new Intent(this, MainTabsActivity.class);
        }
        else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.putExtra(LocationAwareActivity.UPDATE_INTERVAL_PARAM, updateInterval);
        startActivity(intent);

        // remove the activity from the back stack
        LoginActivity.this.finish();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    public static void logout(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_KEY, null);
        editor.putString(TAGS_KEY, null);
        editor.commit();
    }
}
