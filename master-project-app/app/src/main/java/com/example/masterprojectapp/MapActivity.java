package com.example.masterprojectapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import java.util.List;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";
    private ActivityResultLauncher<Intent> activityLauncher;

    private MapView map;
    private Marker userGpsPosition;

    private LocationManager locationManager;

    private boolean centerUser = true;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        map = findViewById(R.id.wc_map);
        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        userGpsPosition = new Marker(map);

        getMadrid();
        getUserLocation();

        GeoDataManager dataManager = new GeoDataManager();
        List<Marker> markerList = dataManager.parseGeoFile(this.getApplicationContext(), map);

        Drawable customIcon = ContextCompat.getDrawable(this, R.drawable.ic_bathroom_position);
        Bitmap bitmap = ((BitmapDrawable) customIcon).getBitmap();
        int desiredWidth = 35;
        int desiredHeight = 35;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true);
        Drawable resizedIcon = new BitmapDrawable(getResources(), resizedBitmap);

        for (Marker marker : markerList) {
            marker.setIcon(resizedIcon);
            map.getOverlays().add(marker);
        }

        for (final Marker marker : markerList) {
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    String title = marker.getTitle();
                    GeoPoint position = marker.getPosition();
                    Log.i(TAG, "Marker clickeado: Title: " + title + ", Position: " + position.toString());
                    Double restroom_x_post = position.getLongitude();
                    Double restroom_y_post = position.getLatitude();

                    GeoPoint userPosition = userGpsPosition.getPosition();
                    Double user_x_post = userPosition.getLongitude();
                    Double user_y_post = userPosition.getLatitude();

                    startBathroomActivity(title, restroom_x_post, restroom_y_post, user_x_post, user_y_post);
                    return true;
                }
            });
        }

        activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.i(TAG, "BathroomActivity Terminada Correctamente");

                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Log.i(TAG, "BathroomActivity Terminada Mal");
                }
            }
        );
        // [END onCreate]
    }

    private void startBathroomActivity(String title, Double restroom_x_post, Double restroom_y_post, Double user_x_post, Double user_y_post) {
        Intent intent = new Intent(MapActivity.this, BathroomActivity.class);
        intent.putExtra("activity_code", title);
        intent.putExtra("title", title);
        intent.putExtra("restroom_x_post", restroom_x_post);
        intent.putExtra("restroom_y_post", restroom_y_post);
        intent.putExtra("user_x_post", user_x_post);
        intent.putExtra("user_y_post", user_y_post);
        activityLauncher.launch(intent);
    }

    private void getMadrid() {
        Log.i(TAG, "Centrar ubicación en Madrid");
        GeoPoint startPoint = new GeoPoint(40.416775, -3.703790);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        userGpsPosition.setPosition(startPoint);
        userGpsPosition.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(userGpsPosition);

        Toast.makeText(this, R.string.getting_position, Toast.LENGTH_LONG).show();

        // [END getMadrid]
    }

    private void getUserLocation() {
        Log.i(TAG, "Se va a recuperar la posicion del usuario");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "Se ha recuperado la latitud: " + location.getLatitude() + " y la longitud: " + location.getLongitude());

                GeoPoint actualPosition = new GeoPoint(location.getLatitude(), location.getLongitude());

                if (centerUser) {
                    map.getController().setZoom(16.0);
                    map.getController().setCenter(actualPosition);
                    centerUser = !centerUser;
                }

                userGpsPosition.setPosition(actualPosition);
                userGpsPosition.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                map.getOverlays().add(userGpsPosition);
                map.invalidate(); // Actualizar el mapa
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "El usuario ha dado permisos de geolocalizacion");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
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