package com.example.matt.rightfit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.City;
import com.example.matt.rightfit.firebase_entry.Event;
import com.example.matt.rightfit.firebase_entry.FollowingUser;
import com.example.matt.rightfit.firebase_entry.User;
import com.example.matt.rightfit.util.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Matt on 4/2/2017.
 */

public class TrendingFragment extends Fragment {
    public static App app = (App) getApplicationContext();
    public Button global, city;
    public ListView trendingList, cityList;
    public EventAdapter eventAdapter;
    public ArrayList<Event> eventList;
    public ArrayList<City> cityArray;
    public CityAdapter cityAdapter;
    public ProgressDialog pd;
    public FloatingActionButton filterCity;
    private static final int FILTER_CITY = 0;
    public int filteredCity = 0;
    final static String userNow = FirebaseAuth.getInstance().getCurrentUser().getUid();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View r = (View) inflater.inflate(R.layout.trending_posts, container, false);

        filterCity = (FloatingActionButton) r.findViewById(R.id.filter_city);
        filterCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FilterCityActivity.class);
                startActivityForResult(intent, FILTER_CITY);
            }
        });
        global = (Button) r.findViewById(R.id.global_button);
        global.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventList = new ArrayList<>();
                eventAdapter = new EventAdapter(getContext(), eventList);
                trendingList.setAdapter(eventAdapter);
                filterCity.setVisibility(View.GONE);
                queryAllEvents();
            }
        });
        city = (Button) r.findViewById(R.id.city_button);
        city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //eventList = new ArrayList<>();
                //eventAdapter = new EventAdapter(getContext(), eventList);
                //trendingList.setAdapter(eventAdapter);
                cityArray = new ArrayList<>();
                filterCity.setVisibility(View.VISIBLE);
                //if(filteredCity == 0)
                //    queryCityEvents();
                //else
                queryFilteredCities();
            }
        });


        eventList = new ArrayList<>();
        cityArray = new ArrayList<>();
        trendingList = (ListView) r.findViewById(R.id.global_list_trending);
        cityList = (ListView) r.findViewById(R.id.global_list_trending);

        queryAllEvents();
        return r;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_CITY && resultCode == Activity.RESULT_OK) {

            filteredCity = 1;
            Toast.makeText(getContext(), "City Settings Changed.", Toast.LENGTH_SHORT).show();

            cityArray = new ArrayList<>();
            queryFilteredCities();
            cityAdapter.notifyDataSetChanged();

        }
    }

    public void queryAllEvents() {
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Event").orderByChild("likes").startAt(0).endAt(1000).limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {
                for (DataSnapshot dataSnapshot : dataSnapshot1.getChildren()) {
                    Event ev = dataSnapshot.getValue(Event.class);
                    eventList.add(0, ev);
                }
                eventAdapter = new EventAdapter(getContext(), eventList);
                trendingList.setAdapter(eventAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class EventAdapter extends ArrayAdapter<Event> {
        private ImageButton options;


        private int eventDeleted = 0;

        EventAdapter(Context context, ArrayList<Event> users) {
            super(context, R.layout.user_event_framed_layout, R.id.user_event_title, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            final Event event = getItem(position);
            TextView eventTitle = (TextView) convertView.findViewById(R.id.user_event_title);
            ImageView eventImage = (ImageView) convertView.findViewById(R.id.user_event_mini_image);
            TextView startTime = (TextView) convertView.findViewById(R.id.user_event_start_time);
            TextView endTime = (TextView) convertView.findViewById(R.id.user_event_end_time);
            TextView eventLoc = (TextView) convertView.findViewById(R.id.user_event_location);

            TextView displayNameView = (TextView) convertView.findViewById(R.id.mini_name);
            ImageView profilePicture = (ImageView) convertView.findViewById(R.id.mini_profile_picture);
            TextView userHandleView = (TextView) convertView.findViewById(R.id.mini_user_handle);
            ImageButton followButton = (ImageButton) convertView.findViewById(R.id.mini_profile_add_button);
            if (event.ownerID != FirebaseAuth.getInstance().getCurrentUser().getUid())
                followButton(followButton,FirebaseAuth.getInstance().getCurrentUser().getUid(), event.ownerID);


            eventTitle.setText(event.eventName);
            startTime.setText(event.startTime);
            endTime.setText(event.endTime);
            eventLoc.setText(event.address);
            TextView numLikes = (TextView) convertView.findViewById(R.id.user_event_like_count);
            TextView numComments = (TextView) convertView.findViewById(R.id.user_event_comment_count);
            TextView sharesCount = (TextView) convertView.findViewById(R.id.user_event_share_count);

            numLikes.setText(Integer.toString(event.likes));
            numComments.setText(Integer.toString(event.comments));
            sharesCount.setText(Integer.toString(event.shares));

            displayNameView.setText(event.DisplayName);
            userHandleView.setText(event.handle);

            setButtons(convertView, event.eventID, event.ownerID);
            if (eventDeleted == 0)
                setExtraValues(event.eventID, event.ownerID);

            try {
                if (event.userProfilePicture != null)
                    Picasso.with(getContext()).load(event.userProfilePicture).transform(new CircleTransform()).into(profilePicture);
                else
                    Picasso.with(getContext()).load(R.mipmap.ic_launcher).transform(new CircleTransform()).into(profilePicture);
            } catch (Exception e) {}
            try {
                if (event.ProfilePicture != null)
                    Picasso.with(getContext()).load(event.ProfilePicture).into(eventImage);
                else
                    Picasso.with(getContext()).load(R.drawable.images).into(eventImage);
            } catch (Exception e) {}

            //On clicks to navigate to view user or event
            displayNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ViewUserActivity.class);
                    intent.putExtra("otherUserID", event.ownerID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ViewUserActivity.class);
                    intent.putExtra("otherUserID", event.ownerID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            eventTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ViewEventActivity.class);
                    intent.putExtra("eventid", event.eventID);
                    intent.putExtra("type", "Event");
                    intent.putExtra("ownerID", event.ownerID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            eventImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ViewEventActivity.class);
                    intent.putExtra("eventid", event.eventID);
                    intent.putExtra("type", "Event");
                    intent.putExtra("ownerID", event.ownerID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });

            return convertView;
        }

        private void followButton(ImageButton followButton, final String curUserID, final String otherUserID) {
            followButton.setOnClickListener(new View.OnClickListener() {
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


        public void setButtons(final View view, final String EventID, final String currUsr) {
            final ImageButton likeButton = (ImageButton) view.findViewById(R.id.user_event_like_button);
            likeButton.setOnClickListener(new View.OnClickListener() {
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
                                        updateCounts(EventID, view);

                                    }
                                    else{
                                        likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.crimson));
                                        Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
                                        Event.Like(EventID, currUsr);
                                        updateCounts(EventID, view);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            });


            final ImageButton commentButton = (ImageButton) view.findViewById(R.id.user_event_comment_button);
            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    Bundle params = new Bundle();
                    Intent intent = new Intent(context, CommentsListActivity.class);
                    intent.putExtra("postID", EventID.toString());
                    intent.putExtra("type", "Event");
                    context.startActivity(intent);
                    updateCounts(EventID,view);
                }
            });

            final ImageButton shareButton = (ImageButton) view.findViewById(R.id.user_event_share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.MainBlue));
                    Event.Share(EventID, userNow);
                    updateCounts(EventID,view);
                }
            });
            options = (ImageButton) view.findViewById(R.id.mini_profile_more_button);
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu(view, currUsr, EventID);
                }
            });

        }

        public void updateCounts(final String eventID, View view){
            final TextView numLikes = (TextView) view.findViewById(R.id.user_event_like_count);
            final TextView numComments = (TextView) view.findViewById(R.id.user_event_comment_count);
            final TextView sharesCount = (TextView) view.findViewById(R.id.user_event_share_count);
            Event.requestEvent(eventID, "authToken", new Event.EventReceivedListener() {
                @Override
                public void onEventReceived(Event... events) {
                    Event ev = events[0];
                    try{
                        numLikes.setText(Integer.toString(ev.likes));
                        numComments.setText(Integer.toString(ev.comments));
                        sharesCount.setText(String.valueOf(ev.shares));


                    } catch(Exception e){}
                }
            });
        }

        public void setExtraValues(final String eventID, final String ownerID) {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("User").child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User owner = dataSnapshot.getValue(User.class);
                    ref.child("Event").child(eventID).child("DisplayName").setValue(owner.DisplayName);
                    ref.child("Event").child(eventID).child("handle").setValue(owner.handle);
                    try {
                        ref.child("Event").child(eventID).child("userProfilePicture").setValue(owner.ProfilePicture);
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }


        public void popupMenu(View view, final String ownerID, final String eventID) {
            options = (ImageButton) view.findViewById(R.id.mini_profile_more_button);
            final PopupMenu popup = new PopupMenu(view.getContext(), options);
            popup.getMenuInflater().inflate(R.menu.event_options, popup.getMenu());
            if (String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(ownerID))
                popup.getMenu().findItem(R.id.delete_event).setVisible(true);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int i = item.getItemId();
                    if (i == R.id.delete_event) {
                        promptDelete(ownerID, eventID);
                        return true;
                    }
                    if (i == R.id.report_event) {
                        Toast.makeText(getApplicationContext(), "Reporting Event...", Toast.LENGTH_SHORT).show();
                        reportEvent(ownerID, eventID);
                        return true;
                    } else {
                        return onMenuItemClick(item);
                    }
                }
            });
            popup.show();
        }

        public void promptDelete(final String ownerID, final String eventID) {
            android.support.v7.app.AlertDialog.Builder dlgAlert = new android.support.v7.app.AlertDialog.Builder(getContext());
            dlgAlert.setMessage("Are you sure you want to delete this event? This action cannot be undone!");
            dlgAlert.setTitle("Delete Event?");

            dlgAlert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    eventDeleted = 1;
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

        public void reportEvent(final String ownerID, final String eventID) {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Event");
            ref.child(eventID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ((String) dataSnapshot.child("description").getValue() == null) return;
                    else {
                        if (!dataSnapshot.child("numberOfReports").exists())
                            ref.child(eventID).child("numberOfReports").setValue(0);
                        else {
                            long numberOfReports = (long) dataSnapshot.child("numberOfReports").getValue();
                            //parse whitespace
                            String[] content = ((String) dataSnapshot.child("description").getValue()).split("\\s+");
                            if (hasBadWord(content)) {
                                numberOfReports++;
                                ref.child(eventID).child("numberOfReports").setValue(numberOfReports);
                                //TODO: set the amount of reports before a event is deleted
                                if (numberOfReports > 5) {
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

        public boolean hasBadWord(String[] content) {
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


    }


    public class CityAdapter extends ArrayAdapter<City> {
        CityAdapter(Context context, ArrayList<City> cities) {
            super(context, R.layout.city_layout, R.id.city_name, cities);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            final City city = getItem(position);
            TextView cityName = (TextView) convertView.findViewById(R.id.city_name);
            ImageView cityImage = (ImageView) convertView.findViewById(R.id.city_image);
            TextView cityLocation = (TextView) convertView.findViewById(R.id.city_location);

            cityName.setText(city.CityName);
            cityLocation.setText(city.CityName + ", " + city.State + ", " + city.Country);

            cityName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), CityEventsActivity.class);
                    intent.putExtra("CityName", city.CityName);
                    startActivity(intent);
                }
            });
            cityImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), CityEventsActivity.class);
                    intent.putExtra("CityName", city.CityName);
                    startActivity(intent);
                }
            });
            try {
                if (city.Picture != null)
                    Picasso.with(getContext()).load(city.Picture).into(cityImage);
                else
                    Picasso.with(getContext()).load(R.drawable.cityscape).into(cityImage);
            } catch (Exception e) {
            }
            return convertView;
        }
    }

    public void queryCityEvents() {
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("City").orderByChild("CityName").startAt("A").endAt("Z").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {
                for (DataSnapshot dataSnapshot : dataSnapshot1.getChildren()) {
                    City cty = dataSnapshot.getValue(City.class);
                    cityArray.add(cty);
                }
                cityAdapter = new CityAdapter(getContext(), cityArray);
                cityList.setAdapter(cityAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void queryFilteredCities() {
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference().child("CityFilters");
        RootRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild("CityName").startAt("A").endAt("Z").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {
                for (DataSnapshot dataSnapshot : dataSnapshot1.getChildren()) {
                    City cty = dataSnapshot.getValue(City.class);
                    cityArray.add(cty);
                }
                cityAdapter = new CityAdapter(getContext(), cityArray);
                cityList.setAdapter(cityAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void showProgressDialog() {
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.show();
    }
}
