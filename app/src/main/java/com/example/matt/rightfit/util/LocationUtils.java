package com.example.matt.rightfit.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.List;

/**
 * Created by Bradley Wang on 4/12/2017.
 */

public class LocationUtils {
    public static Location getBestAvailableLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        Location bestAvailable = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) continue;
            if (bestAvailable == null || l.getAccuracy() < bestAvailable.getAccuracy())
                bestAvailable = l;
        }

        return bestAvailable == null ? new Location("dummy") : bestAvailable;
    }

}
