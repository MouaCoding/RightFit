package com.example.matt.rightfit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.Event;
import com.example.matt.rightfit.firebase_entry.Post;
import com.example.matt.rightfit.firebase_entry.Shares;
import com.example.matt.rightfit.firebase_entry.User;
import com.example.matt.rightfit.util.CircleTransform;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Bradley Wang on 2/13/2017.
 * Edit by Matt on 3/8/201 --> Queried displayName and updated the profile page name
 * Edit by Matt 3/28/2017-4/1/2017 --> Populate profile, change profile pic via upload/capture
 */
public class ProfilePageFragment extends Fragment {
    public TextView userName,
            hash_tag,
            numberFollowers,
            numberFollowing,
            numLikes,
            about,
            morePosts,
            moreEvents,
            moreSharedPosts,
            moreSharedEvents;
    public EditText profileMain;
    public ImageView profilePicture, edit, editDisplay;
    public ImageButton changeProfile, options;

    //Posts
    public ImageView miniProfilePicture;
    public TextView miniUserName,
            miniHandle,
            body,
            postsNumLikes,
            postsNumShares,
            postsNumComments,
            profileFollowing,
            profileFollowers;

    //Populating list of posts and events
    public ListView postList, eventList, sharedPosts, sharedEvents;
    public ArrayList<Post> postArray, sharedPostArray;
    public ArrayList<Event> eventArray, sharedEventArray;
    public NotificationFragment.PostAdapter postAdapter;
    public TrendingFragment.EventAdapter eventAdapter;
    public SharingAdapters.SharedPostAdapter sharedPostAdapter;
    public SharingAdapters.SharedEventAdapter sharedEventAdapter;



    public User temp;
    ProgressDialog pd;
    private static final int SELECT_PICTURE = 100;
    //request int for using camera
    private static final int CAPTURE_PICTURE = 200;
    private static final int ABOUT_ME = 300;
    public FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();

