package com.example.matt.RightFit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.matt.RightFit.firebase_entry.Event;
import com.example.matt.RightFit.firebase_entry.Post;
import com.example.matt.RightFit.firebase_entry.Shares;
import com.example.matt.RightFit.firebase_entry.User;
import com.example.matt.RightFit.util.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Matt on 4/2/2017.
 * Class to view other user profiles
 */

public class ViewUserActivity extends AppCompatActivity {
    public TextView userName,
            hash_tag,
            numberFollowers,
            numFollowing,
            numLikes,
            about,
            morePosts,
            moreEvents,
            moreSharedPosts,
            moreSharedEvents;
    public ImageView profilePicture;
    public ImageButton backButton;

    //Posts
    public ImageView miniProfilePicture;
    public TextView miniUserName,
            miniHandle,
            body,
            postsNumLikes,
            postsNumComments,
            postNumShares,
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


    public FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);

        userName = (TextView) findViewById(R.id.profile_name_main);
        hash_tag = (TextView) findViewById(R.id.profile_userhandle);
        numberFollowers = (TextView) findViewById(R.id.profile_followers_value);
        numFollowing = (TextView) findViewById(R.id.profile_number_following);
        numLikes = (TextView) findViewById(R.id.profile_karma_value);
        about = (TextView) findViewById(R.id.profile_about_text);
        postList = (ListView) findViewById(R.id.post_list);
        eventList = (ListView) findViewById(R.id.event_list);
        sharedPosts = (ListView) findViewById(R.id.shared_post_list);
        sharedEvents = (ListView) findViewById(R.id.shared_event_list);
        profilePicture = (ImageView) findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Enlarge the profile picture
            }
        });
        backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Posts
        miniProfilePicture = (ImageView) findViewById(R.id.mini_profile_picture);
        miniUserName = (TextView) findViewById(R.id.mini_name);
        miniHandle = (TextView) findViewById(R.id.mini_user_handle);
        body = (TextView) findViewById(R.id.user_post_body);
        postsNumLikes = (TextView) findViewById(R.id.user_post_like_count);
        postsNumComments = (TextView) findViewById(R.id.user_post_comment_count);
        postNumShares = (TextView) findViewById(R.id.user_post_share_count);
        profileFollowing = (TextView) findViewById(R.id.profile_following);
        profileFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FollowingListActivity.class);
                intent.putExtra("otherUserID", getIntent().getStringExtra("otherUserID"));
                startActivity(intent);
            }
        });
        profileFollowers = (TextView) findViewById(R.id.profile_followers_label);
        profileFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FollowerListActivity.class);
                intent.putExtra("otherUserID", getIntent().getStringExtra("otherUserID"));
                startActivity(intent);
            }
        });

        morePosts = (TextView) findViewById(R.id.more_posts);
        morePosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MorePostsActivity.class);
                intent.putExtra("userKey", getIntent().getStringExtra("otherUserID"));
                startActivity(intent);
            }
        });

        moreEvents = (TextView) findViewById(R.id.more_events);
        moreEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoreEventsActivity.class);
                intent.putExtra("userKey", getIntent().getStringExtra("otherUserID"));
                startActivity(intent);
            }
        });
        moreSharedPosts = (TextView) findViewById(R.id.more_shared_posts);
        moreSharedPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoreSharedPostsActivity.class);
                intent.putExtra("userKey", getIntent().getStringExtra("otherUserID"));
                startActivity(intent);
            }
        });

        moreSharedEvents = (TextView) findViewById(R.id.more_shared_events);
        moreSharedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoreSharedEventsActivity.class);
                intent.putExtra("userKey", getIntent().getStringExtra("otherUserID"));
                startActivity(intent);
            }
        });

        postArray = new ArrayList<>();
        eventArray = new ArrayList<>();
        sharedPostArray = new ArrayList<>();
        sharedEventArray = new ArrayList<>();

        Intent intent = getIntent();
        String otherUserID = intent.getStringExtra("otherUserID");
        if (otherUserID != null)
            queryFirebase(otherUserID);

        populateSharedPost(otherUserID);
        populateSharedEvent(otherUserID);
        populatePost(otherUserID);
        populateEvent(otherUserID);
        getEventLikes();
    }

    public void queryFirebase(String userUID) {
        FirebaseDatabase.getInstance().getReference("User").child(userUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User temp = dataSnapshot.getValue(User.class);
                        userName.setText(temp.DisplayName);
                        hash_tag.setText(temp.handle);
                        numberFollowers.setText(Integer.toString(temp.NumberFollowers));
                        numFollowing.setText(Integer.toString(temp.NumberFollowing));
                        numLikes.setText(Integer.toString(temp.LikesReceived));
                        about.setText(temp.AboutMe);

                        //TRY because user might not have profile picture yet
                        try {
                            //Convert the URL to aa Bitmap using function, then set the profile picture
                            if (temp.ProfilePicture != null)
                                Picasso.with(getBaseContext()).load(temp.ProfilePicture).transform(new CircleTransform()).into(profilePicture);
                            else
                                Picasso.with(getBaseContext()).load(R.mipmap.ic_launcher).transform(new CircleTransform()).into(profilePicture);
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
    }


    //populate posts from firebase
    public void populatePost(String otherID) {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Post");
        users.orderByChild("ownerID").equalTo(otherID).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        else {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                Post post = userSnapshot.getValue(Post.class);
                                //Most recent first
                                if(!post.isAnon)
                                    postArray.add(0, post);
                            }
                            if (postArray.size() != 0) {
                                postAdapter = new NotificationFragment.PostAdapter(ViewUserActivity.this, postArray);
                                try {
                                    postList.setAdapter(postAdapter);
                                } catch (Exception e) {
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
    }

    public void populateEvent(String otherID) {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Event");
        //two events since events views are big
        users.orderByChild("ownerID").equalTo(otherID).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        else {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                Event event = userSnapshot.getValue(Event.class);
                                //Most recent first
                                eventArray.add(0, event);
                            }
                            if (eventArray.size() != 0) {
                                eventAdapter = new TrendingFragment.EventAdapter(ViewUserActivity.this, eventArray);
                                try {
                                    eventList.setAdapter(eventAdapter);
                                } catch (Exception e) {
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
    }



    public void populateSharedPost(final String otherID) {
        DatabaseReference shared = FirebaseDatabase.getInstance().getReference("Shares").child(otherID);
        shared.orderByChild("type").equalTo("Post").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot shareSnap : dataSnapshot.getChildren()){
                            final String key = shareSnap.getKey();
                            final Shares share = shareSnap.getValue(Shares.class);
                            final String id = share.id;
                            FirebaseDatabase.getInstance().getReference().child("Post")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(id)){
                                                Post.requestPost(share.id, "auth", new Post.PostReceivedListener() {
                                                    @Override
                                                    public void onPostReceived(Post... posts) {
                                                        Post pst = posts[0];
                                                        sharedPostArray.add(0, pst);
                                                        android.util.Log.e("nat", String.valueOf(sharedEventAdapter.getCount()));
                                                        sharedPostAdapter.notifyDataSetChanged();
                                                    }


                                                });
                                            }
                                            else{
                                                FirebaseDatabase.getInstance().getReference().child("Shares").child(otherID)
                                                        .child(key).removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            android.util.Log.e("nat", share.id );


                        }

                        sharedPostAdapter = new SharingAdapters.SharedPostAdapter(ViewUserActivity.this, sharedPostArray, false);
                        sharedPosts.setAdapter(sharedPostAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public void populateSharedEvent(final String otherID) {
        DatabaseReference shared = FirebaseDatabase.getInstance().getReference("Shares").child(otherID);
        shared.orderByChild("type").equalTo("Event").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot shareSnap : dataSnapshot.getChildren()){
                            final String key = dataSnapshot.getKey();
                            Shares share = shareSnap.getValue(Shares.class);
                            final String id = share.id;
                            android.util.Log.e("nat", share.id );
                            FirebaseDatabase.getInstance().getReference().child("Event")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(id)){
                                        Event.requestEvent(id, "auth", new Event.EventReceivedListener() {
                                            @Override
                                            public void onEventReceived(Event... events) {
                                                Event ev = events[0];
                                                sharedEventArray.add(0, ev);
                                                android.util.Log.e("nat", String.valueOf(sharedEventAdapter.getCount()));
                                                sharedEventAdapter.notifyDataSetChanged();
                                            }


                                        });
                                    }
                                    else{
                                        FirebaseDatabase.getInstance().getReference().child("Shares").child(otherID)
                                                .child(key).removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }

                        sharedEventAdapter = new SharingAdapters.SharedEventAdapter(ViewUserActivity.this, sharedEventArray, false);
                        sharedEvents.setAdapter(sharedEventAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



    public void getEventLikes() {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("Event");
        eventRef.orderByChild("ownerID").equalTo(getIntent().getStringExtra("otherUserID")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int likesCount = 0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Event e = dataSnapshot1.getValue(Event.class);
                    likesCount += e.likes;
                }
                FirebaseDatabase.getInstance().getReference().child("LikesCount").child(getIntent().getStringExtra("otherUserID")).setValue(likesCount);
                getPostLikes();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getPostLikes() {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("Post");
        eventRef.orderByChild("ownerID").equalTo(getIntent().getStringExtra("otherUserID")).addValueEventListener(new ValueEventListener() {
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("LikesCount").child(getIntent().getStringExtra("otherUserID"));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long totalLikes = (long) dataSnapshot.getValue();
                totalLikes = totalLikes + likesCount;
                FirebaseDatabase.getInstance().getReference().child("LikesCount").child(getIntent().getStringExtra("otherUserID")).setValue(totalLikes);
                numLikes.setText(Long.toString(totalLikes));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

}
