package com.example.matt.rightfit.notifications;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Bradley Wang on 5/24/2017.
 */

public class RHRNMessagingService  extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

//        Toast.makeText(this, message.getNotification().getBody(), Toast.LENGTH_LONG).show();
    }
    //
}
