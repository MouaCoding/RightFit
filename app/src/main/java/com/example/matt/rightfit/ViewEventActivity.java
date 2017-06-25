package com.example.matt.rightfit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.Comments;
import com.example.matt.rightfit.firebase_entry.Event;
import com.example.matt.rightfit.firebase_entry.Likes;
import com.example.matt.rightfit.util.CircleTransform;
import com.firebase.client.ServerValue;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ViewEventActivity extends AppCompatActivity implements OnMapReadyCallback {
    public TextView content, likes, comments, shares, displayName, handle, viewComments;
    private TextView likesCount;
    private TextView commentsCount;
    private TextView sharesCount;
    public ImageView profile, eventImage;
    public ImageButton back, likeButton, commentButton, shareButton;
    public GoogleMap mMap;
    private LatLng createLoc;
    private MapView event_location;
    private CheckBox anon;

    public ArrayList<Comments> commentArray;
    public ListView commentList;
    public CommentsListActivity.CommentsAdapter commentsAdapter;
    String EventID, currUsr, ownerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event/*framed_view_event*/);
        content = (TextView) findViewById(R.id.view_event_content);
        likes = (TextView) findViewById(R.id.user_event_like_count);
        comments = (TextView) findViewById(R.id.user_event_comment_count);
        profile = (ImageView) findViewById(R.id.view_event_user);
        shares = (TextView) findViewById(R.id.user_event_share_count);
        displayName = (TextView) findViewById(R.id.view_user_displayname);
        eventImage = (ImageView) findViewById(R.id.view_user_eventimage);
        eventImage.requestFocus();
        commentList = (ListView) findViewById(R.id.view_event_comment_list);
        handle = (TextView) findViewById(R.id.view_user_handle);
        likesCount = (TextView) findViewById(R.id.user_event_like_count);
        commentsCount = (TextView) findViewById (R.id.user_event_comment_count);
        sharesCount = (TextView) findViewById(R.id.user_event_share_count);
        viewComments = (TextView) findViewById(R.id.view_comments);
        viewComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentList.setVisibility(View.VISIBLE);
                getComments(getIntent().getStringExtra("eventid"), false);
            }
        });
        EventID = getIntent().getStringExtra("eventid");
        currUsr = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ownerID = getIntent().getStringExtra("ownerID");
        commentArray = new ArrayList<>();
        commentsAdapter = new CommentsListActivity.CommentsAdapter(ViewEventActivity.this, commentArray, "Event");
        commentList.setAdapter(commentsAdapter);
        commentList.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        event_location = (MapView) findViewById(R.id.event_location_map_view);
        back = (ImageButton) findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        likeButton = (ImageButton) findViewById(R.id.user_event_like_button);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Likes.hasLiked(2, EventID, currUsr )){
                    likeButton.setColorFilter(ContextCompat.getColor(ViewEventActivity.this,R.color.colorTextDark));
                    Toast.makeText(ViewEventActivity.this, "Unliked", Toast.LENGTH_SHORT).show();
                    Event.Unlike(EventID, currUsr);
                    updateCounts(EventID);
                }
                else{
                    likeButton.setColorFilter(ContextCompat.getColor(ViewEventActivity.this,R.color.crimson));
                    Toast.makeText(ViewEventActivity.this, "Liked", Toast.LENGTH_SHORT).show();
                    Event.Like(EventID, currUsr);
                    updateCounts(EventID);
                }
            }
        });

        commentButton = (ImageButton) findViewById(R.id.user_event_comment_button);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = ViewEventActivity.this;
                Bundle params = new Bundle();
                Intent intent = new Intent(context, CommentsListActivity.class);
                intent.putExtra("postID", EventID.toString());
                intent.putExtra("type", "Event");
                //context.startActivityForResult(intent, RC);
                context.startActivity(intent);
                updateCounts(EventID);

            }
        });
        shareButton = (ImageButton) findViewById(R.id.user_event_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareButton.setColorFilter(ContextCompat.getColor(ViewEventActivity.this,R.color.MainBlue));
                Event.Share(EventID, currUsr);
                updateCounts(EventID);
            }
        });
        displayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewEventActivity.this, ViewUserActivity.class);
                intent.putExtra("otherUserID", getIntent().getExtras().getString("ownerID"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewEventActivity.this, ViewUserActivity.class);
                intent.putExtra("otherUserID", getIntent().getExtras().getString("ownerID"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        //event_location.getMapAsync(this);
        //event_location.onCreate(savedInstanceState);
        String eventid = null;
        if (getIntent().getExtras() != null) {
            eventid = getIntent().getExtras().getString("eventid");
            populate(eventid);
            //populateComments(eventid);
            //TODO:MM - get the post location
            //getPostLocation(postid);
        }
        //createLoc = new LatLng(0,0);


    }

    public void updateCounts(final String eventID){
        Event.requestEvent(eventID, "authToken", new Event.EventReceivedListener() {
            @Override
            public void onEventReceived(Event... events) {
                Event ev = events[0];
                try{
                    likesCount.setText(Integer.toString(ev.likes));
                    commentsCount.setText(Integer.toString(ev.comments));
                    sharesCount.setText(String.valueOf(ev.shares));


                } catch(Exception e){}
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
    }

    private void populate(final String eventid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Event");
        ref.child(eventid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                try {
                    content.setText(event.description);
                } catch (Exception e) {} //to encompass some posts with no description
                likes.setText(Integer.toString(event.likes));
                comments.setText(Integer.toString(event.comments));
                shares.setText(Integer.toString(event.shares));
                displayName.setText(event.DisplayName);
                handle.setText(event.handle);
                try {
                    if (event.userProfilePicture != null)
                        Picasso.with(getBaseContext()).load(event.userProfilePicture).transform(new CircleTransform()).into(profile);
                    else
                        Picasso.with(getBaseContext()).load(R.mipmap.ic_launcher).into(profile);
                } catch (Exception e) {
                }
                try {
                    if (event.ProfilePicture != null) {
                        Picasso.with(getBaseContext()).load(event.ProfilePicture).into(eventImage);
                        //Picasso.with(getBaseContext()).load(event.ProfilePicture).into(eventImage);
                        eventImage.setVisibility(View.VISIBLE);
                    } else
                        Picasso.with(getBaseContext()).load(R.drawable.images).into(eventImage);
                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void createComment(String userID, String postID, String Content, int Order, String responseID, boolean anon) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference rootreference = FirebaseDatabase.getInstance().getReference("Comments").child(postID);
        DatabaseReference reference;
        if (Order == 0) {
            reference = rootRef.child("comment").push();
        } else {
            reference = rootRef.child("comment").child(postID).child(responseID).push();
        }
        String key = reference.getKey();

        DatabaseReference createdComment = FirebaseDatabase.getInstance().getReference("Comments").child(postID).child(key);
        createdComment.setValue(new Comments(userID, key, Content, postID, Order, 0, 0, anon, ServerValue.TIMESTAMP));
        createdComment.child("timestamp_create").setValue(ServerValue.TIMESTAMP);
    }

    public void getPostLocation(final String postid) {
        DatabaseReference postsOnMap = FirebaseDatabase.getInstance().getReference("PostLocations");
        final GeoFire postFire = new GeoFire(postsOnMap);

        postFire.getLocation("location", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                GeoQuery postQuery = postFire.queryAtLocation(new GeoLocation(location.latitude, location.longitude), 10);
                postQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String s, GeoLocation l) {
                        LatLng location = new LatLng(l.latitude, l.longitude);
                        mMap.addMarker(new MarkerOptions()
                                .position(location).draggable(false)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.exclamation_blue)));
                    }

                    @Override
                    public void onKeyExited(String key) {
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                    }

                    @Override
                    public void onGeoQueryReady() {
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }

    public void getComments(String postID, boolean first) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postID);
        ref.orderByChild("timestamp_create");
        if (first) {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                        Comments comment = commentSnapshot.getValue(Comments.class);
                        commentArray.add(comment);
                        commentsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        } else {
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Comments comment = dataSnapshot.getValue(Comments.class);
                    commentsAdapter.add(comment);
                    commentsAdapter.notifyDataSetChanged();
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }

    }

    public void populateComments(String eventid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(eventid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    ; //no comments, so do nothing
                else {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Comments comments = dataSnapshot1.getValue(Comments.class);
                        commentArray.add(comments);
                    }
                    commentsAdapter = new CommentsListActivity.CommentsAdapter(ViewEventActivity.this, commentArray, "Event");
                    commentList.setAdapter(commentsAdapter);
                    //commentsAdapter.notifyDataSetChanged();
                    commentList.setOnTouchListener(new ListView.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            int action = event.getAction();
                            switch (action) {
                                case MotionEvent.ACTION_DOWN:
                                    // Disallow ScrollView to intercept touch events.
                                    v.getParent().requestDisallowInterceptTouchEvent(true);
                                    break;

                                case MotionEvent.ACTION_UP:
                                    // Allow ScrollView to intercept touch events.
                                    v.getParent().requestDisallowInterceptTouchEvent(false);
                                    break;
                            }

                            // Handle ListView touch events.
                            v.onTouchEvent(event);
                            return true;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
