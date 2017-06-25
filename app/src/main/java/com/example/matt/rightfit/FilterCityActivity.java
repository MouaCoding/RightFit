package com.example.matt.rightfit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.matt.rightfit.firebase_entry.City;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FilterCityActivity extends AppCompatActivity {

    public ArrayList<City> cityArray;
    public CityCheckBoxAdapter cityAdapter;
    public ListView cityList;
    public CheckBox checkBox;
    public EditText search;
    public TextWatcher filterCity;
    private ImageButton backButton, menuButton;
    Button finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_city);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cityArray = new ArrayList<>();
        cityList = (ListView) findViewById(R.id.city_list);
        menuButton = (ImageButton) findViewById(R.id.profile_app_bar_options);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu();
            }
        });
        backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish the current activity
                finish();
            }
        });

        filterCity = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {try {cityAdapter.getFilter().filter(s);} catch (Exception e) {}}

            @Override
            public void afterTextChanged(Editable s) {}
        };
        search = (EditText) findViewById(R.id.search_cities);
        search.addTextChangedListener(filterCity);

        finishButton = (Button) findViewById(R.id.finish_city_filter);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                finish();
            }
        });
        queryAllCities();
    }

    public void queryAllCities() {
        final DatabaseReference cityRef = FirebaseDatabase.getInstance().getReference().child("CityFilters");
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("City").orderByChild("CityName").startAt("A").endAt("Z").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {
                for (DataSnapshot dataSnapshot : dataSnapshot1.getChildren()) {
                    City cty = dataSnapshot.getValue(City.class);
                    cityArray.add(cty);
                }
                cityAdapter = new CityCheckBoxAdapter(getBaseContext(), cityArray);
                cityList = (ListView) findViewById(R.id.city_list);
                cityList.setAdapter(cityAdapter);
                cityList.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        checkBox = (CheckBox) view.findViewById(R.id.city_checkbox);
                        checkBox.setChecked(!checkBox.isChecked());
                        if (checkBox.isChecked())
                            cityRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(cityArray.get(position).CityName).setValue(cityArray.get(position));
                        else
                            cityRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(cityArray.get(position).CityName).removeValue();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    public class CityCheckBoxAdapter extends ArrayAdapter<City> {
        private ArrayList<City> mCities;
        private ArrayList<City> mCitiesFilter;
        CityCheckBoxAdapter(Context context, ArrayList<City> cities) {
            super(context, R.layout.filter_city, R.id.city_name, cities);
            mCities = cities;
            mCitiesFilter = cities;
            getFilter();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            final City city = getItem(position);

            TextView cityName = (TextView) convertView.findViewById(R.id.city_name);
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.city_checkbox);
            cityName.setText(city.CityName);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cityName.getLayoutParams();
            cityName.setLayoutParams(layoutParams);
            checkIfCityExists(city.CityName, cb);
            return convertView;
        }

        public void checkIfCityExists(String cityName, final CheckBox cb) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("CityFilters").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            ref.child(cityName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        cb.setChecked(true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }

        @Override
        public int getCount() {
            return mCities.size();
        }

        //Get the data item associated with the specified position in the data set.
        @Override
        public City getItem(int position) {return mCities.get(position);}

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
                        ArrayList<City> filterList = new ArrayList<City>();
                        for (int i = 0; i < mCitiesFilter.size(); i++) {

                            if (mCitiesFilter.get(i).CityName.contains(charSequence)) {
                                filterList.add(mCitiesFilter.get(i));
                            }
                        }
                        results.count = filterList.size();
                        results.values = filterList;
                    } else {
                        results.count = mCitiesFilter.size();
                        results.values = mCitiesFilter;
                    }
                    return results;
                }
                //Invoked in the UI thread to publish the filtering results in the user interface.
                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint,
                                              FilterResults results) {
                    mCities = (ArrayList<City>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }


    private void popupMenu() {
        PopupMenu popup = new PopupMenu(FilterCityActivity.this, menuButton);
        popup.getMenuInflater().inflate(R.menu.other_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear all activities above it
                    startActivity(intent);
                    finish();
                    return true;
                } else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }
}
