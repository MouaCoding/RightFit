package com.example.matt.RightFit.backend;

import com.example.matt.RightFit.firebase_entry.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by Matt on 5/10/2017.
 */

public class Posts {

    public static void requestPost(String postID, String authToken, final Posts.PostsReceivedListener listener) {
        if (listener == null) return;

        HashMap<String, Object> request = new HashMap<String, Object>();
        request.put("type", "GetSinglePost");
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("postID", postID/*"-KjE16gpdn5sZQudEZe0"*/);
        request.put("params", params);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AppEngineRequests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push();
        ref.setValue(request);
        FirebaseDatabase.getInstance().getReference("AppEngineResults").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ref.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) return;
                        if (!"ok".equals(dataSnapshot.child("status").getValue())) {
                            // do not ok status handling
                            dataSnapshot.getRef().setValue(null);
                            return;
                        }
                        DataSnapshot res = dataSnapshot.child("result");
                        Post pst = new Post((String) res.child("ownerid").getValue(), (String) res.child("postid").getValue(),
                                (String) res.child("creationdate").getValue(), (String) res.child("creationdate").getValue(),
                                (String) res.child("content").getValue(), null, Double.NaN, (Integer) res.child("shares").getValue(Integer.class),
                                (Integer) res.child("likes").getValue(Integer.class), (Integer) res.child("comments").getValue(Integer.class),
                                (Boolean) res.child("isanon").getValue(Boolean.class));
                        dataSnapshot.getRef().setValue(null);
                        FirebaseDatabase.getInstance().getReference("AppEngineResults").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ref.getKey()).removeEventListener(this);
                        listener.onPostsReceived(pst);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onPostsReceived();
                    }
                });
    }

    public static interface PostsReceivedListener {
        public void onPostsReceived(Post... posts);
    }
}
