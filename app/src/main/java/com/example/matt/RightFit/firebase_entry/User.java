package com.example.matt.RightFit.firebase_entry;

import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Matthew Moua on 2/17/2017.
 */
public class User {

    //User's information is public to be stored in the database
    public String   FirstName,
                    LastName,
                    DisplayName,
                    handle,
                    Email,
                    Phone,
                    City,
                    State,
                    id,
                    uid, // added user id, easier to use with FirebaseUser instance
                    AboutMe,
                    ProfilePicture;
    public ArrayList<String> followers;
    public int  NumberFollowing,
                NumberFollowers,
                LikesReceived;

    public String   fullName;

    //Private password to not allow storage on Firebase database
    private String Password;

    public User() {
        FirstName = LastName = DisplayName = handle = Email = Phone = City = State = id = uid = AboutMe = null;
    }

    //Copy constructor to assign email to a user's full name
    public User(String aEmail, String fullname) {
        Email       = aEmail;
        fullName    = fullname;
    }

    public User(String aFirstName,String aLastName, String aDisplayName, String aHashTag,String aEmail, String aPassword,
                String aPhone,String aCity,String aState,String aId, String aUid, int numberFollowing, int likesReceived,int numberFollowers) {
        FirstName   = aFirstName;
        LastName    = aLastName;
        DisplayName = aDisplayName;
        handle      = aHashTag;
        Email       = aEmail;
        Password    = aPassword;
        Phone       = aPhone;
        City        = aCity;
        State       = aState;
        id          = aId;
        uid         = aUid;
        NumberFollowing = numberFollowing;
        LikesReceived = likesReceived;
        NumberFollowers = numberFollowers;
    }

    //Since phone is optional, need a constructor for one without phone
    public User(String aFirstName, String aLastName, String adisplayName, String aHashTag,String aEmail, String aPassword,
                String aCity, String aState, String aId, String aUid, int numberFollowing, int likesReceived,int numberFollowers) {
        FirstName   = aFirstName;
        LastName    = aLastName;
        DisplayName = adisplayName;
        handle      = aHashTag;
        fullName    = FirstName + " " + String.valueOf(LastName);
        Email       = aEmail;
        Password    = aPassword;
        City        = aCity;
        State       = aState;
        id          = aId;
        uid         = aUid;
        NumberFollowing = numberFollowing;
        LikesReceived = likesReceived;
        NumberFollowers = numberFollowers;
    }

    public static void requestUser(String UserID, String authToken, final User.UserReceivedListener listener) {
        if (listener == null) return;
        FirebaseDatabase.getInstance().getReference("User").child(UserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User usr  = dataSnapshot.getValue(User.class);
                        listener.onUserReceived(usr);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onUserReceived();
                    }
                });
    }

    public static interface UserReceivedListener {
        public void onUserReceived(User... users);
    }

    public static void incLike(final String Usr){
        FirebaseDatabase.getInstance().getReference("User").child(Usr).child("LikesReceived").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() == null){
                    mutableData.setValue(0);
                }
                else{
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count +  1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    public static void incFollowers(final String Usr){
        FirebaseDatabase.getInstance().getReference("User").child(Usr).child("NumberFollowers").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() == null){
                    mutableData.setValue(0);
                }
                else{
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count +  1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    public static void incFollowing(final String Usr){
        FirebaseDatabase.getInstance().getReference("User").child(Usr).child("NumberFollowing").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() == null){
                    mutableData.setValue(0);
                }
                else{
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count +  1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });


    }

    public static void decLike(final String Usr){
        FirebaseDatabase.getInstance().getReference("User").child(Usr).child("LikesReceived").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() == null){
                    mutableData.setValue(0);
                }
                else{
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count -  1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    public static void decFollowers(final String Usr){
        FirebaseDatabase.getInstance().getReference("User").child(Usr).child("NumberFollowers").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() == null){
                    mutableData.setValue(0);
                }
                else{
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count -  1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    public static void decFollowing(final String Usr){
        FirebaseDatabase.getInstance().getReference("User").child(Usr).child("NumberFollowing").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() == null){
                    mutableData.setValue(0);
                }
                else{
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count -  1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });


    }
    /*public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getUserEmail() {
        return Email;
    }

    public String getPhone() {
        return Phone;
    }

    public String getAddress() {
        return Address;
    }

    public String getCity() {
        return City;
    }

    public String getState() {
        return State;
    }*/
}
