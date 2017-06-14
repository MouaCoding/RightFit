package com.example.matt.RightFit;

import android.Manifest;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.example.matt.RightFit.firebase_entry.City;
import com.example.matt.RightFit.firebase_entry.Event;
import com.example.matt.RightFit.util.RHRNNotifications;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.facebook.FacebookSdk.getApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATIONS_PERMISSION = 0;
    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public int countNumber = 0;
    public int isEducation = 0, isSports = 0, isParty = 0, isClubEvent = 0, isOther = 0, logout = 0;
    Map<String, Integer> map;
    int[] filter;

    private GoogleMap mMap;
    public MapView mapView;
    public static final String TAG = MapsFragment.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public int radius = 1000;
    private FloatingActionButton button;
    private Menu menu;

    private double kmToMiles = 0.621371;
    public double curLatitude;
    public double curLongitude;
    private boolean first = true;

    private String curUserID;
    private GeoLocation curLocation;

    public DatabaseReference eventsOnMap,
            postsOnMap;

    public BottomNavigationView topNavigationView;

    // eventually remove these
    ValueAnimator temp;
    Marker ourLoc;
    Circle removeCircle;

    public GeoQuery eventQuery;
    public GeoQuery postQuery;

    public Bitmap Marker;

    public HashMap<Marker, String> eventMarkerKeys = new HashMap<Marker, String>();
    public HashMap<String, Marker> eventKeyMarkers = new HashMap<String, Marker>();
    public HashMap<Marker, String> postMarkerKeys = new HashMap<Marker, String>();
    public HashMap<String, Marker> postKeyMarkers = new HashMap<String, Marker>();

    //Globals related to on map long click
    private LinearLayout layoutToAdd;
    private int inflateButtons = 0;

    //Uploading to fb
    // creating an instance of Firebase Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    //creating a storage reference.
    StorageReference storageRef = storage.getReferenceFromUrl("gs://righthererightnow-72e20.appspot.com");
    Uri filePath;

    public ArrayList<Event> eventArrayList;
    public TrendingFragment.EventAdapter eventAdapter;
    public ListView eventListView;
    public int listview = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View r = (View) inflater.inflate(R.layout.maps_fragment_layout, container, false);

        mapView = (MapView) r.findViewById(R.id.primary_map);
