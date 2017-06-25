package com.example.matt.rightfit;

import android.app.Application;

import com.firebase.client.Firebase;
import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Matt on 3/2/2017.
 */

public class App extends Application {
    private static final String TAG = "App";
    private static final String TWITTER_KEY = "8Tyr4BWoGSNK1ezT2ifu21tkx";
    private static final String TWITTER_SECRET = "	8NxGsJeGtQEdKWrQJ5F5KCyd99Ciau1i79seMJcCyJE0jQ5veL";

    public String[] badWords;

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
        Firebase.setAndroidContext(this);

        badWords = getResources().getStringArray(R.array.badwords);
    }

}