    // creating an instance of Firebase Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    //creating a storage reference.
    StorageReference storageRef = storage.getReferenceFromUrl("gs://rightfit-5ea19.appspot.com");
    Uri filePath;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.profile_page_layout, container, false);

        //The below code is for handling exception of querying the profile picture:
        //exception is thrown when application attempts to perform a networking operation in the main thread.
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        userName = (TextView) r.findViewById(R.id.profile_name_main);
        hash_tag = (TextView) r.findViewById(R.id.profile_userhandle);
        numberFollowers = (TextView) r.findViewById(R.id.profile_followers_value);
        numberFollowing = (TextView) r.findViewById(R.id.profile_number_following);
        numLikes = (TextView) r.findViewById(R.id.profile_karma_value);
        about = (TextView) r.findViewById(R.id.profile_about_text);
        options = (ImageButton) r.findViewById(R.id.profile_app_bar_options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu();
            }
        });
        profilePicture = (ImageView) r.findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Enlarge the profile picture
            }
        });
        profileFollowing = (TextView) r.findViewById(R.id.profile_following);
        profileFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FollowingListActivity.class);
                startActivity(intent);
            }
        });
        profileFollowers = (TextView) r.findViewById(R.id.profile_followers_label);
        profileFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FollowerListActivity.class);
                startActivity(intent);
            }
        });
        changeProfile = (ImageButton) r.findViewById(R.id.change_profile_picture);
        changeProfile.requestFocus();
        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MM: I decided to use an alert dialog to display a popup which has 3 clickable buttons
                // For changing profile picture.
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
                dlgAlert.setMessage("Capture New Image or Upload an Image");
                dlgAlert.setTitle("Profile Picture Change");

                //type doesnt matter, but order of buttons do
                //If user wants to upload from gallery
                dlgAlert.setNegativeButton("Upload", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Opens the gallery of the phone if user clicked "Upload"
                        Toast.makeText(getApplicationContext(), "Upload a Picture", Toast.LENGTH_LONG).show();
                        Intent i = new Intent();
                        i.setType("image/*");
                        i.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
                    }
                });
                //if user decides to take a new picture
                dlgAlert.setNeutralButton("Capture", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Capture", Toast.LENGTH_LONG).show();
                        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(imageIntent, CAPTURE_PICTURE);
                    }
                });
                //if user cancels
                dlgAlert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });

                dlgAlert.setCancelable(true);
                dlgAlert.create();
                dlgAlert.show();

            }
        });
        edit = (ImageView) r.findViewById(R.id.edit_about_me); //clicked pencil edit
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AboutMeActivity.class);
                startActivityForResult(intent,ABOUT_ME);
            }
        });

        profileMain = (EditText) r.findViewById(R.id.profile_name_main);
        editDisplay = (ImageView) r.findViewById(R.id.edit_display); //clicked pencil edit
        editDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDisplayName = profileMain.getText().toString().trim();
                editDisplay.clearFocus();
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(editDisplay.getWindowToken(), 0);
                Toast.makeText(getApplicationContext(), "Display Name Changed to " + newDisplayName, Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference().child("User")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("DisplayName").setValue(newDisplayName);
            }
        });

        morePosts = (TextView) r.findViewById(R.id.more_posts);
        morePosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MorePostsActivity.class);
                intent.putExtra("userKey", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
            }
        });

        moreEvents = (TextView) r.findViewById(R.id.more_events);
        moreEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoreEventsActivity.class);
                intent.putExtra("userKey", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
            }
        });

        moreSharedPosts = (TextView) r.findViewById(R.id.more_shared_posts);
        moreSharedPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoreSharedPostsActivity.class);
                intent.putExtra("userKey", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
            }
        });

        moreSharedEvents = (TextView) r.findViewById(R.id.more_shared_events);
        moreSharedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoreSharedEventsActivity.class);
                intent.putExtra("userKey", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
            }
        });

        //Posts
        miniProfilePicture = (ImageView) r.findViewById(R.id.mini_profile_picture);
        miniUserName = (TextView) r.findViewById(R.id.mini_name);
        miniHandle = (TextView) r.findViewById(R.id.mini_user_handle);
        body = (TextView) r.findViewById(R.id.user_post_body);
        postsNumLikes = (TextView) r.findViewById(R.id.user_post_like_count);
        postsNumComments = (TextView) r.findViewById(R.id.user_post_comment_count);
        postList = (ListView) r.findViewById(R.id.post_list);
        eventList = (ListView) r.findViewById(R.id.event_list);
        sharedPosts = (ListView) r.findViewById(R.id.shared_post_list);
        sharedEvents = (ListView) r.findViewById(R.id.shared_event_list);
        postsNumShares = (TextView) r.findViewById(R.id.user_post_share_count);
        postArray = new ArrayList<>();
        eventArray = new ArrayList<>();
        sharedEventArray = new ArrayList<>();
        sharedPostArray = new ArrayList<>();


        populateSharedPost();
        populateSharedEvent();
        queryFirebase();
        populatePost();
        populateEvent();
        getEventLikes();
        return r;
    }

    //Handles finished activity when a user has clicked an image from gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // When an Image is picked, the image is called back
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK
                && null != data) {
            // Get the Image from data
            filePath = data.getData();
            //Upload to firebase
            uploadToFirebase();
            //Then set the photo from the chosen gallery
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(getApplicationContext().getContentResolver(), filePath);
                Log.d("pathfileee", bitmap.toString());
                //Picasso.with(getContext()).load(filePath).into(profilePicture);
                profilePicture.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Else user did not pick an image
        } else if (requestCode == CAPTURE_PICTURE && resultCode == RESULT_OK) {
            // Get the Image from data
            filePath = data.getData();
            //Upload to firebase
            uploadToFirebase();
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePicture.setImageBitmap(imageBitmap);
            //Else user did not pick an image
        } else if(requestCode == ABOUT_ME && resultCode == RESULT_OK){
            about.setText(data.getStringExtra("aboutme"));
        } else {
            Toast.makeText(getApplicationContext(), "Cancelled",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void queryFirebase() {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("User");
        users.orderByChild("uid").equalTo(fbuser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            temp = userSnapshot.getValue(User.class);
                            userName.setText(temp.DisplayName);
                            hash_tag.setText(temp.handle);
                            numberFollowers.setText(Integer.toString(temp.NumberFollowers));
                            numberFollowing.setText(Integer.toString(temp.NumberFollowing));
                            numLikes.setText(Integer.toString(temp.LikesReceived));
                            about.setText(temp.AboutMe);

                            //TRY because user might not have profile picture yet
                            try {
                                //Convert the URL to aa Bitmap using function, then set the profile picture
                                if (temp.ProfilePicture != null)
                                    Picasso.with(getContext()).load(temp.ProfilePicture).transform(new CircleTransform()).into(profilePicture);
                                else
                                    Picasso.with(getContext()).load(R.mipmap.ic_launcher).into(profilePicture);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
    }

    public void uploadToFirebase() {
        //create the profile picture name using their uid + .jpg
        String childFile = String.valueOf(fbuser.getUid()) + ".jpg";

        //If the file was chosen from gallery then != null
        if (filePath != null) {
            //Create child using the above string
            StorageReference fileRef = storageRef.child(childFile);
            //Create the upload using built-in UploadTask
            UploadTask uploadTask = fileRef.putFile(filePath);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    //Set the download URL
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //Store URL under the current user
                    FirebaseDatabase.getInstance().getReference().child("User")
                            .child(fbuser.getUid()).child("ProfilePicture").setValue(downloadUrl.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Upload Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Select an image", Toast.LENGTH_SHORT).show();
        }
    }

    //populate posts from firebase
    public void populatePost() {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Post");
        users.orderByChild("ownerID").equalTo(fbuser.getUid()).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            Post post = userSnapshot.getValue(Post.class);
                            //Most recent first
                            if(!post.isAnon){
                            postArray.add(0, post);}
                        }
                        postAdapter = new NotificationFragment.PostAdapter(getContext(), postArray);
                        postList.setAdapter(postAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
    }

    public void populateEvent() {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Event");
        //two events since events views are big
        users.orderByChild("ownerID").equalTo(fbuser.getUid()).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            Event event = userSnapshot.getValue(Event.class);
                            //Most recent first
                            eventArray.add(0, event);
                        }
                        eventAdapter = new TrendingFragment.EventAdapter(getContext(), eventArray);
                        eventList.setAdapter(eventAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
    }

    public void populateSharedPost() {
        DatabaseReference shared = FirebaseDatabase.getInstance().getReference("Shares").child(fbuser.getUid().toString());
        shared.orderByChild("type").equalTo("Post").limitToLast(1);

                shared.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot shareSnap : dataSnapshot.getChildren()){
                            final String key = shareSnap.getKey();

                            Shares share = shareSnap.getValue(Shares.class);
                            final String id = share.id;

                            if(share.type.equals("Post")) {
                                FirebaseDatabase.getInstance().getReference().child("Post")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild(id)) {
                                                    Post.requestPost(id, "auth", new Post.PostReceivedListener() {
                                                        @Override
                                                        public void onPostReceived(Post... posts) {
                                                            Post pst = posts[0];
                                                            sharedPostArray.add(0, pst);
                                                            android.util.Log.e("nat", String.valueOf(sharedEventAdapter.getCount()));
                                                            sharedPostAdapter.notifyDataSetChanged();
                                                        }


                                                    });
                                                } else {
                                                    FirebaseDatabase.getInstance().getReference().child("Shares").child(fbuser.getUid().toString()).child(key)
                                                            .removeValue(); // delete if has been deleted.
                                                }
                                            }


                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }

                        sharedPostAdapter = new SharingAdapters.SharedPostAdapter(getContext(), sharedPostArray, true);
                        sharedPosts.setAdapter(sharedPostAdapter);
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public void populateSharedEvent() { //populates latest event
        DatabaseReference shared = FirebaseDatabase.getInstance().getReference("Shares").child(fbuser.getUid().toString());
        shared.orderByChild("type").equalTo("Event").limitToLast(1);

            shared.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot shareSnap : dataSnapshot.getChildren()) {
                        final String key = shareSnap.getKey();

                        Shares share = shareSnap.getValue(Shares.class);
                        final String id = share.id;

                        if (share.type.equals("Event")) {
                            FirebaseDatabase.getInstance().getReference().child("Event")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) { //check if event has been deleted
                                            if (dataSnapshot.hasChild(id)) {

                                                Event.requestEvent(id, "auth", new Event.EventReceivedListener() {
                                                    @Override
                                                    public void onEventReceived(Event... events) {
                                                        Event ev = events[0];
                                                        sharedEventArray.add(0, ev);
                                                        android.util.Log.e("nat", String.valueOf(sharedEventAdapter.getCount()));
                                                        sharedEventAdapter.notifyDataSetChanged();
                                                    }


                                                });
                                            } else {
                                                FirebaseDatabase.getInstance().getReference().child("Shares").child(fbuser.getUid().toString()).child(key)
                                                        .removeValue(); // delete if has been deleted.
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    sharedEventAdapter = new SharingAdapters.SharedEventAdapter(getContext(), sharedEventArray, true);
                    sharedEvents.setAdapter(sharedEventAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }

    private void popupMenu() {
        PopupMenu popup = new PopupMenu(getActivity(), options);
        popup.getMenuInflater().inflate(R.menu.other_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear all activities above it
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                } else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }



    public void getEventLikes() {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("Event");
        eventRef.orderByChild("ownerID").equalTo(fbuser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int likesCount = 0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Event e = dataSnapshot1.getValue(Event.class);
                    likesCount += e.likes;
                }
                FirebaseDatabase.getInstance().getReference().child("LikesCount").child(fbuser.getUid()).setValue(likesCount);
                getPostLikes();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getPostLikes() {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("Post");
        eventRef.orderByChild("ownerID").equalTo(fbuser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long likesCount = 0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Post e = dataSnapshot1.getValue(Post.class);
                    likesCount += e.likes;
                }
                setLikes(likesCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setLikes(final long likesCount) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("LikesCount").child(fbuser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long totalLikes = (long) dataSnapshot.getValue();
                totalLikes = totalLikes + likesCount;
                FirebaseDatabase.getInstance().getReference().child("LikesCount").child(fbuser.getUid()).setValue(totalLikes);
                numLikes.setText(Long.toString(totalLikes));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
