package com.example.matt.rightfit;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.matt.rightfit.custom.view.UserPostView;

/**
 * Created by Bradley Wang on 3/6/2017.
 */

public class ViewPostDialogFragment extends DialogFragment {
    UserPostView postView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.post_view_dialog_layout, container, false);

        postView = (UserPostView) r.findViewById(R.id.user_post);
        postView.getPost(getArguments().getString("post_id")); // Can be modified

        return r;
    }

    public static ViewPostDialogFragment createInstance(String postID) {
        Bundle args = new Bundle();
        args.putString("post_id", postID);
        ViewPostDialogFragment r = new ViewPostDialogFragment();
        r.setArguments(args);

        return r;
    }
}
