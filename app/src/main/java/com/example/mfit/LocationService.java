package com.example.mfit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.models.Token;
import com.example.models.User;
import com.example.models.UserLocation;
import com.example.myplace.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executor;

public class LocationService extends Service {
    public static final int TWO_MINUTES = 10000; // 120 seconds
    public static Boolean isRunning = false;
    public FirebaseAuth auth;
    public LocationManager mLocationManager;
    public LocationUpdaterListener mLocationListener;
    public Location previousBestLocation = null;
    private DatabaseReference database;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationUpdaterListener();

        super.onCreate();
        database= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();

        //Toast.makeText(LocationService.this,"BBBBBBBB", Toast.LENGTH_SHORT).show();
    }

    Handler mHandler = new Handler();
    Runnable mHandlerTask = new Runnable(){
        @Override
        public void run() {
            if (!isRunning) {
                startListening();
            }
            mHandler.postDelayed(mHandlerTask, TWO_MINUTES);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandlerTask.run();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopListening();
        mHandler.removeCallbacks(mHandlerTask);
        super.onDestroy();
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) mLocationListener);

            if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }
        isRunning = true;
    }

    private void stopListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        isRunning = false;
    }

    public class LocationUpdaterListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location) {

                checkTokenNotifications(location.getLatitude(), location.getLongitude());
                checkFriendNotification(location.getLatitude(), location.getLongitude());

            if (isBetterLocation(location, previousBestLocation)) {
                previousBestLocation = location;
                try {
                    if(AllUsersData.getInstance().sendMyLocation == true) {
                        database.child("locations").child(auth.getCurrentUser().getUid()).child("longi").setValue(location.getLongitude());
                        database.child("locations").child(auth.getCurrentUser().getUid()).child("lat").setValue(location.getLatitude());
                    }
                    MyTokensData.getInstance().currentLocation = location;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    stopListening();
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            stopListening();
        }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private double calculateDistanceBetweenTwoGeoPoints(double long1,double lat1,double long2,double lat2){
        double earthRadiusKm = 6371.0;
        double dLat = degreesToRadians(lat2-lat1);
        double dLon = degreesToRadians(long2-long1);
        double latt1 = degreesToRadians(lat1);
        double latt2 = degreesToRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(latt1) * Math.cos(latt2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double res = earthRadiusKm * c;
        return res;
    }

    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }


    private void checkTokenNotifications(double currLat, double currLong){
        for(int i=0; i<MyTokensData.getInstance().myTokens.size(); i++) {
            Token token = MyTokensData.getInstance().myTokens.get(i);
            if (!token.getUserId().equals(auth.getCurrentUser().getUid())) {
                double dist = calculateDistanceBetweenTwoGeoPoints(currLong, currLat, token.longitude, token.latitude);
                dist*=1000;
                if(dist<=1300)
                    sendTokenNotification();
            }
        }
    }

    private void checkFriendNotification(double currLat, double currLong){
        HashMap<String, UserLocation> friendMap = AllUsersData.getInstance().getFriendsLocations();
        Collection<String> keys = friendMap.keySet();
        final Object[] niz = keys.toArray();
        for(int i = 0; i<niz.length;i++) {
            UserLocation userLocation = friendMap.get(niz[i]);
            double dist = calculateDistanceBetweenTwoGeoPoints(currLong, currLat, userLocation.longi, userLocation.lat);
            dist*=1000;
            if(dist<=1300)
                sendFriendNotification();
        }
    }

    private void sendTokenNotification(){
        Intent intent = new Intent(LocationService.this, Map.class);
        intent.putExtra("state", Map.SHOW_MAP);
        PendingIntent pIntent = PendingIntent.getActivity(LocationService.this.getApplicationContext(), 0, intent, 0);

        Notification n  = new Notification.Builder(LocationService.this)
                .setContentTitle("Token is close")
                .setContentText("You might want to collect it and up your score!")
                .setSmallIcon(R.drawable.baseline_add_circle_outline_white_48)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);
    }

    private void sendFriendNotification(){
        Intent intent1 = new Intent(LocationService.this, AddFriend.class);
        PendingIntent pIntent1 = PendingIntent.getActivity(LocationService.this.getApplicationContext(), 0, intent1, 0);

        Notification n1  = new Notification.Builder(LocationService.this)
                .setContentTitle("Friend is close")
                .setContentText("You might want to go to them and say Hi!")
                .setSmallIcon(R.drawable.profile_ico)
                .setContentIntent(pIntent1)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager1 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager1.notify(1, n1);
    }
}
