package com.example.matt.rightfit.firebase_entry;


import com.firebase.client.ServerValue;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Natsand on 4/11/2017.
 */

public class Comments {
    public String   ownerID,
            commentID,
            createDate,
            createTime,
            content,
            responseID;

    public Object timestamp_create;
    //  TODO: NS: Change dates and times to type Date


    public int      order,  // is it an original post (0), response (1), or response to a response (2)
                    likes,
                    replies;

    public boolean  isAnon;

    public Comments() {}

    public Comments(String aOwner, String aID, String aContent,
                String aResponseID, int aOrder, int aLikes, int aReplies, boolean Anon, Object timeStamp) {

        ownerID     = aOwner;
        commentID      = aID;

        content     = aContent;

        responseID  = aResponseID;


        order       = aOrder;
        likes       = aLikes;
        replies     = aReplies;

        isAnon      = Anon;

        timestamp_create = timeStamp;
    }

    public static void requestComment(String commentID, final CommentReceivedListener listener){
        if(listener == null)
            return;
        FirebaseDatabase.getInstance().getReference("Comments").child(commentID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Comments comment = dataSnapshot.getValue(Comments.class);
                        listener.onCommentReceived(comment);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onCommentReceived(null);
                    }
                });
    }

    public static interface CommentReceivedListener {
        public void onCommentReceived(Comments...commentses);
    }
    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setCommentID(String CommentID) {
        this.commentID = CommentID;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setContent(String content) { this.content = content; }

    public void setResponseID(String responseID) {
        this.responseID = responseID;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public static void Comment(String userID, String postID, String Content, int Order, String responseID, boolean Anon) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Comments");
        DatabaseReference reference = rootRef.child(postID).push();
        String key = reference.getKey();
        DatabaseReference createdComment = FirebaseDatabase.getInstance().getReference("Comments").child(postID).child(key);
        createdComment.setValue(new Comments(userID, key, Content, postID, Order, 0, 0, Anon, ServerValue.TIMESTAMP));

    }

    public static void changeCount(String type, String commentID, String PostID, final boolean inc) {

        FirebaseDatabase.getInstance().getReference("Comments").child(PostID).child(commentID).child(type).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() == null){
                    mutableData.setValue(0);

                }
                else {
                    int count = mutableData.getValue(Integer.class);
                    if(inc){
                        mutableData.setValue(count + 1);
                    }
                    else{
                        mutableData.setValue(count - 1);
                    }


                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }

        });

    }
}
