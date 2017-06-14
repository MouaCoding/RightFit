package com.example.matt.RightFit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.matt.RightFit.firebase_entry.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MorePostsActivity extends AppCompatActivity {
    public ImageButton backButton, options;
    public TextView postTitle;
    public ListView postList;
    public ArrayList<Post> postArrayList;
    public NotificationFragment.PostAdapter postAdapter;
    public ProgressBar loadMorePosts;
    public int loadPosts = 0;
    public int scrollCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_posts);
        backButton = (ImageButton) findViewById(R.id.back_button);
        postTitle = (TextView) findViewById(R.id.profile_name_chat);
        postList = (ListView) findViewById(R.id.user_all_posts);
        postArrayList = new ArrayList<>();
        postAdapter = new NotificationFragment.PostAdapter(this, postArrayList);
        postList.setAdapter(postAdapter);
        options = (ImageButton) findViewById(R.id.profile_app_bar_options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu();
            }
        });
        //loadMorePosts = (ProgressBar) LayoutInflater.from(this).inflate(R.layout.progress_bar, null);
        //findViewById(R.id.load_more_posts);
        View mProgressBarFooter = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.progress_bar, null, false);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        postList.addFooterView(mProgressBarFooter);
        postList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (findViewById(R.id.load_more_posts).isShown())
                    loadPosts = 0;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (loadPosts == 0) {
                    loadPosts = 1;
                    scrollCount++;
                    postArrayList = new ArrayList<Post>();
                    try {
                        getUserPosts(scrollCount * 25);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    postAdapter.notifyDataSetChanged();
                }
            }
        });


    }

    public void getUserPosts(final int n) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Post");
        ref.orderByChild("ownerID").equalTo(getIntent().getStringExtra("userKey")).limitToLast(n).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Post pst = dataSnapshot1.getValue(Post.class);
                    if(!pst.isAnon){
                        postArrayList.add(pst);}
                }
                try {
                    postTitle.setText(postArrayList.get(0).DisplayName + "'s Posts");
                    postAdapter = new NotificationFragment.PostAdapter(MorePostsActivity.this, postArrayList);
                    postList.setAdapter(postAdapter);
                } catch (Exception e) {}

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void popupMenu() {
        PopupMenu popup = new PopupMenu(MorePostsActivity.this, options);
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
