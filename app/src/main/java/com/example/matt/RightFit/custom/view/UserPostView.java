package com.example.matt.RightFit.custom.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matt.RightFit.App;
import com.example.matt.RightFit.CommentsListActivity;
import com.example.matt.RightFit.R;
import com.example.matt.RightFit.ViewPostActivity;
import com.example.matt.RightFit.firebase_entry.Post;
import com.example.matt.RightFit.util.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by Bradley Wang on 3/6/2017.
 * edited by Matt
 */

public class UserPostView extends FrameLayout {
    public App app = (App) getApplicationContext();
    private UserMiniHeaderView postMakerHeader;

    private TextView postBodyTextView;
    private TextView likesCount;
    private TextView commentsCount;
    private TextView sharesCount;
    private ImageView postImage;

    private ImageButton likeButton;
    private ImageButton commentButton;
    private ImageButton shareButton;
    private ImageButton options;

    private String PostID;
    private String currUsr = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

    public UserPostView(Context context) {
        super(context);
        createView();
    }

    public UserPostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    public void createView() {
        inflate(getContext(), R.layout.user_post_layout, this);

        postMakerHeader = (UserMiniHeaderView) findViewById(R.id.user_post_mini_head);

        postBodyTextView = (TextView) findViewById(R.id.user_post_body);
        likesCount = (TextView) findViewById(R.id.user_post_like_count);
        commentsCount = (TextView) findViewById(R.id.user_post_comment_count);
        sharesCount = (TextView) findViewById(R.id.user_post_share_count);

        likeButton = (ImageButton) findViewById(R.id.user_post_like_button);
        commentButton = (ImageButton) findViewById(R.id.user_post_comment_button);
        shareButton = (ImageButton) findViewById(R.id.user_post_share_button);
        postImage = (ImageView) findViewById(R.id.view_post_image);
        likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               FirebaseDatabase.getInstance().getReference().child("Likes").child(PostID)
                       .addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {
                               if(dataSnapshot.hasChild(currUsr)){
                                   likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorTextDark));
                                   Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                                   Post.Unlike(PostID, currUsr);
                                   updateCounts(PostID);

                               }
                                 else{
                                   likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.crimson));
                                   Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
                                   Post.Like(PostID, currUsr);
                                   updateCounts(PostID);
                               }

                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {

                           }
                       });
            }
        });
        commentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                Bundle params = new Bundle();
                Intent intent = new Intent(context, CommentsListActivity.class);
                intent.putExtra("postID", PostID.toString());
                intent.putExtra("type", "Post");
                //context.startActivityForResult(intent, RC);
                context.startActivity(intent);
                updateCounts(PostID);


            }
        });
        shareButton = (ImageButton) findViewById(R.id.user_post_share_button);
        options = (ImageButton) findViewById(R.id.mini_profile_more_button);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.MainBlue));
                Post.Share(PostID, currUsr);
                updateCounts(PostID);
            }
        });




    }

    public void getPost(final String postID) {
      Post.requestPost(postID, "authToken", new Post.PostReceivedListener() {
          @Override
          public void onPostReceived(Post... posts) {
              Post pst = posts[0];
              PostID = postID;
              setPost(pst);
          }
      });
    }

    public void setPost(final Post p) {
        if(p == null)
            return;

        if(p.isAnon == true){
            postMakerHeader.anonUser();
        }
        else {
            postMakerHeader.getUser(p.ownerID);
        }
        postBodyTextView.setText(p.content);
        likesCount.setText(Integer.toString(p.likes));
        commentsCount.setText(Integer.toString(p.comments));
        sharesCount.setText(Integer.toString(p.shares));

        try{
            if(p.PostPicture != null) postImage.setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(p.PostPicture).transform(new CircleTransform()).into(postImage);
        }catch (Exception e){}


        postBodyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),ViewPostActivity.class);
                intent.putExtra("postid",p.postID);
                intent.putExtra("type", "Event");
                intent.putExtra("ownerID", p.ownerID);
                getContext().startActivity(intent);
            }
        });
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),ViewPostActivity.class);
                intent.putExtra("postid",p.postID);
                intent.putExtra("type", "Event");
                intent.putExtra("ownerID", p.ownerID);
                getContext().startActivity(intent);
            }
        });

        options.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu(getRootView(),p.ownerID,p.postID);
            }
        });

    }

    public void popupMenu(View view, final String ownerID, final String postID)
    {
        options = (ImageButton) view.findViewById(R.id.mini_profile_more_button);
        final PopupMenu popup = new PopupMenu(view.getContext(), options);
        popup.getMenuInflater().inflate(R.menu.post_options, popup.getMenu());
        if(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(ownerID))
            popup.getMenu().findItem(R.id.delete).setVisible(true);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.delete) {
                    promptDelete(ownerID, postID);
                    return true;
                }
                if (i == R.id.report_post) {
                    Toast.makeText(getApplicationContext(),"Reporting Post...",Toast.LENGTH_SHORT).show();
                    reportPost(ownerID, postID);
                    return true;
                }
                else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }

    public void promptDelete(final String ownerID, final String postID)
    {
        android.support.v7.app.AlertDialog.Builder dlgAlert = new android.support.v7.app.AlertDialog.Builder(getContext());
        dlgAlert.setMessage("Are you sure you want to delete this post? This action cannot be undone!");
        dlgAlert.setTitle("Delete Post?");

        dlgAlert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Perform delete
                Toast.makeText(getContext(), "Deleting Post...", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference().child("Post").child(postID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("PostLocations").child(postID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("NotificationRequest").child(ownerID).child(postID).removeValue();
                Toast.makeText(getContext(), "Post Deleted!", Toast.LENGTH_SHORT).show();
                //TODO: update likes received...
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

    public void reportPost(final String ownerID, final String postID)
    {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Post");
        ref.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((String) dataSnapshot.child("content").getValue() == null) return;
                else{
                    if(!dataSnapshot.child("numberOfReports").exists())
                        ref.child(postID).child("numberOfReports").setValue(0);
                    else {
                        long numberOfReports = (long) dataSnapshot.child("numberOfReports").getValue();
                        //parse whitespace
                        String[] content = ((String) dataSnapshot.child("content").getValue()).split("\\s+");
                        if (hasBadWord(content)) {
                            numberOfReports++;
                            ref.child(postID).child("numberOfReports").setValue(numberOfReports);
                            //TODO: set the amount of reports before a post is deleted
                            if(numberOfReports > 5) {
                                FirebaseDatabase.getInstance().getReference().child("Post").child(postID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("PostLocations").child(postID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("NotificationRequest").child(ownerID).child(postID).removeValue();

                            }
                        } //Has bad word
                    }//else number of reports exists
                }//else post has content
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean hasBadWord(String[] content)
    {
        for(String c : content) {
            for (String badWord : app.badWords) {
                c = c.toLowerCase();
                if (c.contains(badWord)) {
                    Toast.makeText(getContext(), "Event has been reported.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
        Toast.makeText(getContext(), "There is nothing to report.", Toast.LENGTH_SHORT).show();
        return false;
    }


    public void updateCounts(final String postID){
        Post.requestPost(postID, "authToken", new Post.PostReceivedListener() {
            @Override
            public void onPostReceived(Post... posts) {
                Post pst = posts[0];
                try{
                    likesCount.setText(Integer.toString(pst.likes));
                    commentsCount.setText(Integer.toString(pst.comments));
                    sharesCount.setText(String.valueOf(pst.shares));


                } catch(Exception e){}
            }
        });
    }
}
