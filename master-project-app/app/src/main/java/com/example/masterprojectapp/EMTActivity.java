package com.example.masterprojectapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EMTActivity extends AppCompatActivity implements EMTApiHandler.OnAccessTokenListener {
    private static final String TAG = "EMTActivity";
    private int ACTIVITY_CODE;

    private TextView routeTitle;
    private MapView map;
    private Marker userPosition;
    Drawable resizedIcon;

    private EMTApiHandler apiHandler;

    private Double restroom_x_post;
    private Double restroom_y_post;
    private Double user_x_post;
    private Double user_y_post;

    private int hour;
    private int minutes;
    private int day;
    private int month;
    private int year;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emt);

        Context context = getApplicationContext();
        ACTIVITY_CODE = Integer.parseInt(context.getString(R.string.emt_code));

        Intent intent = getIntent();
        if (intent != null) {
            restroom_x_post = intent.getDoubleExtra("restroom_x_post", 0);
            restroom_y_post = intent.getDoubleExtra("restroom_y_post", 0);
            user_x_post = intent.getDoubleExtra("user_x_post", 0);
            user_y_post = intent.getDoubleExtra("user_y_post", 0);
        }

        routeTitle = findViewById(R.id.route_title);
        routeTitle.setText("PEPE");

        map = findViewById(R.id.emt_map);
        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        getInitialUserPosition();

        Drawable customIcon = ContextCompat.getDrawable(this, R.drawable.ic_ubi_emt);
        Bitmap bitmap = ((BitmapDrawable) customIcon).getBitmap();
        int desiredWidth = 35;
        int desiredHeight = 35;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true);
        resizedIcon = new BitmapDrawable(getResources(), resizedBitmap);

        apiHandler = new EMTApiHandler();
        apiHandler.getAccessToken(this, context);
    }

    private void getInitialUserPosition() {
        Log.i(TAG, "Centrar ubicación");
        userPosition = new Marker(map);

        GeoPoint startPoint = new GeoPoint(user_y_post, user_x_post);
        map.getController().setZoom(18.0);
        map.getController().setCenter(startPoint);

        userPosition.setPosition(startPoint);
        userPosition.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(userPosition);
    }

    @Override
    public void onAccessTokenReceived(String accessToken) {
        getDate();

        String path = "transport/busemtmad/travelplan/";
        JsonObject params = new JsonObject();
        params.addProperty("routeType", "P");
        params.addProperty("coordinateXFrom", user_x_post);
        params.addProperty("coordinateYFrom", user_y_post);
        params.addProperty("coordinateXTo", restroom_x_post);
        params.addProperty("coordinateYTo", restroom_y_post);
        params.addProperty("day", day);
        params.addProperty("month", month);
        params.addProperty("year", year);
        params.addProperty("hour", hour);
        params.addProperty("minute", minutes);
        params.addProperty("culture", "es");
        params.addProperty("allowBus", true);
        params.addProperty("allowBike", false);

        String responseBody = apiHandler.makeApiRequest(this, path, accessToken, params);
        drawRoute(responseBody);
    }

    @Override
    public void onAccessTokenError() {
        Log.e(TAG, "Ha habido un error al obtener el token de acceso");
        goBackToPreviousActivity(RESULT_CANCELED);
    }

    private void getDate() {
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);
    }


    private void drawRoute(String responseBody) {
        List<GeoPoint> points = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(responseBody);

            JSONObject dataObject = jsonObject.getJSONObject("data");

            JSONArray sectionsArray = dataObject.getJSONArray("sections");
            JSONObject sectionsObject = sectionsArray.getJSONObject(0);

            JSONObject routeObject = sectionsObject.getJSONObject("route");

            JSONArray featuresArray = routeObject.getJSONArray("features");

            for (int i = 0; i < featuresArray.length(); i++) {
                JSONObject featureObject = featuresArray.getJSONObject(i);
                Log.i(TAG, "featureObject: " + featureObject.toString());

                JSONObject propertiesObject =  featureObject.getJSONObject("properties");
                String description = propertiesObject.getString("description");

                JSONObject geometryObject = featureObject.getJSONObject("geometry");
                String type = geometryObject.getString("type");

                if(type.equals("Point")) {
                    JSONArray coordinatesArray = geometryObject.getJSONArray("coordinates");

                    double longitude = coordinatesArray.getDouble(0);
                    double latitude = coordinatesArray.getDouble(1);

                    Marker marker = new Marker(map);
                    GeoPoint position = new GeoPoint(latitude, longitude);
                    marker.setPosition(position);
                    marker.setIcon(resizedIcon);
                    marker.setSnippet(description);

                    map.getOverlays().add(marker);

                    points.add(position);

                    Log.i(TAG, "latitude: " + latitude + "longitude: " + longitude);
                }
            }
            Polyline polyline = new Polyline();
            polyline.setPoints(points);

            Paint paint = polyline.getOutlinePaint();
            paint.setColor(Color.BLUE);

            map.getOverlayManager().add(polyline);
            map.invalidate();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void goBackToPreviousActivity(int resultStatus) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("activityCode", ACTIVITY_CODE);
        setResult(resultStatus, resultIntent);
        finish();
    }
}