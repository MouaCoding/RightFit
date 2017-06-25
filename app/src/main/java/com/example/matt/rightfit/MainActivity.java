package com.example.matt.rightfit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.matt.rightfit.fragments.CameraFragment;

public class MainActivity extends AppCompatActivity {
    MainPagerAdapter pagerAdapter;
    ViewPager mainViewPager;
    BottomNavigationView bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.main_app_toolbar);
        //toolbar.setLogo(R.drawable.ic_search_black_24dp);
        //setSupportActionBar(toolbar);


        // populate fragments
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        // populate pager with fragments
        mainViewPager = (ViewPager) findViewById(R.id.main_content_view_pager);
        mainViewPager.setOffscreenPageLimit(2);
        mainViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.main_bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem bottom_navigation) {
                        switch (bottom_navigation.getItemId()) {
                            case R.id.map_pin:
                                mainViewPager.setCurrentItem(0, true);
                                break;
                            case R.id.megaphone:
                                mainViewPager.setCurrentItem(1, true);
                                break;
                            case R.id.menu:
                                mainViewPager.setCurrentItem(2, true);
                                break;
                            case R.id.music_social_group:
                                mainViewPager.setCurrentItem(3, true);
                                break;
                            case R.id.identity_card:
                                mainViewPager.setCurrentItem(4, true);
                                break;
                        }
                        return true;
                    }
                });
    }

    private class MainPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_PAGES = 5;

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    //return new MapsFragment();
                    return new CameraFragment();
                case 1:
                    return new TrendingFragment();
                case 2:
                    return new CreatePostOrEventTempShimFragment();
                    //return new MapsFragment();
                case 3:
                    return new NotificationFragment();
                case 4:
                    return new ProfilePageFragment();// return profile page fragment
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
