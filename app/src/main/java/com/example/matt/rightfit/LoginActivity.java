
package com.example.matt.rightfit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.User;
import com.example.matt.rightfit.util.RHRNNotifications;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;

import retrofit2.Call;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnClickListener {
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int LOCATIONS_PERMISSION = 0;
    private static final int RC_SIGN_IN = 9001;

    // UI references.
    public AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    TextView forgotPassword;

    //Firebase authentication object
    private FirebaseAuth firebaseAuth;

    //process dialog to display to user
    public ProgressDialog progressDialog;

    private CallbackManager callbackManager;


    //Google sign in
    private GoogleApiClient mGoogleApiClient;

    //Twitter Login
    private TwitterLoginButton twitterLoginButton;

    private ProgressDialog pd;

    CheckBox keeplog;
    boolean isChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // hide keyboard until clicked

        //Setup a Firebase object
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        //set email object to user's text input
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        //Set password
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    //If login matches
                    signIn();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        //If register button is clicked, switches activity to RegisterActivity
        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATIONS_PERMISSION);
        }

        forgotPassword = (TextView) findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(intent);

            }
        });

        //Init google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        findViewById(R.id.sign_in_button).setOnClickListener(this);

        facebookRegister();


        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                twitterSignIn(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "Failed to Log In!", Toast.LENGTH_LONG).show();
            }
        });

        //Checkbox of keep me logged in
        keeplog = (CheckBox) findViewById(R.id.keeplog);
        keeplog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //if clicked, then draws a check and set PREFS_NAME
                SharedPreferences settings = getSharedPreferences("com.example.rhrn.RightHereRightNow", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("isChecked", isChecked);
                editor.commit();
            }
        });

        //Sets settings1 to PREFS_NAME and set it to the boolean isChecked
        SharedPreferences settings1 = getSharedPreferences("com.example.rhrn.RightHereRightNow", 0);
        isChecked = settings1.getBoolean("isChecked", false);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (isChecked & firebaseUser != null) {
            RHRNNotifications.subscribeToMessages();

            RHRNNotifications.subscribeToFollows();

            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else {
            //do nothing
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                googleRegister(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    //Facebook register user
    private void facebookRegister() {
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_about_me", "user_photos"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                facebookSignIn(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
            }
        });
    }

    private void facebookSignIn(AccessToken accessToken) {
        showProgressDialog();
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
                            final Profile profile = Profile.getCurrentProfile();
                            final User usr = new User(profile.getFirstName(), profile.getLastName(), null, null, null, null, null, null, null, "000", user.getUid(), 0, 0, 0);

                            //updateUI(user);
                            String msg = "Successfully logged in as " + Profile.getCurrentProfile().getFirstName();
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            RootRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    LoginManager.getInstance().logOut();
                                    if (dataSnapshot.hasChild(user.getUid())) {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), AlmostDoneActivity.class);
                                        intent.putExtra("first_name", profile.getFirstName());
                                        intent.putExtra("last_name", profile.getLastName());
                                        intent.putExtra("profile_picture", profile.getProfilePictureUri(100, 100));
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        pd.dismiss();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void googleSignIn() {
        googleSignOut();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void googleSignOut() {
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //updateUI(null);
                    }
                });
    }

    public void showProgressDialog() {
        pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Loading...");
        pd.show();
    }

    private void googleRegister(final GoogleSignInAccount acct) {
        final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        showProgressDialog();
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            //updateUI(user);

                            Toast.makeText(LoginActivity.this, "Successfully Logged in as " + acct.getDisplayName(), Toast.LENGTH_SHORT).show();
                            RootRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user.getUid())) {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), AlmostDoneActivity.class);
                                        intent.putExtra("first_name", acct.getGivenName());
                                        intent.putExtra("last_name", acct.getFamilyName());
                                        intent.putExtra("email", acct.getEmail());
                                        intent.putExtra("profile_picture", acct.getPhotoUrl());
                                        intent.putExtra("uid", acct.getId());
                                        startActivity(intent);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        pd.dismiss();
                    }
                });

    }

    private void twitterSignIn(final TwitterSession session) {
        showProgressDialog();
        final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        TwitterSession session1 = Twitter.getSessionManager().getActiveSession();
        Call<com.twitter.sdk.android.core.models.User> call = Twitter.getApiClient(session1).getAccountService().verifyCredentials(true, true);
        call.enqueue(new Callback<com.twitter.sdk.android.core.models.User>() {
            @Override
            public void success(Result<com.twitter.sdk.android.core.models.User> userResult) {
                com.twitter.sdk.android.core.models.User usr = userResult.data;
                //FirebaseAuth.getInstance().getCurrentUser().updateEmail(usr.email);
                try {
                    User curUser = new User(usr.name, usr.name, usr.screenName, "@" + usr.screenName, usr.email, null, null, usr.location, usr.location, "000", FirebaseAuth.getInstance().getCurrentUser().getUid(), 0, 0, 0);
                    RootRef.child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(curUser);
                }catch(Exception e){}
            }

            @Override
            public void failure(TwitterException e) {

            }

        });

        final AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            //updateUI(user);

                            String msg = "Successfully logged in as @" + session.getUserName() + " (#" + session.getUserId() + ")";
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        pd.dismiss();
                    }
                });
    }

    //added a sign in function
    private void signIn() {
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        //if empty email, display error
        if (TextUtils.isEmpty(email)) {
            //get the string from strings.xml and display it
            mEmailView.setError(getString(R.string.error_field_required));
            return;
        } else if (!isEmailValid(email)) {
            //if not empty then it's wrong data
            mEmailView.setError(getString(R.string.error_invalid_email));
        }

        //if password is empty
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            return;
        } else if (!isPasswordValid(password)) {
            //if password is incorrect
            mPasswordView.setError(getString(R.string.error_invalid_password));
        }

        //Displays the process display of logging in
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        //Authenticate with firebase backend
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //if successfully logs in, displays success,
                            Toast.makeText(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            //if failed, display error message
                            Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_LONG).show();
                        }
                        //once done, process dialog disappears
                        progressDialog.dismiss();
                    }
                });
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            googleSignIn();
        }
        //else if (i == R.id.sign_out_button) {googleSignOut();}
    }
}

