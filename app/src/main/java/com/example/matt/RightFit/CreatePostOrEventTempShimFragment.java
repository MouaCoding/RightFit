package com.example.matt.RightFit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Bradley Wang on 2/26/2017.
 */

public class CreatePostOrEventTempShimFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.post_event_create_shim_layout, container, false);

        Button b = (Button) r.findViewById(R.id.create_post_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                if (manager.findFragmentById(R.id.post_event_create_shim_fragment_container) != null)
                    manager.beginTransaction()
                            .replace(R.id.post_event_create_shim_fragment_container, new CreatePostFragment())
                            .addToBackStack(null)
                            .commit();
                else
                    manager.beginTransaction()
                            .add(R.id.post_event_create_shim_fragment_container, new CreatePostFragment())
                            .addToBackStack(null)
                            .commit();
            }
        });

        b = (Button) r.findViewById(R.id.create_event_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                if (manager.findFragmentById(R.id.post_event_create_shim_fragment_container) != null)
                    manager.beginTransaction()
                            .replace(R.id.post_event_create_shim_fragment_container, new CreateEventFragment())
                            .addToBackStack(null)
                            .commit();
                else
                    manager.beginTransaction()
                            .add(R.id.post_event_create_shim_fragment_container, new CreateEventFragment())
                            .addToBackStack(null)
                            .commit();
            }
        });
        return r;
    }
}
