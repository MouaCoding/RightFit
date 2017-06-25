package com.example.matt.rightfit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.matt.rightfit.firebase_entry.Event;
import com.example.matt.rightfit.firebase_entry.User;
import com.example.matt.rightfit.util.LocationUtils;
import com.firebase.client.ServerValue;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;

public class CreateEventFragment extends Fragment implements OnMapReadyCallback {

    private ImageButton chooseTheme,
            uploadPhoto;
    private ImageView eventImage;
    private TextView uploadPhotoText,
            chooseThemeText;

    private EditText event_name,
            event_description,
            startDate,
            endDate,
            startTime,
            endTime,
            address;

    private TimePickerDialog sTime,
            eTime;

    private DatePickerDialog sDate,
            eDate;


    int currDay, currMonth, currYear, currHour, currMinute;

    public int isEducation = 0, isSports = 0, isParty = 0, isClubEvent = 0, isOther = 0;
    Map<String, Integer> map;

    public Button filterButton;
    GoogleMap mMap;
    LatLng createLoc;

    private MapView event_location;

    //globals for image uploading
    private static final int SELECT_PICTURE = 100;
    //request int for using camera
    private static final int CAPTURE_PICTURE = 200;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://rightfit-5ea19.appspot.com");
    Uri filePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.create_event_page_layout, container, false);

        //filters = new int[5];
        Button b = (Button) r.findViewById(R.id.create_event_confirm);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
            }
        });

        //Initializes each text view to the class's objects
        eventImage = (ImageView) r.findViewById(R.id.event_image);
        event_name = (EditText) r.findViewById(R.id.event_name);
        event_description = (EditText) r.findViewById(R.id.event_description);
        startDate = (EditText) r.findViewById(R.id.editStartDate);
        endDate = (EditText) r.findViewById(R.id.editEndDate);
        startTime = (EditText) r.findViewById(R.id.editStartTime);
        endTime = (EditText) r.findViewById(R.id.editEndTime);
        address = (EditText) r.findViewById(R.id.editAddress);
        chooseThemeText = (TextView) r.findViewById(R.id.event_choose_theme_text);
        uploadPhotoText = (TextView) r.findViewById(R.id.event_upload_image_text);

        Calendar c = Calendar.getInstance();
        currDay = c.get(Calendar.DAY_OF_MONTH);
        currMonth = c.get(Calendar.MONTH);
        currYear = c.get(Calendar.YEAR);
        currHour = c.get(Calendar.HOUR_OF_DAY);
        currMinute = c.get(Calendar.MINUTE);

        final Calendar firstDate = Calendar.getInstance();


        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sTime = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String amPm;
                        String min;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }
                        if (hourOfDay == 0) {
                            hourOfDay = 12;
                        }
                        if (hourOfDay > 12) {
                            hourOfDay = hourOfDay - 12;
                        }
                        if (minute < 10) {
                            min = "0" + minute;
                        } else {
                            min = Integer.toString(minute);
                        }
                        startTime.setText(hourOfDay + ":" + min + amPm);
                    }
                }, currHour, currMinute, DateFormat.is24HourFormat(getActivity()));
                sTime.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eTime = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String amPm;
                        String min;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }
                        if (hourOfDay == 0) {
                            hourOfDay = 12;
                        }
                        if (hourOfDay > 12) {
                            hourOfDay = hourOfDay - 12;
                        }
                        if (minute < 10) {
                            min = "0" + minute;
                        } else {
                            min = Integer.toString(minute);
                        }
                        endTime.setText(hourOfDay + ":" + min + amPm);
                    }
                }, currHour, currMinute, DateFormat.is24HourFormat(getActivity()));
                eTime.show();
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sDate = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        startDate.setText((month + 1) + "/" + dayOfMonth + "/" + year);
                        firstDate.set(year, month, dayOfMonth);
                    }
                }, currYear, currMonth, currDay);
                sDate.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                sDate.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eDate = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        endDate.setText((month + 1) + "/" + dayOfMonth + "/" + year);
                    }
                }, currYear, currMonth, currDay);
                eDate.getDatePicker().setMinDate(firstDate.getTimeInMillis() - 1000);
                eDate.show();
            }
        });

        filterButton = (Button) r.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterMenu(v);
            }
        });

        event_location = (MapView) r.findViewById(R.id.event_location_map_view);
        event_location.onCreate(savedInstanceState);
        event_location.getMapAsync(this);

        Location loc = LocationUtils.getBestAvailableLastKnownLocation(getContext());
        createLoc = new LatLng(loc.getLatitude(), loc.getLongitude());

        uploadPhoto = (ImageButton) r.findViewById(R.id.event_upload_image);
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

        chooseTheme = (ImageButton) r.findViewById(R.id.event_choose_theme);
        chooseTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                eventImage.setImageBitmap(bitmap);
                eventImage.setVisibility(View.VISIBLE);
                uploadPhoto.setVisibility(View.INVISIBLE);
                chooseTheme.setVisibility(View.INVISIBLE);
                uploadPhotoText.setVisibility(View.INVISIBLE);
                chooseThemeText.setVisibility(View.INVISIBLE);
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
            eventImage.setImageBitmap(imageBitmap);
            eventImage.setVisibility(View.VISIBLE);
            uploadPhoto.setVisibility(View.INVISIBLE);
            chooseTheme.setVisibility(View.INVISIBLE);
            uploadPhotoText.setVisibility(View.INVISIBLE);
            chooseThemeText.setVisibility(View.INVISIBLE);
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
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }
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
                .title("Event Location");

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(createLoc, 16));

        mMap.addMarker(x).showInfoWindow();
    }

    @Override
    public void onStart() {
        event_location.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        event_location.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        event_location.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        event_location.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        event_location.onDestroy();
    }

    public void createEvent() {

        String str_event_name = event_name.getText().toString().trim();
        String str_event_description = event_description.getText().toString().trim();
        String str_eventSDate = startDate.getText().toString();
        String str_eventEDate = endDate.getText().toString();
        String str_eventSTime = startTime.getText().toString();
        String str_eventETime = endTime.getText().toString();
        String str_eventAddr = address.getText().toString();

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        try {
            Toast.makeText(getContext(), "Creating Event...", Toast.LENGTH_SHORT).show();
            DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference gettingKey = RootRef.child("Event").push();
            DatabaseReference createdEvent = RootRef.child("Event").child("Event_" + gettingKey.getKey());
            String eventKey = "Event_" + gettingKey.getKey();
            uploadToFirebase(eventKey);
            gettingKey.setValue(null);

            DatabaseReference eventLocation = RootRef.child("EventLocations");
            GeoFire geoFireLocation = new GeoFire(eventLocation);

            createdEvent.setValue(new Event(str_event_name, FirebaseAuth.getInstance().getCurrentUser().getUid(), str_eventSDate,
                    str_eventEDate, str_eventSTime, str_eventETime, str_eventAddr,
                    str_event_description, 10, 0, 0, 0));
            createdEvent.child("timestamp_create").setValue(ServerValue.TIMESTAMP);
            createdEvent.child("eventID").setValue("Event_" + gettingKey.getKey());
            try {
                for (String key : map.keySet()) {
                    createdEvent.child(key).setValue(map.get(key));
                    //IF Statements ONLY because we can have multiple categories of events
                    if (key.equals("isSports") && map.get(key) == 1) {
                        DatabaseReference sport = RootRef.child("SportEventLocations");
                        GeoFire geoFire = new GeoFire(sport);
                        geoFire.setLocation(createdEvent.getKey(), new GeoLocation(createLoc.latitude, createLoc.longitude));
                    }
                    if (key.equals("isEducation") && map.get(key) == 1) {
                        DatabaseReference sport = RootRef.child("EducationEventLocations");
                        GeoFire geoFire = new GeoFire(sport);
                        geoFire.setLocation(createdEvent.getKey(), new GeoLocation(createLoc.latitude, createLoc.longitude));
                    }
                    if (key.equals("isClubEvent") && map.get(key) == 1) {
                        DatabaseReference sport = RootRef.child("ClubEventLocations");
                        GeoFire geoFire = new GeoFire(sport);
                        geoFire.setLocation(createdEvent.getKey(), new GeoLocation(createLoc.latitude, createLoc.longitude));
                    }
                    if (key.equals("isOther") && map.get(key) == 1) {
                        DatabaseReference sport = RootRef.child("OtherEventLocations");
                        GeoFire geoFire = new GeoFire(sport);
                        geoFire.setLocation(createdEvent.getKey(), new GeoLocation(createLoc.latitude, createLoc.longitude));
                    }
                    if (key.equals("isParty") && map.get(key) == 1) {
                        DatabaseReference sport = RootRef.child("PartyEventLocations");
                        GeoFire geoFire = new GeoFire(sport);
                        geoFire.setLocation(createdEvent.getKey(), new GeoLocation(createLoc.latitude, createLoc.longitude));
                    }
                }
            } catch (Exception e) {
            }

            // public Event(String aName, String aOwner, String aStartDate, String aEndDate, String aStartTime,
            //              String aEndTime, String aAddress, String aDescription,
            //              double aViewRadius, int aLikes, int aComments, int aRSVPs)

            geoFireLocation.setLocation(createdEvent.getKey(), new GeoLocation(createLoc.latitude, createLoc.longitude));
            setExtraValues(eventKey, FirebaseAuth.getInstance().getCurrentUser().getUid());

            //Saves the city of created event
            Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocation(createLoc.latitude, createLoc.longitude, 1);

                if (addresses.size() > 0 & addresses != null) {
                    RootRef.child("Event").child("Event_" + gettingKey.getKey()).child("City")
                            .setValue(addresses.get(0).getLocality());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //progressDialog.dismiss();
            Toast.makeText(getContext(), "Event Created!", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
        }

        getActivity().getSupportFragmentManager().popBackStack();
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


                    return true;
                } else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }

    public void checkboxFilter(MenuItem item) {
        item.setChecked(!item.isChecked());
        SharedPreferences settings = getActivity().getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("checkbox", item.isChecked());
        editor.commit();
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        item.setActionView(new View(getContext()));
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
                    FirebaseDatabase.getInstance().getReference().child("Event")
                            .child(eventID).child("ProfilePicture").setValue(downloadUrl.toString());
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
