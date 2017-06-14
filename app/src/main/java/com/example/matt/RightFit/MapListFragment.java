package com.example.matt.RightFit;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.matt.RightFit.firebase_entry.Event;
import com.example.matt.RightFit.firebase_entry.Post;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public Button eventButton, postButton;

    public ListView eventList, postList;
    public TrendingFragment.EventAdapter eventAdapter;
    public ArrayList<Event> eventArray;
    public ArrayList<Post> postArray;
    public ArrayList<String> keys;
    public NotificationFragment.PostAdapter postAdapter;

    public double curLatitude;
    public double curLongitude;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    public GeoQuery eventQuery;
    public GeoQuery postQuery;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.map_as_list, container, false);

        eventArray = new ArrayList<>();
        postArray = new ArrayList<>();
        eventList = (ListView) r.findViewById(R.id.map_listview);
        postList = (ListView) r.findViewById(R.id.map_listview);

        eventButton = (Button) r.findViewById(R.id.events_button);
        eventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventArray = new ArrayList<>();
                eventAdapter = new TrendingFragment.EventAdapter(getContext(), eventArray);
                eventList.setAdapter(eventAdapter);
                queryEvents();
            }
        });
        postButton = (Button) r.findViewById(R.id.posts_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postArray = new ArrayList<>();
                postAdapter = new NotificationFragment.PostAdapter(getContext(), postArray);
                postList.setAdapter(postAdapter);
                queryPosts();
            }
        });


        return r;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setContentView(R.layout.map_as_list);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }


    private void queryEvents() {

    }

    private void queryPosts() {

    }

    private void handleNewLocation(Location location) {
        eventQuery.setCenter(new GeoLocation(curLatitude = location.getLatitude(), curLongitude = location.getLongitude()));
        postQuery.setCenter(new GeoLocation(curLatitude, curLongitude));

        curLatitude = location.getLatitude();
        curLongitude = location.getLongitude();

    }

    public void drawPointsWithinUserRadius() {


        DatabaseReference eventsOnMap = FirebaseDatabase.getInstance().getReference("EventLocations");
        GeoFire eventFire = new GeoFire(eventsOnMap);

        DatabaseReference postsOnMap = FirebaseDatabase.getInstance().getReference("PostLocations");
        GeoFire postFire = new GeoFire(postsOnMap);

        Log.i("locationlattt", Double.toString(curLatitude));

        eventQuery = eventFire.queryAtLocation(new GeoLocation(curLatitude, curLongitude), 1); // 12800.0);
        postQuery = postFire.queryAtLocation(eventQuery.getCenter(), 1);//12800.0);

        eventQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String s, GeoLocation l) {

                LatLng location = new LatLng(l.latitude, l.longitude);

                Log.i("EVenttt", s);
                storeEventToList(s);
            }  // have discovered an event, so put it in hashmap and put a marker for it

            @Override
            public void onKeyMoved(String s, GeoLocation l) {

                LatLng location = new LatLng(l.latitude, l.longitude);

            }  // event has been moved, so move marker

            @Override
            public void onKeyExited(String s) {
            }  // event no longer in range, so remove it from hashmaps and map

            @Override
            public void onGeoQueryError(DatabaseError e) {
                Log.d("ERROR", "GEOQUERY EVENT ERROR");
            }

            @Override
            public void onGeoQueryReady() {

            }
        });

        postQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String s, GeoLocation l) {
                LatLng location = new LatLng(l.latitude, l.longitude);
                //here

            }

            @Override
            public void onKeyMoved(String s, GeoLocation l) {

                LatLng location = new LatLng(l.latitude, l.longitude);


            }

            @Override
            public void onKeyExited(String s) {
            }

            @Override
            public void onGeoQueryError(DatabaseError e) {
                Log.d("ERROR", "GEOQUERY POST ERROR");
            }

            @Override
            public void onGeoQueryReady() {

            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("MapListFragment", "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            //if it cannot, then it requests for the location from client
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {

            curLatitude = location.getLatitude();
            curLongitude = location.getLongitude();
        }

        drawPointsWithinUserRadius();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    public void storeEventToList(String eventKey) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Event");
        ref.child(eventKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event ev = dataSnapshot.getValue(Event.class);
                eventArray.add(ev);
                Log.d("goteventtt", eventArray.get(0).eventID);
                eventAdapter = new TrendingFragment.EventAdapter(getContext(), eventArray);
                eventList.setAdapter(eventAdapter);
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