//        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

        drawPointsWithinUserRadius();
        //mapView = new MapView(getActivity());
        layoutToAdd = (LinearLayout) r.findViewById(R.id.maps_fragment_layout);


        topNavigationView = (BottomNavigationView) r.findViewById(R.id.top_navigation);

        try {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) topNavigationView.getChildAt(0);
            BottomNavigationItemView city = (BottomNavigationItemView) menuView.getChildAt(2);
            city.setShiftingMode(false);
            city.setChecked(city.getItemData().isChecked());
        } catch (Exception e) {
        }

        topNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem bottom_navigation) {
                        View menuItemView = r.findViewById(R.id.search);
                        View options = r.findViewById(R.id.refresh);
                        switch (bottom_navigation.getItemId()) {
                            case R.id.search:
                                filterMenu(menuItemView);
                                break;
                            case R.id.message:
                                getCurrentUserInfo();
                                break;
                            case R.id.current_city:
                                break;
                            case R.id.favorite: //TODO: Refresh or favorite?
                                Toast.makeText(getApplicationContext(), "Refreshing...", Toast.LENGTH_SHORT).show();
                                drawPointsWithinUserRadius();
                                onResume();
                                Toast.makeText(getApplicationContext(), "Refreshed!", Toast.LENGTH_SHORT).show();
                                //promptFavorite();
                                break;
                            case R.id.refresh:
                                optionsMenu(options);
                                break;
                        }
                        return true;
                    }
                });

        // restore any state here if necessary

        return r;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initializing googleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        Marker = drawMarkerWithSize(100, 100);
        eventArrayList = new ArrayList<>();
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        mGoogleApiClient.connect();
    }


    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        curUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (eventMarkerKeys.containsKey(marker)) {
                    ViewEventDialogFragment.createInstance(eventMarkerKeys.get(marker)).show(getChildFragmentManager(), null);
                }  // if marker clicked is an event

                else if (postMarkerKeys.containsKey(marker)) {
                    ViewPostDialogFragment.createInstance(postMarkerKeys.get(marker)).show(getChildFragmentManager(), null);
                }  // if marker clicked is a post

                return true;
            }
        });  // add listeners for clicking markers

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragEnd(Marker marker) {
                ourLoc.remove();
                removeCircle.remove();
                temp.removeAllListeners();
                temp.end();
                temp.cancel();

                Location newPos = new Location(LocationManager.GPS_PROVIDER);
                newPos.setLatitude(marker.getPosition().latitude);
                newPos.setLongitude(marker.getPosition().longitude);
                handleNewLocation(newPos);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //if user long clicks at even numbers (0, 2, 4, 6, 8 times) then inflate view
                if ((inflateButtons % 2) == 0) {
                    //Toast.makeText(getContext(), "Create Event or Post", Toast.LENGTH_SHORT).show();
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    View view = inflater.inflate(R.layout.post_event_create_shim_layout, null);
                    layoutToAdd.addView(view);
                    giveButtonFunctionality();
                } else {
                    //Else remove the view if odd numbers
                    View namebar = getView().findViewById(R.id.post_event_create);
                    layoutToAdd.removeView(namebar);
                }
                inflateButtons++;
            }
        });




        /*
        //////Save an image of the city using Google's Metadata

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                    Place place = placeLikelihood.getPlace();
                    Log.d("idddd", place.getId());
                }
                placeLikelihoods.release();
            }
        });



        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, "ChIJIQBpAG2ahYAR_6128GcTUEo").setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
            @Override
            public void onResult(final PlacePhotoMetadataResult placePhotoMetadataResult) {
                if (placePhotoMetadataResult.getStatus().isSuccess()) {
                    final PlacePhotoMetadataBuffer photoMetadata = placePhotoMetadataResult.getPhotoMetadata();
                    final PlacePhotoMetadata placePhotoMetadata = photoMetadata.get(0);
                    final String photoDetail = placePhotoMetadata.toString();
                    placePhotoMetadata.getPhoto(mGoogleApiClient).setResultCallback(new ResultCallback<PlacePhotoResult>() {
                        @Override
                        public void onResult(PlacePhotoResult placePhotoResult) {
                            if (placePhotoResult.getStatus().isSuccess()) {
                                Log.d("Photo", "Photo "+photoDetail+" loaded");
                                try {
                                    //converts the bitmap to uri,
                                    filePath = getImageUri(getApplicationContext(), placePhotoResult.getBitmap());
                                    uploadToFirebase(photoDetail);
                                } catch(Exception e){}
                            } else {
                                Log.d("Photo", "Photo "+photoDetail+" failed to load");
                            }
                        }
                    });
                    photoMetadata.release();
                } else {
                    Log.e(TAG, "No photos returned");
                }
            }
        });
*/


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        //if app has permission to use current location,
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //finds the current location
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                //if it cannot, then it requests for the location from client
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {

                //set the map to the current location
                handleNewLocation(location);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        eventQuery.setCenter(new GeoLocation(curLatitude = location.getLatitude(), curLongitude = location.getLongitude()));
        postQuery.setCenter(new GeoLocation(curLatitude, curLongitude));

        LatLng latLng = new LatLng(curLatitude, curLongitude);
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            final List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses.size() > 0 & addresses != null) {

                MenuItem cityMenuItem = topNavigationView.getMenu().findItem(R.id.current_city);
                cityMenuItem.setTitle(addresses.get(0).getLocality());
                final DatabaseReference cityRef = FirebaseDatabase.getInstance().getReference().child("City").child(addresses.get(0).getLocality());
                cityRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) { //if city is already in the database
                            //TODO: If you can think of a better way to do city pictures, then implement it
                            if ((dataSnapshot.child("CityName").getValue()).equals("Davis"))
                                cityRef.child("Picture").setValue("https://firebasestorage.googleapis.com/v0/b/righthererightnow-72e20.appspot.com/o/davis.jpg?alt=media&token=9a201385-b9e7-400c-9e63-dee572aebce3");
                            if ((dataSnapshot.child("CityName").getValue()).equals("Sacramento"))
                                cityRef.child("Picture").setValue("https://firebasestorage.googleapis.com/v0/b/righthererightnow-72e20.appspot.com/o/sacramento.jpg?alt=media&token=1beabb71-309a-4661-8456-73403c27c933");
                            if ((dataSnapshot.child("CityName").getValue()).equals("Galt"))
                                cityRef.child("Picture").setValue("https://firebasestorage.googleapis.com/v0/b/righthererightnow-72e20.appspot.com/o/galt.jpg?alt=media&token=967f71cf-8a6c-4025-b9bd-62ac03f798ec");
                            if ((dataSnapshot.child("CityName").getValue()).equals("Dixon"))
                                cityRef.child("Picture").setValue("https://firebasestorage.googleapis.com/v0/b/righthererightnow-72e20.appspot.com/o/dixon.jpg?alt=media&token=b4d67a69-4016-405c-9a91-1c8d94195440");
                            if ((dataSnapshot.child("CityName").getValue()).equals("Vacaville"))
                                cityRef.child("Picture").setValue("https://firebasestorage.googleapis.com/v0/b/righthererightnow-72e20.appspot.com/o/vacaville.jpg?alt=media&token=89ec6f85-edb5-4428-8384-ceb554e14113");
                            if ((dataSnapshot.child("CityName").getValue()).equals("Winters"))
                                cityRef.child("Picture").setValue("https://firebasestorage.googleapis.com/v0/b/righthererightnow-72e20.appspot.com/o/winters.png?alt=media&token=7c1b633b-c1e3-4df3-bd36-9a9d082c1841");
                            if ((dataSnapshot.child("CityName").getValue()).equals("San Francisco"))
                                cityRef.child("Picture").setValue("https://firebasestorage.googleapis.com/v0/b/righthererightnow-72e20.appspot.com/o/sanfrancisco.jpg?alt=media&token=6cdb2008-b37a-40ec-b8c2-92780aa08ebb");

                        } else {
                            //city does not exist, so create new
                            try { //Sometimes, the city doesnt exist on google maps, so try.
                                City city = new City(addresses.get(0).getLocality(),
                                        addresses.get(0).getAdminArea(),
                                        addresses.get(0).getCountryName(), " ", "0");
                                cityRef.setValue(city);
                            } catch (Exception e) {
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("My Location");
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.exc));
        ourLoc = mMap.addMarker(options);
        if (first) {
            first = false;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }

        final Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(curLatitude, curLongitude))
                .strokeColor(Color.CYAN)
                .radius(radius));
        removeCircle = circle;

        ValueAnimator vAnimator = new ValueAnimator();
        temp = vAnimator;
        vAnimator.setRepeatCount(ValueAnimator.INFINITE);
        vAnimator.setRepeatMode(ValueAnimator.RESTART);
        //TODO: Implement radius change where user wants to change radius.
        //radius can be changed.
        vAnimator.setIntValues(0, radius);
        //This sets how long you want the duration of animation to be.
        vAnimator.setDuration(4000);
        vAnimator.setEvaluator(new IntEvaluator());
        vAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                // Log.e("", "" + animatedFraction);
                circle.setRadius(animatedFraction * radius);
            }
        });

        vAnimator.start();

    }


    public void drawPointsWithinUserRadius() {
        eventsOnMap = FirebaseDatabase.getInstance().getReference("EventLocations");
        GeoFire eventFire = new GeoFire(eventsOnMap);

        postsOnMap = FirebaseDatabase.getInstance().getReference("PostLocations");
        GeoFire postFire = new GeoFire(postsOnMap);

        eventQuery = eventFire.queryAtLocation(new GeoLocation(curLatitude, curLongitude), radius / 1000); // 12800.0);
        //(radius * 0.001) * kmToMiles * 70);
        postQuery = postFire.queryAtLocation(eventQuery.getCenter(), radius / 1000);//12800.0);
        // (radius * 0.001) * kmToMiles * 70);

        // might be something to do with initialization

        Log.d("CENTER", Double.toString(eventQuery.getCenter().latitude));

        eventQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String s, GeoLocation l) {

                LatLng location = new LatLng(l.latitude, l.longitude);

                Marker m = mMap.addMarker(new MarkerOptions()
                        .position(location).draggable(false)
                        //TODO: MM: Change marker size with our algorithm -> query likes and multiply
                        //.icon(BitmapDescriptorFactory.fromBitmap(Marker)));
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.exclamation_point)));
                eventMarkerKeys.put(m, s);
                eventKeyMarkers.put(s, m);

                //if(listview == 1)
                //  storeEventToList(s);
            }  // have discovered an event, so put it in hashmap and put a marker for it

            @Override
            public void onKeyMoved(String s, GeoLocation l) {
                Marker toRemove = eventKeyMarkers.get(s);
                eventMarkerKeys.remove(eventKeyMarkers.remove(s));
                toRemove.remove();

                LatLng location = new LatLng(l.latitude, l.longitude);
                Marker m = mMap.addMarker(new MarkerOptions().position(location).draggable(false));

                eventMarkerKeys.put(m, s);
                eventKeyMarkers.put(s, m);
            }  // event has been moved, so move marker

            @Override
            public void onKeyExited(String s) {
                Marker m = eventKeyMarkers.get(s);
                eventMarkerKeys.remove(eventKeyMarkers.remove(s));
                m.remove();
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
                Marker m = mMap.addMarker(new MarkerOptions().position(location).draggable(false)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.exclamation_blue)));
                postMarkerKeys.put(m, s);
                postKeyMarkers.put(s, m);
            }

            @Override
            public void onKeyMoved(String s, GeoLocation l) {
                Marker toRemove = postKeyMarkers.get(s);
                postMarkerKeys.remove(postKeyMarkers.remove(s));
                toRemove.remove();

                LatLng location = new LatLng(l.latitude, l.longitude);
                Marker m = mMap.addMarker(new MarkerOptions().position(location).draggable(false));

                postMarkerKeys.put(m, s);
                postKeyMarkers.put(s, m);
            }

            @Override
            public void onKeyExited(String s) {
                Marker m = postKeyMarkers.get(s);
                postMarkerKeys.remove(postKeyMarkers.remove(s));
                m.remove();
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


//    public void storeEventToList(String eventKey)
//    {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Event");
//        ref.orderByChild("eventID").equalTo(eventKey).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(!dataSnapshot.exists()) return;
//                else{
//                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
//                    eventArrayList.add(dataSnapshot1.getValue(Event.class));
//                    Log.d("EVENTTT", eventArrayList.get(0).eventID);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


    public void getCurrentUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userKey = user.getUid();
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("User");
        rootRef.child(userKey).child("UsersMessaged").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> keys = new ArrayList<String>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String other = userSnapshot.getKey();
                    keys.add(other);
                }

                Bundle extra = new Bundle();
                extra.putSerializable("objects", keys);

                Intent intent = new Intent(getApplicationContext(), MessageListActivity.class);
                intent.putExtra("extra", extra);
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public Bitmap drawMarkerWithSize(int width, int height) {
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources()
                .getDrawable(R.drawable.exclamation_point, null);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap marker = Bitmap.createScaledBitmap(b, width, height, false);
        return marker;
    }

    public void promptFavorite() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
        dlgAlert.setTitle("Would you like to save this location?");
        dlgAlert.setMessage("You can come back to this location later.");

        dlgAlert.setNegativeButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Opens the gallery of the phone if user clicked "Upload"
                Toast.makeText(getApplicationContext(), "Location Saved!", Toast.LENGTH_LONG).show();
            }
        });

        //if user cancels
        dlgAlert.setPositiveButton("No", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dlgAlert.setCancelable(true);
        dlgAlert.create();
        dlgAlert.show();
    }

    public void optionsMenu(View r) {
        PopupMenu popup = new PopupMenu(getActivity(), r);
        popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                /*if (i == R.id.action1) {
                    Toast.makeText(getApplicationContext(), "Local Post and Events in a List.", Toast.LENGTH_LONG).show();
                    //listview=1;
                    //drawPointsWithinUserRadius();
                    listView();
                    return true;
                } else */
                if (i == R.id.logout) {
                    logout = 1;
                    // TODO delete token
                    RHRNNotifications.unsubscribeFromMessages();
                    RHRNNotifications.unsubscribeFromFollows();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear all activities above it
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                } else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }

    public void drawWithFilters(final Map<String, Integer> aMap) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Event");
        //iterate through all the keys/flags that are on filtering
        for (final String key : aMap.keySet()) {
            int val = aMap.get(key);
            if (val == 1) {
                Log.d("KEY", key);
                eventRef.orderByChild(key).equalTo(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Log.d("keyevent", childSnapshot.getKey());
                            mMap.clear();
                            Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            handleNewLocation(loc);
                            queryWithFilter(key, childSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    public void queryWithFilter(String filter, String filterKey) {
        DatabaseReference filterEvent;
        GeoFire eventFire;
        //Filter by event
        if (filter.equals("isSports"))
            filterEvent = FirebaseDatabase.getInstance().getReference("SportEventLocations");
        else if (filter.equals("isEducation"))
            filterEvent = FirebaseDatabase.getInstance().getReference("EducationEventLocations");
        else if (filter.equals("isClubEvent"))
            filterEvent = FirebaseDatabase.getInstance().getReference("ClubEventLocations");
        else if (filter.equals("isOther"))
            filterEvent = FirebaseDatabase.getInstance().getReference("OtherEventLocations");
        else if (filter.equals("isParty"))
            filterEvent = FirebaseDatabase.getInstance().getReference("PartyEventLocations");
        else
            filterEvent = FirebaseDatabase.getInstance().getReference("EventLocations");
        eventFire = new GeoFire(filterEvent);

        DatabaseReference filterPost = FirebaseDatabase.getInstance().getReference("SportPostLocations");
        GeoFire postFire = new GeoFire(filterPost);

        GeoQuery filterEventQuery = eventFire.queryAtLocation(new GeoLocation(curLatitude, curLongitude), radius / 1000); // 12800.0);
        //(radius * 0.001) * kmToMiles * 70);
        GeoQuery filterPostQuery = postFire.queryAtLocation(filterEventQuery.getCenter(), radius / 1000);//12800.0);
        // (radius * 0.001) * kmToMiles * 70);

        Log.d("CENTER", Double.toString(filterEventQuery.getCenter().latitude));

        filterEventQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String s, GeoLocation l) {
                LatLng location = new LatLng(l.latitude, l.longitude);
                Marker m = mMap.addMarker(new MarkerOptions()
                        .position(location).draggable(false)
                        //.icon(BitmapDescriptorFactory.fromBitmap(Marker)));
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.exclamation_point)));
                eventMarkerKeys.put(m, s);
                eventKeyMarkers.put(s, m);
            }  // have discovered an event, so put it in hashmap and put a marker for it

            @Override
            public void onKeyMoved(String s, GeoLocation l) {
                Marker toRemove = eventKeyMarkers.get(s);
                eventMarkerKeys.remove(eventKeyMarkers.remove(s));
                toRemove.remove();

                LatLng location = new LatLng(l.latitude, l.longitude);
                Marker m = mMap.addMarker(new MarkerOptions().position(location).draggable(false));

                eventMarkerKeys.put(m, s);
                eventKeyMarkers.put(s, m);
            }  // event has been moved, so move marker

            @Override
            public void onKeyExited(String s) {
                Marker m = eventKeyMarkers.get(s);
                eventMarkerKeys.remove(eventKeyMarkers.remove(s));
                m.remove();
            }  // event no longer in range, so remove it from hashmaps and map

            @Override
            public void onGeoQueryError(DatabaseError e) {
                Log.d("ERROR", "GEOQUERY EVENT ERROR");
            }

            @Override
            public void onGeoQueryReady() {

            }
        });

        filterPostQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String s, GeoLocation l) {
                LatLng location = new LatLng(l.latitude, l.longitude);
                Marker m = mMap.addMarker(new MarkerOptions().position(location).draggable(false)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.exclamation_blue)));
                postMarkerKeys.put(m, s);
                postKeyMarkers.put(s, m);
            }

            @Override
            public void onKeyMoved(String s, GeoLocation l) {
                Marker toRemove = postKeyMarkers.get(s);
                postMarkerKeys.remove(postKeyMarkers.remove(s));
                toRemove.remove();

                LatLng location = new LatLng(l.latitude, l.longitude);
                Marker m = mMap.addMarker(new MarkerOptions().position(location).draggable(false));

                postMarkerKeys.put(m, s);
                postKeyMarkers.put(s, m);
            }

            @Override
            public void onKeyExited(String s) {
                Marker m = postKeyMarkers.get(s);
                postMarkerKeys.remove(postKeyMarkers.remove(s));
                m.remove();
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


    public void filterMenu(View r) {
        PopupMenu popup = new PopupMenu(getActivity(), r);
        popup.getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.filter1) {
                    checkboxFilter(item);
                    isEducation = 1;
                    return false;
                } else if (i == R.id.filter2) {
                    checkboxFilter(item);
                    isSports = 1;
                    return false;
                } else if (i == R.id.filter3) {
                    checkboxFilter(item);
                    isParty = 1;
                    return false;
                } else if (i == R.id.filter4) {
                    checkboxFilter(item);
                    isClubEvent = 1;
                    return false;
                } else if (i == R.id.filter5) {
                    checkboxFilter(item);
                    isOther = 1;
                    return false;
                } else if (i == R.id.done_filter) {
                    map = new HashMap<String, Integer>();
                    map.put("isEducation", isEducation);
                    map.put("isSports", isSports);
                    map.put("isParty", isParty);
                    map.put("isClubEvent", isClubEvent);
                    map.put("isOther", isOther);
                    clearFilterFlags();
                    drawWithFilters(map);

                    return true;
                } else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }

    public void clearFilterFlags() {
        isEducation = isClubEvent = isOther = isParty = isSports = 0;
    }

    public void checkboxFilter(MenuItem item) {
        item.setChecked(!item.isChecked());
        SharedPreferences settings = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("checkbox", item.isChecked());
        editor.commit();
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        item.setActionView(new View(getApplicationContext()));
    }

    //stackoverflow function
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public void giveButtonFunctionality() {
        Button createPost, createEvent;
        createPost = (Button) getView().findViewById(R.id.create_post_button);
        createEvent = (Button) getView().findViewById(R.id.create_event_button);

        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager manager = getActivity().getSupportFragmentManager();
//                if (manager.findFragmentById(R.id.post_event_create_shim_fragment_container) != null)
//                    manager.beginTransaction()
//                            .replace(R.id.post_event_create_shim_fragment_container, new CreatePostFragment())
//                            .addToBackStack(null).commit();
//
//                else
                    manager.beginTransaction()
                            .add(R.id.post_event_create_shim_fragment_container, new CreatePostFragment())
                            .addToBackStack(null).commit();


                ImageButton back = new ImageButton(getContext());
                back.setLayoutParams(new AppBarLayout.LayoutParams(AppBarLayout.LayoutParams.WRAP_CONTENT, AppBarLayout.LayoutParams.WRAP_CONTENT));
                back.setImageResource(R.drawable.ic_arrow_back_black_24dp);
                back.setBackgroundColor(Color.TRANSPARENT);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //if back button clicked, pop the back fragment
                        manager.popBackStack();
                        //delete the created view on the bottom
                        View namebar = getView().findViewById(R.id.post_event_create);
                        layoutToAdd.removeView(namebar);
                        inflateButtons++;
                    }
                });
                //add the back button to the layout
                FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.post_event_create_shim_fragment_container);
                frameLayout.addView(back);

            }
        });
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager manager = getActivity().getSupportFragmentManager();
//                if (manager.findFragmentById(R.id.post_event_create_shim_fragment_container) != null)
//                    manager.beginTransaction()
//                            .replace(R.id.post_event_create_shim_fragment_container, new CreateEventFragment())
//                            .addToBackStack(null)
//                            .commit();
//                else
                    manager.beginTransaction()
                            .add(R.id.post_event_create_shim_fragment_container, new CreateEventFragment())
                            .addToBackStack(null)
                            .commit();

                ImageButton backButton = new ImageButton(getActivity());
                backButton.setLayoutParams(new AppBarLayout.LayoutParams(AppBarLayout.LayoutParams.WRAP_CONTENT, AppBarLayout.LayoutParams.WRAP_CONTENT));
                backButton.setImageResource(R.drawable.ic_arrow_back_black_24dp);
                backButton.setBackgroundColor(Color.TRANSPARENT);
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //if back button clicked, pop the back fragment
                        manager.popBackStack();
                        //delete the created view on the bottom
                        View namebar = getView().findViewById(R.id.post_event_create);
                        layoutToAdd.removeView(namebar);
                        inflateButtons++;
                    }
                });
                //add the back button to the layout
                FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.post_event_create_shim_fragment_container);
                frameLayout.addView(backButton);
            }
        });
    }

    public void listView() {
        final FragmentManager manager = getActivity().getSupportFragmentManager();
        if (manager.findFragmentById(R.id.map_as_list) != null)
            manager.beginTransaction()
                    .replace(R.id.map_as_list, new MapListFragment())
                    .addToBackStack(null)
                    .commit();
        else
            manager.beginTransaction()
                    .add(R.id.map_as_list, new MapListFragment())
                    .addToBackStack(null)
                    .commit();
    }


    public void uploadToFirebase(String photoDetail) {
        //create the profile picture name using their uid + .jpg
        String childFile = photoDetail + ".jpg";

        //If the file was chosen from gallery then != null
        if (filePath != null) {
            //Create child using the above string
            StorageReference fileRef = storageRef.child(childFile);
            //Create the upload using built-in UploadTask
            UploadTask uploadTask = fileRef.putFile(filePath);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(getApplicationContext(), "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    //Set the download URL
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //Store URL under the current user
                    FirebaseDatabase.getInstance().getReference().child("City")
                            .child("Davis").child("Picture").setValue(downloadUrl.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //failed to upload

                }
            });
        } else {
            //no image to upload
        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        if (path != null) return Uri.parse(path);
        else return null;
    }

}
