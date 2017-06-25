package com.example.matt.rightfit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.matt.rightfit.firebase_entry.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CityEventsActivity extends AppCompatActivity {

    public TextView cityName, displayName;
    public ListView eventList;
    public TrendingFragment.EventAdapter eventAdapter;
    public ArrayList<Event> eventArray;
    ImageButton backButton;
    ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_events);

        cityName = (TextView) findViewById(R.id.trending_events_city);
        displayName = (TextView) findViewById(R.id.mini_name);
        ImageView profilePicture = (ImageView) findViewById(R.id.mini_profile_picture);
        backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish the current activity
                finish();
            }
        });

        String city = getIntent().getExtras().getString("CityName");
        cityName.setText("Trending Events in " + city);
        eventArray = new ArrayList<>();

        eventList = (ListView) findViewById(R.id.city_events_list);

        queryCityEvents(city);
        sortByLikes(city);

    }

    public void queryCityEvents(final String city_name) {
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Event").orderByChild("City").equalTo(city_name).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {
                for (DataSnapshot dataSnapshot : dataSnapshot1.getChildren()) {
                    Event ev = dataSnapshot.getValue(Event.class);

                    FirebaseDatabase.getInstance().getReference().child("CityEvents").child(city_name)
                            .child(ev.eventID).setValue(ev);
                    //eventArray.add(ev);
                }
                //FirebaseDatabase.getInstance().getReference().child("City").child(city_name).child("NumFavorites").setValue(Integer.toString(favoriteCount));
                /*eventAdapter = new TrendingFragment.EventAdapter(getBaseContext(),eventArray);
                eventList.setAdapter(eventAdapter);
                eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(CityEventsActivity.this,ViewEventActivity.class);
                        intent.putExtra("otherUserID",eventArray.get(position).ownerID);
                        startActivity(intent);
                    }
                });*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void sortByLikes(String city_name) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("CityEvents");
        ref.child(city_name).orderByChild("likes").startAt(0).endAt(1234567890).limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            Event ev = dataSnapshot1.getValue(Event.class);
                            eventArray.add(0, ev);
                        }
                        eventAdapter = new TrendingFragment.EventAdapter(CityEventsActivity.this, eventArray);
                        eventList.setAdapter(eventAdapter);
                        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(CityEventsActivity.this, ViewEventActivity.class);
                                intent.putExtra("otherUserID", eventArray.get(position).ownerID);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

}
