package com.example.matt.rightfit;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.Post;
import com.example.matt.rightfit.firebase_entry.User;
import com.example.matt.rightfit.util.LocationUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static com.facebook.FacebookSdk.getApplicationContext;

public class CreatePostFragment extends Fragment implements OnMapReadyCallback {
    private MapView post_location;
    private GoogleMap mMap;
    private EditText post_content;
    private CheckBox anon;
    private LatLng createLoc;

    private ImageView postImage;
    private ImageButton uploadPhoto;
    private TextView uploadPhotoText;
    //globals for image uploading
    private static final int SELECT_PICTURE = 100;
    //request int for using camera
    private static final int CAPTURE_PICTURE = 200;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://rightfit-5ea19.appspot.com");
    Uri filePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.create_post_page_layout, container, false);
        Button b = (Button) r.findViewById(R.id.confirm_post_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPost();
            }
        });

        //Initializes each text view to the class's objects
        post_content = (EditText) r.findViewById(R.id.content_post);
        post_location = (MapView) r.findViewById(R.id.post_location_map_view);
        uploadPhotoText = (TextView) r.findViewById(R.id.post_upload_image_text);
        postImage = (ImageView) r.findViewById(R.id.post_image);
        post_location.getMapAsync(this);
        post_location.onCreate(savedInstanceState);


        Location loc = LocationUtils.getBestAvailableLastKnownLocation(getContext());
        createLoc = new LatLng(loc.getLatitude(), loc.getLongitude());

        uploadPhoto = (ImageButton) r.findViewById(R.id.post_upload_image);
        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
                dlgAlert.setMessage("Capture New Image or Upload an Image");
                dlgAlert.setTitle("Give your event an image!");

                //If user wants to upload from gallery
                dlgAlert.setNegativeButton("Upload", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Opens the gallery of the phone if user clicked "Upload"
                        Toast.makeText(getApplicationContext(), "Upload a Picture", Toast.LENGTH_LONG).show();
                        Intent i = new Intent();
                        i.setType("image/*");
                        i.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
                    }
                });
                //if user decides to take a new picture
                dlgAlert.setNeutralButton("Capture", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Capture", Toast.LENGTH_LONG).show();
                        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(imageIntent, CAPTURE_PICTURE);
                    }
                });
                //if user cancels
                dlgAlert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });

                dlgAlert.setCancelable(true);
                dlgAlert.create();
                dlgAlert.show();

            }
        });

        return r;
    }

    //Handles finished activity when a user has clicked an image from gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // When an Image is picked, the image is called back
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK
                && null != data) {
            // Get the Image from data
            filePath = data.getData();
            //Upload to firebase
            //uploadToFirebase();
            //Then set the photo from the chosen gallery
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(getApplicationContext().getContentResolver(), filePath);
                //Picasso.with(getContext()).load(filePath).into(profilePicture);
                postImage.setImageBitmap(bitmap);
                postImage.setVisibility(View.VISIBLE);
                uploadPhoto.setVisibility(GONE);
                uploadPhotoText.setVisibility(GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Else user did not pick an image
        } else if (requestCode == CAPTURE_PICTURE && resultCode == RESULT_OK && data != null) {
            // Get the Image from data
            filePath = data.getData();
            //Upload to firebase
            //uploadToFirebase();
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            postImage.setImageBitmap(imageBitmap);
            postImage.setVisibility(View.VISIBLE);
            uploadPhoto.setVisibility(View.INVISIBLE);
            uploadPhotoText.setVisibility(View.INVISIBLE);
            //Else user did not pick an image
        } else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragEnd(Marker marker) {
                createLoc = marker.getPosition();
            }

            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {}
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
        MarkerOptions x = new MarkerOptions()
                .position(createLoc)
                .draggable(true)
                .title("Post Location");

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(createLoc, 16));
        mMap.addMarker(x).showInfoWindow();
    }

    @Override
    public void onStart() {
        post_location.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        post_location.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        post_location.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        post_location.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        post_location.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        post_location.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        post_location.onLowMemory();
    }

    public void createPost() {
        String postContent = post_content.getText().toString().trim();

        Calendar c = Calendar.getInstance();
        int Year = c.get(Calendar.YEAR);
        int Month = c.get(Calendar.MONTH) + 1; //Calendar starts at 0 I DONT KNOW WHY......
        int Day = c.get(Calendar.DAY_OF_MONTH);
        int Minute = c.get(Calendar.MINUTE);
        int Hour = c.get(Calendar.HOUR_OF_DAY);
        int Second = c.get(Calendar.SECOND);
        String time = "";
        String timeAndDate = String.format("%04d%02d%02d%02d%02d%02d", Year, Month, Day, Hour, Minute, Second);

        if (Hour >= 12) {
            if (Hour == 12) {
                time = Integer.toString(Hour) + ":" + Integer.toString(Minute) + "PM";
            } else {
                time = Integer.toString((Hour - 12)) + ":" + Integer.toString(Minute) + "PM";
            }
        } else {
            time = Integer.toString(Hour) + ":" + Integer.toString(Minute) + "AM";
        }

        try {
            Toast.makeText(getContext(), "Creating Post...", Toast.LENGTH_SHORT).show();
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference gettingKey = rootRef.child("Post").push();
            DatabaseReference createdPost = rootRef.child("Post").child("Post_" + gettingKey.getKey());
            uploadToFirebase("Post_" + gettingKey.getKey());
            gettingKey.setValue(null);

            DatabaseReference postLocation = rootRef.child("PostLocations");
            GeoFire geoFireLocation = new GeoFire(postLocation);

            //set date and time to today, right now?
            createdPost.setValue(new Post(FirebaseAuth.getInstance().getCurrentUser().getUid(), createdPost.getKey(), timeAndDate, time,
                    postContent, "response Post ID", 10, 0, 0, 0, false));

            createdPost.child("timestamp_create").setValue(ServerValue.TIMESTAMP);
            setExtraValues(createdPost.getKey(), FirebaseAuth.getInstance().getCurrentUser().getUid());

            geoFireLocation.setLocation(createdPost.getKey(), new GeoLocation(createLoc.latitude, createLoc.longitude));
            //Saves the city of created event
            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocation(createLoc.latitude, createLoc.longitude, 1);

                if (addresses.size() > 0 & addresses != null) {
                    rootRef.child("Post").child("Post_" + gettingKey.getKey()).child("City")
                            .setValue(addresses.get(0).getLocality());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Set displayname, handle, and profile pic for easier listview populating
            setExtraValues(createdPost.getKey(), FirebaseAuth.getInstance().getCurrentUser().getUid());
            Toast.makeText(getContext(), "Post Created!", Toast.LENGTH_SHORT).show();
            //progressDialog.dismiss();
        } catch (SecurityException e) {
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    public void setExtraValues(final String postID, final String ownerID) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("User").child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User owner = dataSnapshot.getValue(User.class);
                ref.child("Post").child(postID).child("DisplayName").setValue(owner.DisplayName);
                ref.child("Post").child(postID).child("handle").setValue(owner.handle);
                try {
                    ref.child("Post").child(postID).child("ProfilePicture").setValue(owner.ProfilePicture);
                } catch (Exception e) {}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    public void uploadToFirebase(final String eventID) {
        //create the profile picture name using their uid + .jpg
        String childFile = eventID + ".jpg";

        //If the file was chosen from gallery then != null
        if (filePath != null) {
            //Create child using the above string
            StorageReference fileRef = storageRef.child(childFile);
            //Create the upload using built-in UploadTask
            UploadTask uploadTask = fileRef.putFile(filePath);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    //Set the download URL
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //Store URL under the event
                    FirebaseDatabase.getInstance().getReference().child("Post")
                            .child(eventID).child("PostPicture").setValue(downloadUrl.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Upload Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Select an image", Toast.LENGTH_SHORT).show();
        }
    }

}
