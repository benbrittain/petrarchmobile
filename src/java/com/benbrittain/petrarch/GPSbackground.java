package com.benbrittain.petrarch;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.lang.RT;

import com.benbrittain.petrarch.R;

public class GPSbackground extends Service {

    private LocationManager locationManager;
    private String provider;
    LocationListener mlocListener;
    private String FILENAME = "gpsdata.csv";

    FileOutputStream fos;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = LocationManager.GPS_PROVIDER;
        try {
            fos = openFileOutput(FILENAME, Context.MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mlocListener = new MyLocationListener();
        locationManager.requestLocationUpdates(provider, 10, 0, mlocListener);
    }

    @Override
    public void onDestroy() {
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        locationManager.removeUpdates(mlocListener);
        super.onDestroy();
    }


    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            String time = Long.toString(loc.getTime());
            String longitude = 	Double.toString(loc.getLongitude());
            String latitude  = Double.toString(loc.getLatitude());
            String altitude  = Double.toString(loc.getAltitude());
            String accuracy  = Float.toString(loc.getAccuracy());
            String speed     = Float.toString(loc.getSpeed());
            String data      = time + ", " +
                               longitude + ", " +
                               latitude  + ", " +
                               altitude  + ", " +
                               accuracy  + ", " +
                               speed     + "\n" ;					;
            try {
                fos.write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) { }
        @Override
        public void onProviderDisabled(String provider) { }

    }
}
