package com.example.matt.RightFit;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.matt.RightFit.custom.view.UserEventView;

/**
 * Created by Bradley Wang on 3/6/2017.
 */

public class ViewEventDialogFragment extends DialogFragment {
    UserEventView eventView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.event_view_dialog_layout, container, false);

        eventView = (UserEventView) r.findViewById(R.id.user_event);
        eventView.getEvent(getArguments().getString("event_id")); // Can be modified

        return r;
    }

    public static ViewEventDialogFragment createInstance(String eventID) {
        Bundle args = new Bundle();
        args.putString("event_id", eventID);
        ViewEventDialogFragment r = new ViewEventDialogFragment();
        r.setArguments(args);

        return r;
    }
}
