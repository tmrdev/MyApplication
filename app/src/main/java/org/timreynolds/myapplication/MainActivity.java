package org.timreynolds.myapplication;

/**
 * Created by Tim Reynolds
 */

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.timreynolds.myapplication.gcm.QuickstartPreferences;
import org.timreynolds.myapplication.gcm.RegistrationIntentService;
import org.timreynolds.myapplication.utility.Logger;
import org.timreynolds.myapplication.utility.PrefsFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.MailTo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Android Google+ Sign-In (Android client only)
 * NOTE: Using android debug key sha1 in google dev console, update to production key if needed
 *
 * NOTE: Using volley.jar instead of the source repo from git, see build.gradle (Module) for more info
 *
 */
public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    private static String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    // Set email address for Contact section/link in left drawer
    public static final String EMAIL_ADDRESS = "mailto:your_email@mail.com";

    // for gcm services
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    // end gcm services

    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // for gcm services
        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                Logger.i(TAG, getString(R.string.registering_message));
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    //mInformationTextView.setText(getString(R.string.gcm_send_message));
                    Logger.i(TAG, getString(R.string.gcm_send_message));
                } else {
                    //mInformationTextView.setText(getString(R.string.token_error_message));
                    Logger.i(TAG, getString(R.string.token_error_message));
                }
            }
        };
        //mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        // end gcm services

        drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);
        // display the first navigation drawer view on app launch
        displayView(0);
    }

    /**
     * displayView - set which fragment view should be displayed. LoginFragment is the default
     * NOTE: a fragment title is being set to allow for referencing of fragments by Tag name
     *
     * @param position
     */
    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        String fragmentTitle = null;
        switch (position) {
            case 0:
                fragment = new LoginFragment();
                title = getString(R.string.title_home);
                fragmentTitle = "home";
                break;
            case 1:
                fragment = new SearchFragment();
                title = getString(R.string.title_search);
                fragmentTitle = "search";
                break;
            case 2:
                MailTo mt = MailTo.parse(EMAIL_ADDRESS);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{mt.getTo()});
                i.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
                i.putExtra(Intent.EXTRA_CC, mt.getCc());
                i.putExtra(Intent.EXTRA_TEXT, mt.getBody());
                startActivity(i);

                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // NOTE: Fragment Title is getting set with 3rd parameter
            fragmentTransaction.replace(R.id.container_body, fragment, fragmentTitle);
            fragmentTransaction.commit();
            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * displaySearchFragment - used for direct link to SearchFragment from default Login page
     */
    public void displaySearchFragment(){
        Fragment searchFragment = new SearchFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, searchFragment, "search");
        // this adds fragment to back button stack
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        // set the toolbar title
        getSupportActionBar().setTitle("Search Google+");
    }

    /**
     * onCreateOptionsMenu - create options for app menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * onOptionsItemSelected
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            Intent i = new Intent(this, PrefsFragment.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onDrawerItemSelected - display relevant Fragment/Link based on int position
     * see displayView for cases
     *
     * @param view
     * @param position
     */
    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    /**
     * onActivityResult - if RC_SIGN_IN is set then call the method in the LoginFragment class
     * to complete the Google login process
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LoginFragment.RC_SIGN_IN) {
            LoginFragment fragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("home");
            fragment.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
