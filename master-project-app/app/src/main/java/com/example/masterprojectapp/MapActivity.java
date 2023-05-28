package com.example.masterprojectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.mapsforge.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    private MapView map;
    Marker userGpsPosition;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        map = findViewById(R.id.wc_map);
        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        userGpsPosition = new Marker(map);

        getLastLocation();
        getUserLocation();

        // [END onCreate]
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, "Se ha encontrado la ultima localizacion");

                    GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    map.getController().setZoom(15.0);
                    map.getController().setCenter(startPoint);

                    userGpsPosition.setPosition(startPoint);
                    userGpsPosition.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(userGpsPosition);
                } else {
                    Log.e(TAG, "fusedLocationClient:getLastLocation:onSuccess: No se ha encontrado la localizacion");

                    GeoPoint startPoint = new GeoPoint(40.416775, -3.703790);
                    map.getController().setZoom(15.0);
                    map.getController().setCenter(startPoint);

                    userGpsPosition.setPosition(startPoint);
                    userGpsPosition.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(userGpsPosition);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "fusedLocationClient:getLastLocation:onFailre: " + e.getMessage());
            }
        });

        // [END getLastLocation]
    }

    private void getUserLocation() {
        Log.i(TAG, "Se va a recuperar la posicion del usuario");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.i(TAG, "Se ha recupera la latitud: " + location.getLatitude() + " y la longitud: " + location.getLongitude());

                GeoPoint actualPosition = new GeoPoint(location.getLatitude(), location.getLongitude());
                map.getController().setCenter(actualPosition);

                userGpsPosition.setPosition(actualPosition);
                userGpsPosition.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                map.getOverlays().add(userGpsPosition);
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Se pide actualizar la posicion GPS");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // [END gerUserLocation]
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String [] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "El usuario ha dado acceso a la ubicacion");
            } else {
                Log.e(TAG, "El usuario no ha dado acceso a la ubicacion");
                Toast.makeText(MapActivity.this, R.string.require_gps_position, Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}