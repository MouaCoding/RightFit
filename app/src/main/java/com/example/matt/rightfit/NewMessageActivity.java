package com.example.matt.rightfit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
 * Created by Matt on 3/8/2017.
 */

public class NewMessageActivity extends MessageListActivity {
    public Button send;
    public ImageButton backButton;
    public EditText sendTo,
            messageContent;
    public Messages msg;
    public String name, receiverID;
    public App mApp;
    public ArrayList<User> userArray;
    AutoCompleteTextView autoCompleteTextView;
    UserAdapter userAdapter;
    String mConvoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_message);

        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        userArray = new ArrayList<>();

        populateAuto();
        mApp = (App) getApplicationContext();
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when sent, this will find the handle and send message to tha user.
                sendMessage();
            }
        });

        backButton = (ImageButton) findViewById(R.id.back_button1);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sendTo = (EditText) findViewById(R.id.send_to_handle);
        messageContent = (EditText) findViewById(R.id.new_message_content);


    }

    public void populateAuto() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    User usr = dataSnapshot1.getValue(User.class);
                    userArray.add(usr);
                }
                userAdapter = new UserAdapter(getBaseContext(), userArray);
                autoCompleteTextView.setAdapter(userAdapter);
                autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        sendTo.setText(userAdapter.getItem(position).handle);
                        autoCompleteTextView.setText(userAdapter.getItem(position).DisplayName);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage() {
        //get the handle, get the message content
        final String receiverHandle = sendTo.getText().toString().trim();
        String contentOfMessage = messageContent.getText().toString();
        msg = new Messages();
        msg.setDate(new Date());
        msg.setMessage(contentOfMessage);

        //set the sender to the sender's id
        msg.setSender(FirebaseAuth.getInstance().getCurrentUser().getUid());

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userKey = user.getUid();
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("User");
        //query based on the receiver's handle
        rootRef.getRef().orderByChild("handle").equalTo(receiverHandle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    //@Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(getBaseContext(), "User Handle " + receiverHandle + " does not exist!", Toast.LENGTH_SHORT).show();
                        } else {
                            //Iterates through Firebase database
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                //found the receiver by their handle, now set it
                                User receiver = userSnapshot.getValue(User.class);
                                name = receiver.DisplayName;
                                receiverID = receiver.uid;
                                //get the conversation id through here!
                                msg.setReceiver(receiver.uid);
                                mConvoId = userKey + receiver.uid;
                                String[] ids = {user.getUid(), receiver.uid};
                                Arrays.sort(ids);
                                mConvoId = ids[0] + ids[1];
                                Intent i = new Intent();
                                i.putExtra("name", receiver.DisplayName);
                                i.putExtra("handle", receiver.handle);
                                i.putExtra("key", receiver.uid);
                                try {
                                    i.putExtra("profile", receiver.ProfilePicture);
                                } catch (Exception e) {
                                }
                                setResult(RESULT_OK, i);
                            }
                            if (receiverID != null) {
                                rootRef.child(userKey).child("UsersMessaged").child(receiverID).setValue(true);
                                rootRef.child(receiverID).child("UsersMessaged").child(userKey).setValue(true);
                            }
                            else
                                Toast.makeText(NewMessageActivity.this, "No User with that handle found!", Toast.LENGTH_SHORT).show();
                            MessageSource.saveMessage(msg, mConvoId);
                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            intent.putExtra(ChatActivity.RECEIVER_ID, msg.getReceiver());
                            intent.putExtra("ReceiverName", name);
                            intent.putExtra("MessageContent", msg.getMessage());
                            //If success, change to the activity
                            //finish the new message layout
                            finish();
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to retrieve the users.
                    }
                });

    }


}
