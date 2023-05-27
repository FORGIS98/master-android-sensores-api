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

import org.osmdroid.config.Configuration;
import org.osmdroid.mapsforge.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    private MapView map;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        map = findViewById(R.id.wc_map);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        map.setBuiltInZoomControls(true);

        GeoPoint startPoint = new GeoPoint(40.416775, -3.703790);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        Marker marker = new Marker(map);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);

        Location location = getUserLocation();

        startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        marker = new Marker(map);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
        // [END onCreate]
    }

    private Location getUserLocation() {
        Log.i(TAG, "Se va a recuperar la posicion del usuario");

        final Location[] userLocation = new Location[1];

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.i(TAG, "Se ha recupera la latitud: " + location.getLatitude() + " y la longitud: " + location.getLongitude());
                userLocation[0] = location;
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 20, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        return userLocation[0];

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