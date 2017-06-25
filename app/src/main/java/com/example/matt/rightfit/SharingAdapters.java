package com.example.matt.rightfit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.Event;
import com.example.matt.rightfit.firebase_entry.FollowingUser;
import com.example.matt.rightfit.firebase_entry.Post;
import com.example.matt.rightfit.firebase_entry.User;
import com.example.matt.rightfit.util.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.matt.rightfit.NotificationFragment.app;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by NatSand on 5/31/17.
 */

public class SharingAdapters {


    private SharedPostAdapter mPostAdapter;
    private SharedEventAdapter mEventAdapter;

    public static class SharedPostAdapter extends ArrayAdapter<Post> {


        private ArrayList<Post> mPosts;
        private ArrayList<Post> mPostsFilter;
        private boolean Owner;
        private ImageButton likeButton;
        private ImageButton commentButton;
        private ImageButton shareButton;
        private ImageButton options;
        int postDeleted = 0;

        SharedPostAdapter(Context context, ArrayList<Post> users, boolean isOwner){
            super(context, R.layout.user_post_framed_layout/*user_item*/, R.id.mini_name, users);
            mPostsFilter = users;
            mPosts = users;
            Owner = isOwner;
            getFilter();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            final Post post = getItem(position);

            TextView postBodyTextView = (TextView) convertView.findViewById(R.id.user_post_body);
            ImageView miniProfilePicView = (ImageView) convertView.findViewById(R.id.mini_profile_picture);;
            TextView displayNameView= (TextView) convertView.findViewById(R.id.mini_name);
            TextView userHandleView= (TextView) convertView.findViewById(R.id.mini_user_handle);
            TextView numLikes = (TextView) convertView.findViewById(R.id.user_post_like_count);
            TextView numComments = (TextView) convertView.findViewById(R.id.user_post_comment_count);
            ImageButton followButton = (ImageButton) convertView.findViewById(R.id.mini_profile_add_button);
            if (post.ownerID != FirebaseAuth.getInstance().getCurrentUser().getUid())
                followButton(followButton,FirebaseAuth.getInstance().getCurrentUser().getUid(), post.ownerID);


            setButtons(convertView, post.postID, FirebaseAuth.getInstance().getCurrentUser().getUid());
            if(postDeleted == 0)
                setExtraValues(post.postID, post.ownerID);

            displayNameView.setText(post.DisplayName);
            userHandleView.setText(post.handle);
            postBodyTextView.setText(post.content);
            numLikes.setText(Integer.toString(post.likes));
            numComments.setText(Integer.toString(post.comments));
            try {
                if (post.ProfilePicture != null)
                    Picasso.with(getContext()).load(post.ProfilePicture).transform(new CircleTransform()).into(miniProfilePicView);
                else
                    Picasso.with(getContext()).load(R.mipmap.ic_launcher).transform(new CircleTransform()).into(miniProfilePicView);
            } catch(Exception e){}
            displayNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),ViewPostActivity.class);
                    intent.putExtra("postid",post.postID);
                    getContext().startActivity(intent);
                }
            });
            miniProfilePicView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),ViewPostActivity.class);
                    intent.putExtra("postid",post.postID);
                    getContext().startActivity(intent);
                }
            });
            return convertView;
        }

        private void followButton(ImageButton followButton, final String curUserID, final String otherUserID) {
            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (curUserID != null && curUserID != otherUserID) {
                        Toast.makeText(getApplicationContext(),"Followed!", Toast.LENGTH_SHORT).show();
                        FirebaseDatabase.getInstance().getReference("User").child(curUserID).child("Following")
                                .child(otherUserID).setValue(new FollowingUser());
                        incrementFollowers(otherUserID);
                    }
                }
            });
        }

        public void incrementFollowers(final String otherID)
        {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child(otherID);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User follow = dataSnapshot.getValue(User.class);
                    int followerNumber = follow.NumberFollowers;
                    followerNumber++;
                    ref.child("NumberFollowers").setValue(followerNumber);
                    FirebaseDatabase.getInstance().getReference("User").child(otherID).child("Followers")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new FollowingUser());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setButtons(final View view, final String PostID, final String ownerID)
        {
            likeButton = (ImageButton) view.findViewById(R.id.user_post_like_button);
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(PostID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(ownerID)){
                                        likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorTextDark));
                                        Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                                        Post.Unlike(PostID, ownerID);
                                        updateCounts(PostID, view);

                                    }
                                    else{
                                        likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.crimson));
                                        Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
                                        Post.Like(PostID, ownerID);
                                        updateCounts(PostID, view);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            });

            commentButton = (ImageButton) view.findViewById(R.id.user_post_comment_button);
            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    Intent intent = new Intent(context, CommentsListActivity.class);
                    intent.putExtra("postID", PostID.toString());
                    intent.putExtra("type", "Post");
                    context.startActivity(intent);
                    updateCounts(PostID,view);
                }
            });

            shareButton = (ImageButton) view.findViewById(R.id.user_post_share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.MainBlue));
                    Post.Share(PostID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                    updateCounts(PostID,view);
                }
            });

            options = (ImageButton) view.findViewById(R.id.mini_profile_more_button);
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu(view, ownerID, PostID);
                }
            });



        }


        public void updateCounts(final String postID, View convertView){
            final TextView numLikes = (TextView) convertView.findViewById(R.id.user_post_like_count);
            final TextView numComments = (TextView) convertView.findViewById(R.id.user_post_comment_count);
            final TextView sharesCount = (TextView) convertView.findViewById(R.id.user_post_share_count);
            Post.requestPost(postID, "authToken", new Post.PostReceivedListener() {
                @Override
                public void onPostReceived(Post... posts) {
                    Post pst = posts[0];
                    try{
                        numLikes.setText(Integer.toString(pst.likes));
                        numComments.setText(Integer.toString(pst.comments));
                        sharesCount.setText(String.valueOf(pst.shares));


                    } catch(Exception e){}
                }
            });
        }

        @Override
        public int getCount() {

            return mPosts.size();
        }

        //Get the data item associated with the specified position in the data set.
        @Override
        public Post getItem(int position) {

            return mPosts.get(position);
        }

        //Get the row id associated with the specified position in the list.
        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults results = new FilterResults();

                    //If there's nothing to filter on, return the original data for your list
                    if (charSequence != null && charSequence.length() > 0) {
                        ArrayList<Post> filterList = new ArrayList<Post>();
                        for (int i = 0; i < mPostsFilter.size(); i++) {

                            if (mPostsFilter.get(i).DisplayName.contains(charSequence)) {
                                filterList.add(mPostsFilter.get(i));
                            }
                        }
                        results.count = filterList.size();
                        results.values = filterList;
                    } else {
                        results.count = mPostsFilter.size();
                        results.values = mPostsFilter;
                    }

                    return results;
                }

                //Invoked in the UI thread to publish the filtering results in the user interface.
                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint,
                                              FilterResults results) {
                    mPosts = (ArrayList<Post>) results.values;
                    notifyDataSetChanged();
                }
            };
        }

        public void setExtraValues(final String postID, final String ownerID)
        {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("User").child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User owner = dataSnapshot.getValue(User.class);
                    ref.child("Post").child(postID).child("DisplayName").setValue(owner.DisplayName);
                    ref.child("Post").child(postID).child("handle").setValue(owner.handle);
                    try{
                        ref.child("Post").child(postID).child("ProfilePicture").setValue(owner.ProfilePicture);
                    }catch (Exception e){}
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }

        public void popupMenu(View view, final String ownerID, final String postID)
        {
//            MenuItem menuItem = (MenuItem) view.findViewById(R.id.delete);
            options = (ImageButton) view.findViewById(R.id.mini_profile_more_button);
            final PopupMenu popup = new PopupMenu(view.getContext(), options);
            popup.getMenuInflater().inflate(R.menu.shared_post_options, popup.getMenu());
            if(Owner)
                popup.getMenu().findItem(R.id.Unshare_Post).setVisible(true);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int i = item.getItemId();
                    if (i == R.id.Unshare_Post) {
                        promptUnshare(ownerID, postID);
                        return true;
                    }
                    if (i == R.id.Report_Shared_Post) {
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

        public void promptUnshare(final String ownerID, final String postID)
        {
            android.support.v7.app.AlertDialog.Builder dlgAlert = new android.support.v7.app.AlertDialog.Builder(getContext());
            dlgAlert.setMessage("Are you sure you want to unshare this post? This action cannot be undone!");
            dlgAlert.setTitle("Unshare Post?");

            dlgAlert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //Perform delete
                    Toast.makeText(getContext(), "Unsharing Post...", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference().child("Shares").child(ownerID).child(postID).removeValue();
                    Toast.makeText(getContext(), "Post Unshared!", Toast.LENGTH_SHORT).show();

                    postDeleted = 1;
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

        public void setOwner(boolean isowner){
            Owner = isowner;
        }

    }




    public static class SharedEventAdapter extends ArrayAdapter<Event> {
        private ImageButton likeButton;
        private ImageButton commentButton;
        private ImageButton shareButton;
        private ImageButton options;
        private boolean Owner;

        private int eventDeleted = 0;

        SharedEventAdapter(Context context, ArrayList<Event> users, boolean isOwner) {
            super(context, R.layout.user_event_framed_layout, R.id.user_event_title, users);
            android.util.Log.e("nat", "got here");
            Owner = isOwner;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            android.util.Log.e("nat", "in getView");
            final Event event = getItem(position);
            android.util.Log.e("nat", event.eventID);
            TextView eventTitle = (TextView) convertView.findViewById(R.id.user_event_title);
            ImageView eventImage = (ImageView) convertView.findViewById(R.id.user_event_mini_image);
            TextView startTime = (TextView) convertView.findViewById(R.id.user_event_start_time);
            TextView endTime = (TextView) convertView.findViewById(R.id.user_event_end_time);
            TextView eventLoc = (TextView) convertView.findViewById(R.id.user_event_location);
            TextView numLikes = (TextView) convertView.findViewById(R.id.user_event_like_count);
            TextView numComments = (TextView) convertView.findViewById(R.id.user_event_comment_count);

            TextView displayNameView = (TextView) convertView.findViewById(R.id.mini_name);
            ImageView profilePicture = (ImageView) convertView.findViewById(R.id.mini_profile_picture);
            TextView userHandleView = (TextView) convertView.findViewById(R.id.mini_user_handle);
            ImageButton followButton = (ImageButton) convertView.findViewById(R.id.mini_profile_add_button);
            if (event.ownerID != FirebaseAuth.getInstance().getCurrentUser().getUid())
                followButton(followButton,FirebaseAuth.getInstance().getCurrentUser().getUid(), event.ownerID);


            eventTitle.setText(event.eventName);
            startTime.setText(event.startTime);
            endTime.setText(event.endTime);
            eventLoc.setText(event.address);
            numLikes.setText(Integer.toString(event.likes));
            numComments.setText(Integer.toString(event.comments));

            displayNameView.setText(event.DisplayName);
            userHandleView.setText(event.handle);

            setButtons(convertView, event.eventID, FirebaseAuth.getInstance().getCurrentUser().getUid());
            if (eventDeleted == 0)
                setExtraValues(event.eventID, event.ownerID);

            try {
                if (event.userProfilePicture != null)
                    Picasso.with(getContext()).load(event.userProfilePicture).transform(new CircleTransform()).into(profilePicture);
                else
                    Picasso.with(getContext()).load(R.mipmap.ic_launcher).transform(new CircleTransform()).into(profilePicture);
            } catch (Exception e) {}
            try {
                if (event.ProfilePicture != null)
                    Picasso.with(getContext()).load(event.ProfilePicture).into(eventImage);
                else
                    Picasso.with(getContext()).load(R.drawable.images).into(eventImage);
            } catch (Exception e) {}

            //On clicks to navigate to view user or event
            displayNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ViewUserActivity.class);
                    intent.putExtra("otherUserID", event.ownerID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ViewUserActivity.class);
                    intent.putExtra("otherUserID", event.ownerID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            eventTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ViewEventActivity.class);
                    intent.putExtra("eventid", event.eventID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            eventImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ViewEventActivity.class);
                    intent.putExtra("eventid", event.eventID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            return convertView;
        }

        private void followButton(ImageButton followButton, final String curUserID, final String otherUserID) {
            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (curUserID != null && curUserID != otherUserID) {
                        Toast.makeText(getApplicationContext(),"Followed!", Toast.LENGTH_SHORT).show();
                        FirebaseDatabase.getInstance().getReference("User").child(curUserID).child("Following")
                                .child(otherUserID).setValue(new FollowingUser());
                        incrementFollowers(otherUserID);
                    }
                }
            });
        }

        public void incrementFollowers(final String otherID)
        {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child(otherID);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User follow = dataSnapshot.getValue(User.class);
                    int followerNumber = follow.NumberFollowers;
                    followerNumber++;
                    ref.child("NumberFollowers").setValue(followerNumber);
                    FirebaseDatabase.getInstance().getReference("User").child(otherID).child("Followers")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new FollowingUser());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void setButtons(final View view, final String EventID, final String currUsr) {
            likeButton = (ImageButton) view.findViewById(R.id.user_event_like_button);
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(EventID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(currUsr)){
                                        likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorTextDark));
                                        Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                                        Event.Unlike(EventID, currUsr);
                                        updateCounts(EventID, view);

                                    }
                                    else{
                                        likeButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.crimson));
                                        Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
                                        Event.Like(EventID, currUsr);
                                        updateCounts(EventID, view);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            });

            commentButton = (ImageButton) view.findViewById(R.id.user_event_comment_button);
            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    Bundle params = new Bundle();
                    Intent intent = new Intent(context, CommentsListActivity.class);
                    intent.putExtra("postID", EventID.toString());
                    intent.putExtra("type", "Event");
                    context.startActivity(intent);
                    updateCounts(EventID,view);

                }
            });

            shareButton = (ImageButton) view.findViewById(R.id.user_event_share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.MainBlue));
                    Event.Share(EventID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                    updateCounts(EventID,view);
                }
            });
            options = (ImageButton) view.findViewById(R.id.mini_profile_more_button);
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu(view, currUsr, EventID);
                }
            });

        }

        public void updateCounts(final String eventID, View view){
            final TextView numLikes = (TextView) view.findViewById(R.id.user_event_like_count);
            final TextView numComments = (TextView) view.findViewById(R.id.user_event_comment_count);
            final TextView sharesCount = (TextView) view.findViewById(R.id.user_event_share_count);
            Event.requestEvent(eventID, "authToken", new Event.EventReceivedListener() {
                @Override
                public void onEventReceived(Event... events) {
                    Event ev = events[0];
                    try{
                        numLikes.setText(Integer.toString(ev.likes));
                        numComments.setText(Integer.toString(ev.comments));
                        sharesCount.setText(String.valueOf(ev.shares));


                    } catch(Exception e){}
                }
            });
        }

        public void setExtraValues(final String eventID, final String ownerID) {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("User").child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User owner = dataSnapshot.getValue(User.class);
                    ref.child("Event").child(eventID).child("DisplayName").setValue(owner.DisplayName);
                    ref.child("Event").child(eventID).child("handle").setValue(owner.handle);
                    try {
                        ref.child("Event").child(eventID).child("userProfilePicture").setValue(owner.ProfilePicture);
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }


        public void popupMenu(View view, final String ownerID, final String eventID) {
            options = (ImageButton) view.findViewById(R.id.mini_profile_more_button);
            final PopupMenu popup = new PopupMenu(view.getContext(), options);
            popup.getMenuInflater().inflate(R.menu.shared_event_options, popup.getMenu());
            if (Owner)
                popup.getMenu().findItem(R.id.Unshare_Event).setVisible(true);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int i = item.getItemId();
                    if (i == R.id.Unshare_Event) {
                        promptUnshare(ownerID, eventID);
                        return true;
                    }
                    if (i == R.id.Report_Shared_Event) {
                        Toast.makeText(getApplicationContext(), "Reporting Event...", Toast.LENGTH_SHORT).show();
                        reportEvent(ownerID, eventID);
                        return true;
                    } else {
                        return onMenuItemClick(item);
                    }
                }
            });
            popup.show();
        }

        public void promptUnshare(final String ownerID, final String eventID) {
            android.support.v7.app.AlertDialog.Builder dlgAlert = new android.support.v7.app.AlertDialog.Builder(getContext());
            dlgAlert.setMessage("Are you sure you want to unshare this event? This action cannot be undone!");
            dlgAlert.setTitle("Unshare Event?");

            dlgAlert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    eventDeleted = 1;
                    //Perform delete
                    Toast.makeText(getContext(), "Unsharing Event...", Toast.LENGTH_SHORT).show();

                    FirebaseDatabase.getInstance().getReference().child("Shares").child(ownerID).child(eventID).removeValue();
                    Toast.makeText(getContext(), "Event Unshared!", Toast.LENGTH_SHORT).show();

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

        public void reportEvent(final String ownerID, final String eventID) {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Event");
            ref.child(eventID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ((String) dataSnapshot.child("description").getValue() == null) return;
                    else {
                        if (!dataSnapshot.child("numberOfReports").exists())
                            ref.child(eventID).child("numberOfReports").setValue(0);
                        else {
                            long numberOfReports = (long) dataSnapshot.child("numberOfReports").getValue();
                            //parse whitespace
                            String[] content = ((String) dataSnapshot.child("description").getValue()).split("\\s+");
                            if (hasBadWord(content)) {
                                numberOfReports++;
                                ref.child(eventID).child("numberOfReports").setValue(numberOfReports);
                                //TODO: set the amount of reports before a event is deleted
                                if (numberOfReports > 5) {
                                    FirebaseDatabase.getInstance().getReference().child("Event").child(eventID).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("EventLocations").child(eventID).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("OtherEventLocations").child(eventID).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("PartyEventLocations").child(eventID).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("SportEventLocations").child(eventID).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("EducationEventLocations").child(eventID).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("ClubEventEventLocations").child(eventID).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(eventID).removeValue();
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

        public void setOwner(boolean isowner){
            Owner = isowner;
        }


    }








}
