package com.example.matt.rightfit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


public class RegisterActivity extends LoginActivity {

    private EditText first_name,
            last_name,
            display_name,
            handle,
            user_email,
            user_password,
            user_phone,
            user_city,
            user_state,
            activity_points,
            numLikes;
    String firstName, lastName, displayName, handle_, fullname, email, password, phone, city, state, id, uid;
    ImageButton back;

    //Button for when user fills in the texts and then clicks on button
    private Button button;
    //create firebase auth object to store into database
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_with_ui);
        //When user clicks the button, calls function registerUser();
        button = (Button) findViewById(R.id.register);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        //Initializes each text view to the class's objects
        first_name = (EditText) findViewById(R.id.first_name);
        last_name = (EditText) findViewById(R.id.last_name);
        display_name = (EditText) findViewById(R.id.display_name);
        handle = (EditText) findViewById(R.id.hashtag);
        user_email = (EditText) findViewById(R.id.register_email);
        user_password = (EditText) findViewById(R.id.register_password);
        user_phone = (EditText) findViewById(R.id.register_phone);
        user_city = (EditText) findViewById(R.id.register_city);
        user_state = (EditText) findViewById(R.id.register_state);
        back = (ImageButton) findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();
    }

    //Function to save the data onto firebase
    private void saveData() {
        //Create a root reference of database onto the JSON tree provided by firebase
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();

        //DatabaseReference user = RootRef.child("User").push();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        //id = user.getKey();
        uid = fbuser.getUid();

        //creating user and indexing by Firebase UID
        User usr = new User(firstName, lastName, displayName, handle_, email, password, phone, city, state, "000", uid, 0, 0, 0);
        DatabaseReference userRef = RootRef.child("User").child(fbuser.getUid());
        userRef.setValue(usr);
        userRef.child("timestamp_create").setValue(ServerValue.TIMESTAMP);

        //store and set the values associated with the user
        //user.setValue(new User(firstName,lastName,displayName,hashTag,email,password,phone,city,state,id,uid));//data);


    }

    //Stackoverflow
    public void verifyEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent, after email is sent just logout the user and finish this activity
                            FirebaseAuth.getInstance().signOut();
                            finish();
                        } else {
                            // email not sent,restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());

                        }
                    }
                });
    }

    //Added a register button function
    public void registerUser() //https://www.simplifiedcoding.net/android-firebase-tutorial-1/
    {
        firstName = first_name.getText().toString().trim();
        lastName = last_name.getText().toString().trim();
        displayName = display_name.getText().toString().trim();
        handle_ = handle.getText().toString().trim();
        fullname = firstName + " " + String.valueOf(lastName);
        email = user_email.getText().toString().trim();
        password = user_password.getText().toString().trim();
        phone = user_phone.getText().toString().trim();
        city = user_city.getText().toString().trim();
        state = user_state.getText().toString().trim();

        //if handle does not have @, then append it and save to database
        if (!isValid(firstName, lastName, displayName, handle_, phone, email, password, city, state))
            return;
        if (!handle_.contains("@"))
            handle_ = "@" + handle_;


        //Once register button is clicked, will display a progress dialog
        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        //calls firebase auth to create user email and password and store to authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        //Should only save user data to Firebase if successful
                        if (task.isSuccessful()) {
                            saveData();
                            verifyEmail();
                            Toast.makeText(RegisterActivity.this, "Successfully registered, Verify your email!", Toast.LENGTH_LONG).show();
                            //Once successfully registered, changes activity to the login activity
                            //Intent goBackToLogin = new Intent (getApplicationContext(), LoginActivity.class);
                            //startActivity(goBackToLogin);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration Error", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private boolean isValid(String firstname, String lastname, String displayname, String handle1, String phone,
                            String email1, String password1, String city, String state) {
        if (firstname.isEmpty()) {
            first_name.setError(getString(R.string.error_field_required));
            setFocus(first_name);
            return false;
        }
        if (lastname.isEmpty()) {
            last_name.setError(getString(R.string.error_field_required));
            setFocus(last_name);
            return false;
        }
        if (displayname.isEmpty()) {
            display_name.setError(getString(R.string.error_field_required));
            setFocus(display_name);
            return false;
        }
        if (handle1.isEmpty()) {
            handle.setError(getString(R.string.error_field_required));
            setFocus(handle);
            return false;
        }
        if (email1.isEmpty()) {
            user_email.setError(getString(R.string.error_field_required));
            setFocus(user_email);
            return false;
        }
        if (password1.isEmpty()) {
            user_password.setError(getString(R.string.error_field_required));
            setFocus(user_password);
            return false;
        }
        //if(city.isEmpty()) {user_city.setError(getString(R.string.error_field_required)); return false;}
        //if(state.isEmpty()) {user_state.setError(getString(R.string.error_field_required));return false;}

        if (!firstname.matches(".*[a-zA-Z]+.*")) {
            first_name.setError("First Name should contain Letters only");
            setFocus(first_name);
            return false;
        }
        if (!lastname.matches(".*[a-zA-Z]+.*")) {
            last_name.setError("Last Name should contain Letters only");
            setFocus(last_name);
            return false;
        }
        if (!displayname.matches(".*[a-zA-Z0-9_]+.*")) {
            display_name.setError("Display Name should contain Letters, Numbers and Underscore only");
            setFocus(display_name);
            return false;
        }
        if (!handle1.matches("^[a-zA-Z0-9_]*$")) {
            handle.setError("Handle should contain Letters, Numbers and Underscore only");
            setFocus(handle);
            return false;
        }
        if (!email1.contains("@")) {
            user_email.setError("Invalid Email");
            setFocus(user_email);
            return false;
        }
        checkIfHandleExists(handle1);
        //TODO: Password Check
        if (password1.length() <= 4) {
            user_password.setError("At least 6 characters, 1 of which is a Number");
            return false;
        }
        /*
        if(!city.matches(".*[a-zA-Z]+.*")) {user_city.setError("Invalid City"); return false;}
        if(!state.matches(".*[a-zA-Z]+.*") ) {user_state.setError("Invalid State"); return false;}
        */

        return true;
    }

    public void checkIfHandleExists(String handle1) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User");
        //find the single instance of the same handle
        ref.orderByChild("handle").equalTo(handle1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if handle already exists in the database, then show error
                if (dataSnapshot.exists()) {
                    handle.setError("Handle exists! Use a different handle.");
                    setFocus(handle);
                    //TODO: Return false when a handle exists in database already
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setFocus(EditText editText) {
        if (editText.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


}


