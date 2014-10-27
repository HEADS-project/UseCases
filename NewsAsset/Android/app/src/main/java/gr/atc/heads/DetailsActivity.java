package gr.atc.heads;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import gr.atc.common.utils.Utils;
import gr.atc.heads.fragments.PackageFragment;
import gr.atc.heads.model.HeadsPoint;


public class DetailsActivity extends ActionBarActivity implements PackageFragment.PackageFragmentListener {
    public static final String PACKAGE_PARAM = "package";
    public static final String ALLOW_DELETE_PARAM = "isDeleteAllowed";
    public static final String ACTION_CODE = "actionCode";

    public static final int PACKAGE_DELETED_CODE = 1;
    public static final int PACKAGE_ADDED_CODE = 2;

    private HeadsPoint HeadsPackage;

    private PackageFragment packageFragment;

    private boolean isDeleteAllowed;

    private AlertDialog networkAlertDialog;
    private AlertDialog deleteWarningDialog;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle extras = getIntent().getExtras();
        HeadsPackage = (HeadsPoint) extras.getSerializable(PACKAGE_PARAM);
        isDeleteAllowed = extras.getBoolean(ALLOW_DELETE_PARAM);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(HeadsPackage.getTitle());
        actionBar.setDisplayHomeAsUpEnabled(true);

        packageFragment = PackageFragment.newInstance(HeadsPackage, false);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, packageFragment)
                .commit();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (deleteWarningDialog != null) {
            deleteWarningDialog.dismiss();
        }

        if (networkAlertDialog != null) {
            networkAlertDialog.dismiss();
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(isDeleteAllowed);
        menu.findItem(R.id.action_upload).setVisible(isDeleteAllowed && HeadsPackage.getId() == null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete:
                if (isNetworkAvailable(R.string.network_warning)) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DetailsActivity.this);

                    alertDialogBuilder.setTitle(R.string.delete_item);
                    alertDialogBuilder
                            .setMessage(R.string.delete_warning_message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    onFormIsBusy();
                                    packageFragment.requestPackageDeletion();
                                    deleteWarningDialog = null;
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    deleteWarningDialog = null;
                                }
                            });
                    deleteWarningDialog = alertDialogBuilder.create();
                    deleteWarningDialog.show();
                }
                return true;
            case R.id.action_upload:
                if (isNetworkAvailable(R.string.network_warning)) {
                    onFormIsBusy();
                    packageFragment.upload();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isNetworkAvailable(int warningMessageResId) {
        if (Utils.isNetworkAvailable(this)) {
            return true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getText(R.string.note))
                .setMessage(getText(warningMessageResId))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        networkAlertDialog = null;
                    }
                });

        networkAlertDialog = builder.create();
        networkAlertDialog.show();

        return false;
    }

    @Override
    public void onFormIsBusy() {
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.waiting));
    }

    @Override
    public void onPackageDeleted(HeadsPoint HeadsPackage) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra(ACTION_CODE, PACKAGE_DELETED_CODE);
        returnIntent.putExtra(PACKAGE_PARAM, HeadsPackage);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onDeleteFailed(String message) {
        onError(message);
    }

    @Override
    public void onLocalPackageUploadCompleted(HeadsPoint HeadsPackage) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra(PACKAGE_PARAM, HeadsPackage);
        returnIntent.putExtra(ACTION_CODE, PACKAGE_ADDED_CODE);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onUploadFailed(String message) {
        onError(message);
    }

    private void onError(String message) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (message == null) {
            message = getString(R.string.an_error_occurred);
        }
        Crouton.makeText(this, message, Style.ALERT).show();
    }
}
