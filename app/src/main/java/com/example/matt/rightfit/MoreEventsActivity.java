package com.example.matt.rightfit;

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

import com.example.matt.rightfit.firebase_entry.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MoreEventsActivity extends AppCompatActivity {
    public ImageButton backButton, options;
    public TextView eventTitle;
    public ListView eventList;
    public ArrayList<Event> eventArrayList;
    public TrendingFragment.EventAdapter eventAdapter;

    public ProgressBar loadMoreEvents;

    public int loadEvents = 0;
    public int scrollCount = 0;
    int first = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_events);
        backButton = (ImageButton) findViewById(R.id.back_button);
        eventTitle = (TextView) findViewById(R.id.profile_name_chat);
        eventList = (ListView) findViewById(R.id.user_all_events);
        eventArrayList = new ArrayList<>();
        eventAdapter = new TrendingFragment.EventAdapter(this, eventArrayList);
        eventList.setAdapter(eventAdapter);
        options = (ImageButton) findViewById(R.id.profile_app_bar_options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu();
            }
        });
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
                    eventAdapter.notifyDataSetChanged();
                    Log.i("counttt", Integer.toString(scrollCount));
                }


            }
        });


    }

    public void getUserEvents(final int n) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Event");
        ref.orderByChild("ownerID").equalTo(getIntent().getStringExtra("userKey")).limitToLast(n).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    eventArrayList.add(0, dataSnapshot1.getValue(Event.class));
                }
                try {
                    eventTitle.setText(eventArrayList.get(0).DisplayName + "'s Events");
                    eventAdapter = new TrendingFragment.EventAdapter(MoreEventsActivity.this, eventArrayList);
                    eventList.setAdapter(eventAdapter);
                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void popupMenu() {
        PopupMenu popup = new PopupMenu(MoreEventsActivity.this, options);
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
