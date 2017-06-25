package com.example.matt.rightfit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.matt.rightfit.firebase_entry.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FollowerListActivity extends AppCompatActivity {
    public ImageButton backButton, options;
    public TextView userTitle;
    public ListView userList;
    public ArrayList<User> userArrayList;
    public MessageListActivity.UserAdapter userAdapter;
    public EditText search;
    public TextWatcher searchFriendsFilter;

    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_list);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        backButton = (ImageButton) findViewById(R.id.back_button);

        userTitle = (TextView) findViewById(R.id.profile_name_chat);
        userList = (ListView) findViewById(R.id.follower_list);
        options = (ImageButton) findViewById(R.id.profile_app_bar_options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu();
            }
        });
        userArrayList = new ArrayList<>();
        userAdapter = new MessageListActivity.UserAdapter(this, userArrayList);
        userList.setAdapter(userAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchFriendsFilter = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {try{userAdapter.getFilter().filter(s);}catch (Exception e){}}
            @Override
            public void afterTextChanged(Editable s) {}
        };
        search = (EditText) findViewById(R.id.search_followers);
        search.addTextChangedListener(searchFriendsFilter);
        if(getIntent().getStringExtra("otherUserID") == null)
            key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        else
            key = getIntent().getStringExtra("otherUserID");

        getUsersFollowed();
    }

    public void getUsersFollowed()
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User");
        userRef.child(key).child("Followers")
                .orderByChild("filler").getRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(!dataSnapshot.exists())
                    return;
                else {
                    getUsers(dataSnapshot.getKey().toString());
                }
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



    public void getUsers(final String follower)
    {
        if(follower == null)
            return;

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User");
        userRef.child(follower).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;
                else {
                    final User usr = dataSnapshot.getValue(User.class);
                    userArrayList.add(usr);

                    try{
                        userAdapter = new MessageListActivity.UserAdapter(FollowerListActivity.this, userArrayList);
                        userList.setAdapter(userAdapter);
                        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(FollowerListActivity.this, ViewUserActivity.class);
                                intent.putExtra("otherUserID", userArrayList.get(position).uid);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                        FirebaseDatabase.getInstance().getReference().child("User").child(key).child("NumberFollowers").setValue(userArrayList.size());
                    }catch(Exception e){}
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void popupMenu() {
        PopupMenu popup = new PopupMenu(FollowerListActivity.this, options);
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
