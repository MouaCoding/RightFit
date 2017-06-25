package com.example.matt.rightfit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.Comments;
import com.example.matt.rightfit.firebase_entry.Event;
import com.example.matt.rightfit.firebase_entry.Post;
import com.example.matt.rightfit.firebase_entry.User;
import com.example.matt.rightfit.util.CircleTransform;
import com.firebase.client.ServerValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.matt.rightfit.NotificationFragment.app;


/**
 * Created by NatSand on 4/25/17.
 */

public class CommentsListActivity extends Activity {

    private ImageButton backButton;
    private Button postButton;
    private CheckBox anon;
    private EditText content;
    private ListView mListView;
    private ArrayList<Comments> mComments;
    private static CommentsAdapter mAdapter;
    public App mApp;

    String postID;
    String type;





    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postID = getIntent().getStringExtra("postID");
        type = getIntent().getStringExtra("type");
        setContentView(R.layout.comment_list);

        mApp = (App) getApplicationContext();
        mComments = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.comment_list_view);
        mAdapter = new CommentsAdapter(CommentsListActivity.this, mComments, type);
        mListView.setAdapter(mAdapter);


        content = (EditText) findViewById(R.id.Comment_content);

        postButton = (Button) findViewById(R.id.Comment_post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mAdapter.clear();
                String temp = content.getText().toString();
                temp = temp.trim();
                if (temp.length() > 0) {
                    Comments comment = createComment(FirebaseAuth.getInstance().getCurrentUser().getUid(), postID, temp, 0, null, false);
                    mAdapter.add(comment);
                    mAdapter.notifyDataSetChanged();
                    content.setText("");
                    if(type.equals("Event"))
                        Event.changeCount("comments", postID, true);
                    else if(type.equals("Post"))
                        Post.changeCount("comments", postID, true);

                } else {
                    Toast.makeText(getApplicationContext(), "Please Enter Comment", Toast.LENGTH_SHORT).show();
                }

            }
        });
        backButton = (ImageButton) findViewById(R.id.comment_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getComments(postID);

    }


    public void getComments(String postID) {
        Toast.makeText(getApplicationContext(), "Comments Section", Toast.LENGTH_LONG).show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postID);
        ref.orderByChild("timestamp_create");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                        Comments comment = commentSnapshot.getValue(Comments.class);
                        mComments.add(comment);
                        mAdapter.notifyDataSetChanged();
                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });


        }

    public Comments createComment(String userID, String postID, String Content, int Order, String responseID, boolean anon) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference rootreference = FirebaseDatabase.getInstance().getReference("Comments").child(postID);
        DatabaseReference reference;
        if (Order == 0) {
            reference = rootRef.child("comment").push();
        } else {
            reference = rootRef.child("comment").child(postID).child(responseID).push();
        }
        String key = reference.getKey();

        DatabaseReference createdComment = FirebaseDatabase.getInstance().getReference("Comments").child(postID).child(key);
        Comments Result = new Comments(userID, key, Content, postID, Order, 0, 0, anon, ServerValue.TIMESTAMP);
        createdComment.setValue(Result);
        createdComment.child("timestamp_create").setValue(ServerValue.TIMESTAMP);

        return Result;
    }

    public static class CommentsAdapter extends ArrayAdapter<Comments> {

        private ArrayList<Comments> mComments;
        private ImageButton options;
        int postDeleted = 0;
        String type;

        CommentsAdapter(Context context, ArrayList<Comments> commentses, String Type){
            super(context, R.layout.comment_post_display, R.id.comment_content, commentses);
            mComments = commentses;
            type = Type;

        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            final Comments comment = getItem(position);


            final ImageView miniProfilePicture = (ImageView) convertView.findViewById(R.id.comment_profile_picture);;
            final TextView displayName= (TextView) convertView.findViewById(R.id.comment_displayName);
            final TextView handle= (TextView) convertView.findViewById(R.id.comment_handle);
            final TextView content = (TextView) convertView.findViewById(R.id.comment_content);



            setOptions(convertView, comment.commentID, comment.ownerID, comment.responseID, type);

            if (comment.isAnon) {
                displayName.setText("Anonymous");
                handle.setText("");
                Picasso.with(getContext()).load(R.drawable.happy).transform(new CircleTransform()).into(miniProfilePicture);
                miniProfilePicture.setClickable(false);
                displayName.setClickable(false);
                handle.setClickable(false);
            } else {
                try {
                    User.requestUser(comment.ownerID.toString(), "auth", new User.UserReceivedListener() {
                        @Override
                        public void onUserReceived(User... users) {
                            User usr = users[0];

                            displayName.setText(usr.DisplayName);
                            handle.setText(usr.handle);
                            try {
                                if (usr.ProfilePicture != null)
                                    //Convert the URL to aa Bitmap using function, then set the profile picture
                                    Picasso.with(getContext()).load(usr.ProfilePicture).transform(new CircleTransform()).into(miniProfilePicture);
                                else
                                    Picasso.with(getContext()).load(R.drawable.happy).transform(new CircleTransform()).into(miniProfilePicture);
                            } catch (Exception e) {
                            }
                        }
                    });
                } catch (Exception e) {
                }
            }
            content.setText(comment.content);

            if(!comment.isAnon) {
                displayName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ViewUserActivity.class);
                        intent.putExtra("otherUserID", comment.ownerID);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                });
                miniProfilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ViewUserActivity.class);
                        intent.putExtra("otherUserID", comment.ownerID);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                });
            }

            return convertView;
        }


        public void setOptions(final View view, final String commentID, final String ownerID, final String responseID, final String type)
        {


            options = (ImageButton) view.findViewById(R.id.comment_more_options);
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu(view, ownerID, commentID, responseID, type);
                }
            });



        }


        public void popupMenu(View view, final String ownerID, final String commentID, final String responseID, final String type)
        {
//
            options = (ImageButton) view.findViewById(R.id.comment_more_options);
            final PopupMenu popup = new PopupMenu(view.getContext(), options);
            popup.getMenuInflater().inflate(R.menu.comment_options, popup.getMenu());
            if(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(ownerID))
                popup.getMenu().findItem(R.id.delete_comment).setVisible(true);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int i = item.getItemId();
                    if (i == R.id.delete_comment) {
                        promptDelete(ownerID, responseID, commentID, type );
                        return true;
                    }
                    if (i == R.id.report_comment) {
                        Toast.makeText(getContext(),"Reporting Comment...",Toast.LENGTH_SHORT).show();
                        reportComment(ownerID, responseID, commentID, type);
                        return true;
                    }
                    else {
                        return onMenuItemClick(item);
                    }
                }
            });
            popup.show();
        }

        public void promptDelete(final String ownerID, final String responseID, final String commentID, final String type) {
            android.support.v7.app.AlertDialog.Builder dlgAlert = new android.support.v7.app.AlertDialog.Builder(getContext());
            dlgAlert.setMessage("Are you sure you want to delete this comment? ? This action cannot be undone!");
            dlgAlert.setTitle("Delete Comment?");

            dlgAlert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //Perform delete
                    Toast.makeText(getContext(), "Deleting Comment...", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference().child("Comments").child(responseID).child(commentID).removeValue();
                    Toast.makeText(getContext(), "Comment Deleted!", Toast.LENGTH_SHORT).show();
                    mAdapter.notifyDataSetChanged();
                    if(type.equals("Post")){
                        Post.changeCount("comments", responseID, false);
                    }
                    else if(type.equals("Event")){
                        Event.changeCount("comments", responseID, false);

                    }

                }
            });

            //if user cancels
            dlgAlert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });

            dlgAlert.setCancelable(true);
            dlgAlert.create();
            dlgAlert.show();
        }

        public void reportComment(final String ownerID, final String responseID, final String commentID, final String type) {
            android.util.Log.e("nat", responseID + commentID + type);
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments");
            ref.child(responseID).child(commentID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ((String) dataSnapshot.child("content").getValue() == null) return;
                    else {
                        if (!dataSnapshot.child("numberOfReports").exists()){
                            android.util.Log.e("Nat", "got under numberOfReports");
                            ref.child(responseID).child(commentID).child("numberOfReports").setValue(0);}
                        else {
                            long numberOfReports = (long) dataSnapshot.child("numberOfReports").getValue();
                            //parse whitespace
                            String[] content = ((String) dataSnapshot.child("content").getValue()).split("\\s+");
                            if (hasBadWord(content)) {
                                numberOfReports++;
                                ref.child(responseID).child(commentID).child("numberOfReports").setValue(numberOfReports);
                                //TODO: set the amount of reports before a event is deleted
                                if (numberOfReports > 5) {
                                    FirebaseDatabase.getInstance().getReference().child("Comments").child(responseID).child(commentID).removeValue();
                                    mAdapter.notifyDataSetChanged();
                                    if(type.equals("Post")){
                                        Post.changeCount("comments", responseID, false);
                                    }
                                    else if(type.equals("Event")){
                                        Event.changeCount("comments", responseID, false);

                                    }

                                }
                            } //Has bad word
                        }//else number of reports exists
                    }//else event has content
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public boolean hasBadWord(String[] content) {
            android.util.Log.e("Nat", "hasBadWord");
            for(String c : content) {
                for (String badWord : app.badWords) {
                    c = c.toLowerCase();
                    if (c.contains(badWord)) {
                        Toast.makeText(getContext(), "Comment has been reported.", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
            }
            Toast.makeText(getContext(), "There is nothing to report.", Toast.LENGTH_SHORT).show();
            return false;
        }

        public int getCount() {

            return mComments.size();
        }

        //Get the data item associated with the specified position in the data set.
        @Override
        public Comments getItem(int position) {

            return mComments.get(position);
        }

        //Get the row id associated with the specified position in the list.
        @Override
        public long getItemId(int position) {

            return position;
        }
    }
}
