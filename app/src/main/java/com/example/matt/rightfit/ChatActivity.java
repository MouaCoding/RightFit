package com.example.matt.rightfit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;


import com.example.matt.rightfit.firebase_entry.Messages;
import com.example.matt.rightfit.firebase_entry.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Matt on 3/2/2017.
 */

public class ChatActivity extends MessageListActivity implements View.OnClickListener,
        MessageSource.MessagesCallbacks {

    public static final String USER_EXTRA = "USER";
    public static final String TAG = "ChatActivity";
    public static final String RECEIVER_ID = "RECEIVER_ID";
    public ArrayList<Messages> mMessages;
    public ArrayList<User> mUsers;
    public MessagesAdapter mAdapter;
    public String mRecipient;
    public ListView mListView;
    public Date mLastMessageDate = new Date();
    public String mConvoId;
    public MessageSource.MessagesListener mListener;
    public Messages msg;
    public TextView receiverName;

    private DatabaseReference users;
    public int listener = 0;


    //User (Sender)
    public User sender;
    public FirebaseUser user;
    public ImageButton options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_thread);

        options = (ImageButton) findViewById(R.id.profile_app_bar_options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu();
            }
        });
        receiverName = (TextView) findViewById(R.id.profile_name_chat);
        if (getIntent().getExtras() != null)
            receiverName.setText(getIntent().getExtras().getString("ReceiverName"));
        mListView = (ListView) findViewById(R.id.message_list);
        sender = new User();
        mMessages = new ArrayList<>();
        mUsers = new ArrayList<>();
        mAdapter = new MessagesAdapter(mMessages);
        mListView.setAdapter(mAdapter);
        //setTitle(mRecipient);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish current activity when back button is pressed
                finish();
            }
        });
        Button sendMessage = (Button) findViewById(R.id.send_message);
        sendMessage.setOnClickListener(this);

        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        mRecipient = fbuser.getUid();

        //Establish a link between two ids, this link will not only be unique
        String receiverKey = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            receiverKey = extras.getString(RECEIVER_ID); // get the extra (receiver id) from previous activity
        String[] ids = {mRecipient, receiverKey};
        Arrays.sort(ids);
        mConvoId = ids[0] + ids[1];
        mListener = MessageSource.addMessagesListener(mConvoId, this);
    }

    public void onClick(View v) {
        EditText newMessageView = (EditText) findViewById(R.id.new_message);
        String newMessage = newMessageView.getText().toString();
        newMessageView.setText("");
        msg = new Messages();
        msg.setDate(new Date());
        msg.setMessage(newMessage);
        //set the sender to the sender's id
        msg.setSender(mRecipient);
        msg.setReceiver(getIntent().getExtras().getString(RECEIVER_ID));

        getKeyFromFB();
    }

    @Override
    public void onMessageAdded(Messages message) {
        mMessages.add(message);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageSource.stop(mListener);
    }

    public class MessagesAdapter extends ArrayAdapter<Messages> {
        MessagesAdapter(ArrayList<Messages> messages) {

            super(ChatActivity.this, R.layout.msg_item, R.id.msg, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
            convertView = super.getView(position, convertView, parent);
            Messages message = getItem(position);
            TextView nameView = (TextView) convertView.findViewById(R.id.msg);
            nameView.setText(message.getMessage());
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) nameView.getLayoutParams();
            int sdk = android.os.Build.VERSION.SDK_INT;
            //if the sender matches the id of the sender, then bubble appears right side and green
            if (message.getSender().equals(fbuser.getUid())) {
                nameView.setBackground(getDrawable(R.drawable.bubble_right_green));
                layoutParams.gravity = Gravity.END;
                //else it appears left side and gray
            } else {
                nameView.setBackground(getDrawable(R.drawable.bubble_left_gray));
                layoutParams.gravity = Gravity.LEFT;
            }
            nameView.setLayoutParams(layoutParams);
            return convertView;
        }
    }

    public void getKeyFromFB() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        users = FirebaseDatabase.getInstance().getReference("User");
        //found user and link the message to their sender id
        users.getRef().getRef().orderByChild("Sender ID").equalTo(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            Messages m = userSnapshot.getValue(Messages.class);
                            String[] ids = {mRecipient, m.getReceiver()};
                            Arrays.sort(ids);
                            mConvoId = ids[0] + ids[1];
                            //Log.d(sender.id,"SENDER ID!");
                            //Log.d(key,"key !");
                        }
                        MessageSource.saveMessage(msg, mConvoId);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });

    }

    private void popupMenu() {
        PopupMenu popup = new PopupMenu(ChatActivity.this, options);
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
