package com.example.matt.rightfit.custom.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.matt.rightfit.App;
import com.example.matt.rightfit.CommentsListActivity;
import com.example.matt.rightfit.R;
import com.example.matt.rightfit.ViewEventActivity;
import com.example.matt.rightfit.firebase_entry.Event;
import com.example.matt.rightfit.firebase_entry.User;
import com.example.matt.rightfit.util.CircleTransform;
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

public class UserEventView extends FrameLayout {
    public App app = (App) getApplicationContext();
    private UserMiniHeaderView eventMakerHeader;
    private ImageView eventMiniImageView;
    private TextView eventTitleView;
    private TextView eventStartTimeView;
    private TextView eventEndTimeView;
    private TextView eventLocationView;
    private TextView likesCount;
    private TextView commentsCount;
    private TextView sharesCount;
    int usrLikes;

    private static final int RC = 1;


    private Spinner eventRSVPStateSpinner;

    private String EventID;
    private String OwnerID;
    private int CommentCount;
    private String currUsr = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();


    private ImageButton likeButton;
    private ImageButton commentButton;
    private ImageButton shareButton;
    private ImageButton options;

    public UserEventView(Context context) {
        super(context);
        createView();
    }

    public UserEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    public void createView() {
        inflate(getContext(), R.layout.user_event_layout, this);

        eventMakerHeader = (UserMiniHeaderView) findViewById(R.id.user_event_mini_head);

        eventMiniImageView = (ImageView) findViewById(R.id.user_event_mini_image);
        eventTitleView = (TextView) findViewById(R.id.user_event_title);
        eventStartTimeView = (TextView) findViewById(R.id.user_event_start_time);
        eventEndTimeView = (TextView) findViewById(R.id.user_event_end_time);
        eventLocationView = (TextView) findViewById(R.id.user_event_location);


        eventRSVPStateSpinner = (Spinner) findViewById(R.id.user_event_rsvp_state_spinner);

        String[] rsvpStates = new String[] {getContext().getString(R.string.event_rsvp_not_going), getContext().getString(R.string.event_rsvp_maybe), getContext().getString(R.string.event_rsvp_going)};
        eventRSVPStateSpinner.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.spinner_entry_layout, rsvpStates));
        // TODO create callback for spinner here for updating RSVP state on server side

        likesCount = (TextView) findViewById(R.id.user_event_like_count);
        commentsCount = (TextView) findViewById (R.id.user_event_comment_count);
        sharesCount = (TextView) findViewById(R.id.user_event_share_count);

        likeButton = (ImageButton) findViewById(R.id.user_event_like_button);
        likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               FirebaseDatabase.getInstance().getReference().child("Likes").child(EventID)
                       .addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {
                               if(dataSnapshot.hasChild(currUsr)){
                                   likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorTextDark));
                                   Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                                   Event.Unlike(EventID, currUsr);
                                   updateCounts(EventID);

                               }
                               else{
                                   likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.crimson));
                                   Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
                                   Event.Like(EventID, currUsr);
                                   updateCounts(EventID);
                               }
                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {

                           }
                       });
            }
        });

        commentButton = (ImageButton) findViewById(R.id.user_event_comment_button);
        commentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
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
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.MainBlue));
                Event.Share(EventID, currUsr);
                updateCounts(EventID);
            }
        });

        eventTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewEventActivity.class);
                intent.putExtra("eventid", EventID);
                intent.putExtra("type", "Event");
                intent.putExtra("ownerID", OwnerID);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });
        eventMiniImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewEventActivity.class);
                intent.putExtra("eventid", EventID);
                intent.putExtra("type", "Event");
                intent.putExtra("ownerID", OwnerID);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });

        options = (ImageButton) findViewById(R.id.mini_profile_more_button);
    }

    public void getEvent(final String eventID) {
        // TODO fetch event information from params and fill fields
        Event.requestEvent(eventID, "AuthToken", new Event.EventReceivedListener() {
            @Override
            public void onEventReceived(Event... events) {
                Event ev = events[0];
                try {
                    setEvent(ev);
                    EventID = eventID;
                    OwnerID = ev.ownerID;
                }catch(Exception e){}

            }
        });
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

    public void setEvent(final Event ev){
                eventMakerHeader.getUser(ev.ownerID);
                eventTitleView.setText(ev.eventName);
                eventStartTimeView.setText(ev.startTime);
                eventEndTimeView.setText(ev.endTime);
                eventLocationView.setText(ev.address);
                likesCount.setText(Integer.toString(ev.likes));
                commentsCount.setText(Integer.toString(ev.comments));
                sharesCount.setText(String.valueOf(ev.shares));

                try {
                    if(ev.ProfilePicture != null)
                        Picasso.with(getContext()).load(ev.ProfilePicture).transform(new CircleTransform()).into(eventMiniImageView);
                    else
                        Picasso.with(getContext()).load(R.drawable.images).transform(new CircleTransform()).into(eventMiniImageView);
                }catch (Exception e){}

        options.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu(getRootView(),ev.ownerID,ev.eventID);
            }
        });
    }


    public void popupMenu(View view, final String ownerID, final String eventID)
    {
        options = (ImageButton) view.findViewById(R.id.mini_profile_more_button);
        final PopupMenu popup = new PopupMenu(view.getContext(), options);
        popup.getMenuInflater().inflate(R.menu.event_options, popup.getMenu());
        if(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(ownerID))
            popup.getMenu().findItem(R.id.delete_event).setVisible(true);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.delete_event) {
                    promptDelete(ownerID, eventID);
                    return true;
                }
                if (i == R.id.report_event) {
                    Toast.makeText(getApplicationContext(),"Reporting Event...",Toast.LENGTH_SHORT).show();
                    reportEvent(ownerID, eventID);
                    return true;
                }
                else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }

    public void promptDelete(final String ownerID, final String eventID)
    {
        android.support.v7.app.AlertDialog.Builder dlgAlert = new android.support.v7.app.AlertDialog.Builder(getContext());
        dlgAlert.setMessage("Are you sure you want to delete this event? This action cannot be undone!");
        dlgAlert.setTitle("Delete Event?");

        dlgAlert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Perform delete
                Toast.makeText(getContext(), "Deleting Event...", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference().child("Event").child(eventID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("EventLocations").child(eventID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("OtherEventLocations").child(eventID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("PartyEventLocations").child(eventID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("SportEventLocations").child(eventID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("EducationEventLocations").child(eventID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("ClubEventEventLocations").child(eventID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Likes").child(eventID).removeValue();


                Toast.makeText(getContext(), "Event Deleted!", Toast.LENGTH_SHORT).show();
                //TODO: update likes received...
            }
        });

        //if user cancels
        dlgAlert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dlgAlert.setCancelable(true);
        dlgAlert.create();
        dlgAlert.show();
    }

    public void reportEvent(final String ownerID, final String eventID)
    {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Event");
        ref.child(eventID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((String) dataSnapshot.child("description").getValue() == null) return;
                else{
                    if(!dataSnapshot.child("numberOfReports").exists())
                        ref.child(eventID).child("numberOfReports").setValue(0);
                    else {
                        long numberOfReports = (long) dataSnapshot.child("numberOfReports").getValue();
                        //parse whitespace
                        String[] content = ((String) dataSnapshot.child("description").getValue()).split("\\s+");
                        if (hasBadWord(content)) {
                            numberOfReports++;
                            ref.child(eventID).child("numberOfReports").setValue(numberOfReports);
                            //TODO: set the amount of reports before a event is deleted
                            if(numberOfReports > 5) {
                                FirebaseDatabase.getInstance().getReference().child("Event").child(eventID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("EventLocations").child(eventID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("OtherEventLocations").child(eventID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("PartyEventLocations").child(eventID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("SportEventLocations").child(eventID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("EducationEventLocations").child(eventID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("ClubEventEventLocations").child(eventID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("Likes").child(eventID).removeValue();
                            }
                        } //Has bad word
                    }//else number of reports exists
                }//else event has content
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean hasBadWord(String[] content)
    {
        for(String c : content) {
            for (String badWord : app.badWords) {
                c = c.toLowerCase();
                if (c.contains(badWord)) {
                    Toast.makeText(getContext(), "Event has been reported.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
        Toast.makeText(getContext(), "There is nothing to report.", Toast.LENGTH_SHORT).show();
        return false;
    }



    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public void getOwnerLikes(String ownerID){
        FirebaseDatabase.getInstance().getReference().child("User").child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User postOwner = dataSnapshot.getValue(User.class);
                usrLikes = postOwner.LikesReceived;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
