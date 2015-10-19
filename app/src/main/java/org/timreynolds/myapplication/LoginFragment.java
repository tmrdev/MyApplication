package org.timreynolds.myapplication;

/**
 * Created by Tim Reynolds
 */

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

import org.timreynolds.myapplication.utility.Logger;

/**
 * LoginFragment - provide Google+ API sign in
 * An account and project is required on the Google Dev Console in order to support oAuth2 authentication and Google+ API access
 *
 * see:
 *  https://developers.google.com/console/help/new/?hl=en_US
 *  https://developers.google.com/+/web/api/rest/oauth
 */
public class LoginFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    private static String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;

    public static final int RC_SIGN_IN = 0;

    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;

    private static final String SAVED_PROGRESS = "sign_in_progress";

    // GoogleApiClient wraps our service connection to Google Play services and
    // provides access to the users sign in state and Google's APIs.
    private GoogleApiClient mGoogleApiClient;

    private int mSignInProgress;

    // Used to store the PendingIntent most recently returned by Google Play
    // services until the user clicks 'sign in'.
    private PendingIntent mySignInIntent;

    // Used to store the error code most recently returned by Google Play services
    // until the user clicks 'sign in'.
    private int mSignInError;

    private SignInButton mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private Button mSearchButton;

    private ImageView mImageProfilePic;
    private TextView mTextName, mTextEmail;
    private LinearLayout mLinearProfileLayout;
    private RelativeLayout mRelativeLoginLayout;
    SharedPreferences preferences;

    /**
     * LoginFragment
     */
    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * onCreateView
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return rootView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mSignInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        mSignOutButton = (Button) rootView.findViewById(R.id.sign_out_button);
        mRevokeButton = (Button) rootView.findViewById(R.id.revoke_access_button);

        mImageProfilePic = (ImageView) rootView.findViewById(R.id.imgProfilePic);
        mTextName = (TextView) rootView.findViewById(R.id.txtName);
        mTextEmail = (TextView) rootView.findViewById(R.id.txtEmail);
        mLinearProfileLayout = (LinearLayout) rootView.findViewById(R.id.llProfile);
        mRelativeLoginLayout = (RelativeLayout) rootView.findViewById(R.id.rLayoutLogin);

        // Button listeners
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);

        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }

        mGoogleApiClient = buildGoogleApiClient();

        mSearchButton =(Button)rootView.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).displaySearchFragment();
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * onAttach
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /**
     * onDetach
     */
    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * GoogleApiClient buildGoogleApiClient
     * set connected and connection failed callbacks
     * add the Google Plus API
     * set the scope to login (using OAuth 2.0)
     *
     * @return builder.build()
     */
    private GoogleApiClient buildGoogleApiClient() {

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN);

        return builder.build();
    }

    /**
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * onStop
     */
    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * onSaveInstanceState
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
    }

    /**
     * onClick
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(MyApplication.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                // We only process button clicks when GoogleApiClient is not transitioning
                // between connected and not connected.
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        mSignInProgress = STATE_SIGN_IN;
                        mGoogleApiClient.connect();
                        break;
                    case R.id.sign_out_button:
                        // We clear the default account on sign out so that Google Play
                        // services will not return an onConnected callback without user
                        // interaction.
                        if (mGoogleApiClient.isConnected()) {
                            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                            mGoogleApiClient.disconnect();
                        }
                        onSignedOut();
                        break;
                    case R.id.revoke_access_button:
                        // After we revoke permissions for the user with a GoogleApiClient
                        // instance, we must discard it and create a new one.
                        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                        // Our sample has caches no user data from Google+, however we
                        // would normally register a callback on revokeAccessAndDisconnect
                        // to delete user data so that we comply with Google developer
                        // policies.
                        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                        mGoogleApiClient = buildGoogleApiClient();
                        mGoogleApiClient.connect();
                        break;
                }
            }
        }
        else{
            Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
        }
    }

    /* onConnected is called when our Activity successfully connects to Google
     * Play services.  onConnected indicates that an account was selected on the
     * device, that the selected account has granted any requested permissions to
     * our app and that we were able to establish a service connection to Google
     * Play services.
     */

    /**
     * onConnected is called when the Activity successfully connects to Google
     * Play services.  onConnected indicates that an account was selected on the
     * device, that the selected account has granted any requested permissions to
     * our app and that we were able to establish a service connection to Google
     * Play services.
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        if(MyApplication.isConnected()) {
            // Reaching onConnected means we consider the user signed in.
            Toast.makeText(getActivity(), "Google+ login successful", Toast.LENGTH_LONG).show();
            // Update the user interface to reflect that the user is signed in.
            mSignInButton.setEnabled(false);
            mSignOutButton.setEnabled(true);
            mRevokeButton.setEnabled(true);
            // Get user's information
            getProfileInformation();
            // Update the UI after signin
            updateLoginStatus(true);
            // Indicate that the sign in process is complete.
            mSignInProgress = STATE_DEFAULT;
        }
        else{
            Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * getProfileInformation - get account information associated with google+ account
     */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                // save profile url for nav drawer display
                preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("my_profile_image_url", personPhotoUrl);
                editor.commit();


                mTextName.setText(personName);
                mTextEmail.setText(email);

                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 2) + PROFILE_PIC_SIZE;

                new LoadProfileImage(mImageProfilePic).execute(personPhotoUrl);

            } else {
                Toast.makeText(getActivity(), "Person information is empty", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    /**
     * updateLoginStatus - if not logged in then show login button, otherwise signed in user
     * will see profile information
     *
     * @param isSignedIn
     */
    private void updateLoginStatus(boolean isSignedIn) {
        if (isSignedIn) {
            mRelativeLoginLayout.setVisibility(View.GONE);
            mLinearProfileLayout.setVisibility(View.VISIBLE);
        } else {
            mRelativeLoginLayout.setVisibility(View.VISIBLE);
            mLinearProfileLayout.setVisibility(View.GONE);
        }
    }

    /**
     * onConnectionFailed is called when the Activity could not connect to Google
     * Play services.  onConnectionFailed indicates that the user needs to select
     * an account, grant permissions or resolve an error in order to sign in.
     *
     * @param result
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Logger.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            // An API requested for GoogleApiClient is not available. The device's current
            // configuration might not be supported with the requested API or a required component
            // may not be installed, such as the Android Wear application. You may need to use a
            // second GoogleApiClient to manage the application's optional APIs.
            Log.w(TAG, "API Unavailable.");
        } else if (mSignInProgress != STATE_IN_PROGRESS) {
            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mySignInIntent = result.getResolution();
            mSignInError = result.getErrorCode();

            if (mSignInProgress == STATE_SIGN_IN) {
                // STATE_SIGN_IN indicates the user already clicked the sign in button
                // so we should continue processing errors until the user is signed in
                // or they click cancel.
                resolveSignInError();
            }
        }

        // In this sample we consider the user signed out whenever they do not have
        // a connection to Google Play services.
        onSignedOut();
    }

    /**
     * Starts an appropriate intent or dialog for user interaction to resolve
     * the current error preventing the user from being signed in.  This could
     * be a dialog allowing the user to select an account, an activity allowing
     * the user to consent to the permissions being requested by your app, a
     * setting to enable device networking, etc.
     */
    private void resolveSignInError() {
        if (mySignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                mSignInProgress = STATE_IN_PROGRESS;
                getActivity().startIntentSenderForResult(mySignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Logger.i(TAG, "Sign in intent could not be sent: " + e.getLocalizedMessage());
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // error types, so we show the default Google Play services error
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            createErrorDialog().show();
        }
    }

    /**
     * onActivityResult
     * Parent MainActivity calls this method directly, otherwise sign in button will need to be clicked twice
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode, data);

        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == getActivity().RESULT_OK) {
                    // If the error resolution was successful we should continue
                    // processing errors.
                    mSignInProgress = STATE_SIGN_IN;
                } else {
                    // If the error resolution was not successful or the user canceled,
                    // we should stop processing errors.
                    mSignInProgress = STATE_DEFAULT;
                }

                if (!mGoogleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    /**
     * onSignedOut - update the login layout to reflect that the user is signed out
     */
    private void onSignedOut() {
        mSignInButton.setEnabled(true);
        mSignOutButton.setEnabled(false);
        mRevokeButton.setEnabled(false);
        updateLoginStatus(false);
    }

    /**
     * onConnectionSuspended - the connection to Google Play services was lost for some reason.
     * call connect() to attempt to re-establish the connection or get a
     * ConnectionResult that we can attempt to resolve.
     *
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {

        mGoogleApiClient.connect();
    }

    /**
     * Dialog createErrorDialog - handle error issues for google login'
     *
     * @return AlertDialog
     */
    private Dialog createErrorDialog() {
        if (GooglePlayServicesUtil.isUserRecoverableError(mSignInError)) {
            return GooglePlayServicesUtil.getErrorDialog(
                    mSignInError,
                    getActivity(),
                    RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Log.e(TAG, "Google Play services resolution cancelled");
                            mSignInProgress = STATE_DEFAULT;
                        }
                    });
        } else {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.play_services_error)
                    .setPositiveButton(R.string.close,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.e(TAG, "Google Play services error could not be "
                                            + "resolved: " + mSignInError);
                                    mSignInProgress = STATE_DEFAULT;
                                }
                            }).create();
        }
    }
}

