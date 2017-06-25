package com.example.matt.rightfit.notifications;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Bradley Wang on 5/24/2017.
 */

public class RHRNInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        FirebaseInstanceId.getInstance().getToken();
        // TODO put token in list of logged in tokens
    }
    //
}
