package com.example.matt.rightfit.firebase_entry;

import com.firebase.client.ServerValue;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by NatSand on 5/23/17.
 */

public class Shares {

    public String type;
    public String id;
    public Object timeStamp;

    Shares(String Type, String ID){ type = Type; id = ID; timeStamp = ServerValue.TIMESTAMP;}

    public Shares() {}

    public static void Share(String type, String id, String user){

        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference("Shares").child(user).child(id);

        rootref.setValue(new Shares(type, id));


    }

}

