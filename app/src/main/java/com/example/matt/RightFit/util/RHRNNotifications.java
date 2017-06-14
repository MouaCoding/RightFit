package com.example.matt.RightFit.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by Bradley Wang on 5/30/2017.
 */

public class RHRNNotifications {
    public static void subscribeToMessages() {
        FirebaseMessaging.getInstance().subscribeToTopic("Messages_" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        // TODO perhaps try switching to using Device Groups for better performance.
    }

    public static void unsubscribeFromMessages() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("Messages_" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        // TODO perhaps try switching to using Device Groups for better performance.
    }

    public static void subscribeToFollows() {
        FirebaseDatabase.getInstance().getReference("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot follow : dataSnapshot.getChildren()) {
                    String otherUser = follow.getKey();
                    FirebaseMessaging.getInstance().subscribeToTopic("Followers_"+otherUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // do nothing
            }
        });
    }

    public static void unsubscribeFromFollows() {
        FirebaseDatabase.getInstance().getReference("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot follow : dataSnapshot.getChildren()) {
                    String otherUser = follow.getKey();
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("Followers_"+otherUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // do nothing
            }
        });
    }
}
