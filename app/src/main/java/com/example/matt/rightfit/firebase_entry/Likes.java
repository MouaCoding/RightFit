package com.example.matt.rightfit.firebase_entry;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by NatSand on 5/6/17.
 */

public class Likes {


    public boolean status;
    public static boolean result;

    Likes(){
        status = true;
    }

    public static void Like(int type, String id, String user){
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference("Likes").child(id).child(user);
        rootref.setValue(new Likes());

    }
    public static void Unlike(int type, String id, String user) {
        FirebaseDatabase.getInstance().getReference("Likes").child(id).child(user).removeValue();

    }


    public static boolean hasLiked(int type, String id, final String user){
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference("Likes").child(id);

        rootref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user)){
                    result = true;
                }
                else{
                    result = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return result;
    }
}
