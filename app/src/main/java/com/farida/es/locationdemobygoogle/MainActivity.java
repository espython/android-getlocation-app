package com.farida.es.locationdemobygoogle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity : ";
    private static final int REQUEST_LOCATION_CODE_PERMISSION = 10;
    Location msLocation;
    FusedLocationProviderClient mFusedLocationClient;
    TextView mLocationText;
    UserLocation userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationText = findViewById(R.id.location_text_view);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
         userLocation = new UserLocation();

        Observable<UserLocation> locationObservable = Observable.just(getLocation());
        locationObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((userLoc)->{
                    Log.d(TAG, "My location lat : " + userLoc.getLat());
                    System.out.printf("This is my lat %s and this is may longitude: %s",userLocation.getLat(),userLocation.getLon());
                    customizeButtonLayout();

        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this, R.string.location_request_denied, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    /***************************
     * Define Custom Methods   *
     * *************************
     */

    private void customizeButtonLayout() {
        Button btn = findViewById(R.id.show_location_btn);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        btn.setLayoutParams(layoutParams);
        btn.setOnClickListener(v -> System.out.printf("RxJava retrive my location is: %f",getLocation().getLat()));
    }

    /* Getting Device Location Method */
    private UserLocation getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "The Location Permission is needed to show the weather  ", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_CODE_PERMISSION);
                }
            }

        } else {
            Log.d(TAG, "getLocation: Permission Granted");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        msLocation = location;
                        userLocation.setLat(location.getLatitude());
                        userLocation.setLon(location.getLongitude());

                        mLocationText.setText(getString(R.string.location_text
                                , msLocation.getLatitude()
                                , msLocation.getLongitude()
                                , msLocation.getTime()));
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        mLocationText.setGravity(Gravity.CENTER_HORIZONTAL);
                        mLocationText.setLayoutParams(params);
                        mLocationText.setTextSize(24);

                    } else {
                        mLocationText.setText(R.string.no_location);
                    }
                }
            });

        }
        return userLocation;
    }
}
