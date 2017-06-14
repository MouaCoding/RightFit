package com.example.matt.RightFit.custom.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.example.matt.RightFit.R;
import com.example.matt.RightFit.ViewUserActivity;
import com.example.matt.RightFit.firebase_entry.FollowingUser;
import com.example.matt.RightFit.firebase_entry.User;
import com.example.matt.RightFit.util.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by Bradley Wang on 3/6/2017.
 * Edited by Matt
 */

public class UserMiniHeaderView extends FrameLayout {
    private ImageView miniProfilePicView;
    private TextView displayNameView;
    private TextView userHandleView;
    private ImageButton moreButton;
    private ImageButton addButton;

    private String otherUserID;
    private String curUserID;

    public UserMiniHeaderView(Context context) {
        super(context);
        createView();
    }

    public UserMiniHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    public void createView() {
        inflate(getContext(), R.layout.user_mini_header_layout, this);
        miniProfilePicView = (ImageView) findViewById(R.id.mini_profile_picture);
        miniProfilePicView.clearFocus();
        //Clicking either user's profile picture or their name will start a view user activity
        miniProfilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewUserActivity.class);
                intent.putExtra("otherUserID",otherUserID);
                getContext().startActivity(intent);
            }
        });
        displayNameView = (TextView) findViewById(R.id.mini_name);
        displayNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewUserActivity.class);
                intent.putExtra("otherUserID",otherUserID);
                getContext().startActivity(intent);
            }
        });
        userHandleView = (TextView) findViewById(R.id.mini_user_handle);
        moreButton = (ImageButton) findViewById(R.id.mini_profile_more_button);

        addButton = (ImageButton) findViewById(R.id.mini_profile_add_button);
        if (otherUserID != FirebaseAuth.getInstance().getCurrentUser().getUid()) {
            followButton();
        }
    }


    public void anonUser(){
        displayNameView.setText("Anonymous");
        displayNameView.setClickable(false);
        userHandleView.setText("");
        userHandleView.setClickable(false);
        miniProfilePicView.setImageResource(R.drawable.happy);
        miniProfilePicView.setClickable(false);
        addButton.setVisibility(View.GONE);

    }

    private void followButton() {
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curUserID != null && curUserID != otherUserID) {
                    Toast.makeText(getApplicationContext(),"Followed!", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference("User").child(curUserID).child("Following")
                            .child(otherUserID).setValue(new FollowingUser());
                    incrementFollowers(otherUserID);
                   }
            }
        });
    }

    public void incrementFollowers(final String otherID)
    {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child(otherID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User follow = dataSnapshot.getValue(User.class);
                int followerNumber = follow.NumberFollowers;
                followerNumber++;
                ref.child("NumberFollowers").setValue(followerNumber);
                FirebaseDatabase.getInstance().getReference("User").child(otherID).child("Followers")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new FollowingUser());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getUser(final String userID) {
        // TODO fetch event information from params and fill fields
        User.requestUser(userID, "AuthToken", new User.UserReceivedListener() {
            @Override
            public void onUserReceived(User...users) {
                User usr = users[0];
                setUser(usr);
            }
        });
    }


    public void setUser(User user){
        displayNameView.setText(user.DisplayName);
        userHandleView.setText(user.handle);
        otherUserID = user.uid;
        curUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try {
            Picasso.with(getContext()).load(user.ProfilePicture).transform(new CircleTransform()).into(miniProfilePicView);
        }catch (Exception e){}
    }

    //stackoverflow function
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch( Exception e) {
            return null;
        }
    }

}
