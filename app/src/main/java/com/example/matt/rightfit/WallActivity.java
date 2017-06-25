package com.example.matt.rightfit;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.matt.rightfit.firebase_entry.Event;
import com.example.matt.rightfit.firebase_entry.Post;

import java.util.ArrayList;

/**
 * Created by Bradley Wang on 4/6/2017.
 */

public class WallActivity extends FragmentActivity {
    private static int PAGE_SIZE = 12;

    String targetUserID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        targetUserID = getIntent().getStringExtra("userID");
        // get listview and set adapter
    }

    private static class WallAdapter extends BaseAdapter {
        ArrayList<Object[]> arr;

        public void refresh() {
//            FirebaseDatabase.getInstance().getReference().child("Post").order;
//            Post p = null; // TODO get most recent post
//            Event e = null; // TODO get most recent event

            arr.clear();
            fetch(PAGE_SIZE);
//            if (p.createTimestamp > e.createTimestamp) {
//                arr.add(new Object[] {e.createTimestamp, e});
//            } else {
//                arr.add(new Object[] {p.createTimestamp, p});
//            }
            //
        }

        public void fetch(int count) {
            Post pp, ps; // most recent post posted, most recent post shared
            Event ep, es; // most recent event posted, most recent event shared;

            if (!arr.isEmpty()) {
                //
            }

            //
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            if (i < 0 || i >= arr.size())
                return null;
            return arr.get(i)[1];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            // TODO get what should go at position i
            // TODO reuse View view if possible
            return null;
        }
    }
}
