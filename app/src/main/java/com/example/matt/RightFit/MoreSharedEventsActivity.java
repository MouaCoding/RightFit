package com.example.matt.RightFit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.matt.RightFit.firebase_entry.Event;
import com.example.matt.RightFit.firebase_entry.Shares;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MoreSharedEventsActivity extends AppCompatActivity {
    public ImageButton backButton, options;
    public TextView eventTitle;
    public ListView eventList;
    public ArrayList<Event> eventArrayList;
    public SharingAdapters.SharedEventAdapter eventAdapter;

    public ProgressBar loadMoreEvents;

    public int loadEvents = 0;
    public int scrollCount = 0;

    public boolean isOwner;
    int first = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_shared_events);
        backButton = (ImageButton) findViewById(R.id.back_button);
        eventTitle = (TextView) findViewById(R.id.profile_name_chat);
        eventList = (ListView) findViewById(R.id.user_all_shared_events);
        eventArrayList = new ArrayList<>();
        isOwner = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(getIntent().getStringExtra("userKey"));
        options = (ImageButton) findViewById(R.id.profile_app_bar_options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu();
            }
        });
        //eventAdapter = new TrendingFragment.EventAdapter(MoreSharedEventsActivity.this, eventArrayList);
        //eventList.setAdapter(eventAdapter);
        //loadMoreEvents = (ProgressBar) LayoutInflater.from(this).inflate(R.layout.progress_bar, null);
        //findViewById(R.id.load_more_events);
        View mProgressBarFooter = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.progress_bar, null, false);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        try {
            eventList.addFooterView(mProgressBarFooter);
        } catch (Exception e) {
        }
        eventList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (findViewById(R.id.load_more_posts).isShown())//.getVisibility() == View.VISIBLE)
                {
                    loadEvents = 0;
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                if (loadEvents == 0) {
                    loadEvents = 1;
                    scrollCount++;
                    eventArrayList = new ArrayList<Event>();
                    try {
                        getUserEvents(scrollCount * 25);
                    } catch (Exception e) {
                    }
//                    eventAdapter.notifyDataSetChanged();
                    Log.i("counttt", Integer.toString(scrollCount));
                }


            }
        });


    }

    public void getUserEvents(final int n) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Shares").child(getIntent().getStringExtra("userKey"));
        ref.orderByChild("type").equalTo("Event").limitToLast(n).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    final String key = dataSnapshot.getKey();
                    Shares share = dataSnapshot1.getValue(Shares.class);
                    final String id = share.id;
                    FirebaseDatabase.getInstance().getReference().child("Event")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(id)){
                                        Event.requestEvent(id, "auth", new Event.EventReceivedListener() {
                                            @Override
                                            public void onEventReceived(Event... events) {
                                                eventArrayList.add(0, events[0]);
                                                eventAdapter.notifyDataSetChanged();
                                            }
                                        });

                                    }
                                    else{
                                        FirebaseDatabase.getInstance().getReference().child("Shares").child(getIntent()
                                                .getStringExtra("userKey")).child(key).removeValue(); // delete if has been deleted.
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                }
                try {eventTitle.setText(eventArrayList.get(0).DisplayName + "'s Events");} catch (Exception e) {}
                    eventAdapter = new SharingAdapters.SharedEventAdapter(MoreSharedEventsActivity.this, eventArrayList, isOwner);
                    eventList.setAdapter(eventAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void popupMenu() {
        PopupMenu popup = new PopupMenu(MoreSharedEventsActivity.this, options);
        popup.getMenuInflater().inflate(R.menu.other_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear all activities above it
                    startActivity(intent);
                    finish();
                    return true;
                } else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }
}
