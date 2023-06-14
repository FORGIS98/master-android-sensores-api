package com.example.masterprojectapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.DashPathEffect;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ZoomButtonsController;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EMTActivity extends AppCompatActivity implements EMTApiHandler.OnAccessTokenListener {
    private static final String TAG = "EMTActivity";
    private int ACTIVITY_CODE;
    private LinearLayout linearLayoutDuration;
    private LinearLayout linearLayout_Way;
    private TextView pointDescription;
    private TextView textView_duration;
    private TextView textView_arrivalTime;
    private ImageButton button_exit;
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

    private String arrivalTime;
    private String duration;
    List<Marker> markers;

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

        pointDescription = findViewById(R.id.description);
        linearLayoutDuration = findViewById(R.id.containerDuration);
        textView_duration = findViewById(R.id.duration);
        textView_arrivalTime = findViewById(R.id.arrivalTime);
        linearLayout_Way = findViewById(R.id.containerWay);
        button_exit = findViewById(R.id.exit);

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

        Toast.makeText(this, "Calculando ruta más óptima...", Toast.LENGTH_LONG).show();
        apiHandler = new EMTApiHandler();
        apiHandler.getAccessToken(this, context);

        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToPreviousActivity(RESULT_OK);
            }
        });
    }

    private void getInitialUserPosition() {
        Log.i(TAG, "Centrar ubicación");
        userPosition = new Marker(map);

        GeoPoint startPoint = new GeoPoint(user_y_post, user_x_post);
        map.getController().setZoom(18.5);
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
        params.addProperty("routeType", "W");
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

        // Modificar los textos con la ruta calculada
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(duration != null && textView_arrivalTime != null && !duration.isEmpty() && !arrivalTime.isEmpty()) {
                    linearLayoutDuration.setVisibility(View.VISIBLE);
                    textView_duration.setText(duration);
                    textView_arrivalTime.setText(arrivalTime);
                }
                if(markers != null && !markers.isEmpty()) {
                    Marker marker = markers.get(0);
                    String description = marker.getSnippet();
                    pointDescription.setText(description);
                    linearLayout_Way.setVisibility(View.VISIBLE);

                    showDescriptionPoint();
                }
            }
        });
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


    @SuppressLint("ResourceAsColor")
    private void drawRoute(String responseBody) {
        List<GeoPoint> points = new ArrayList<>();
        markers = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(responseBody);

            JSONObject dataObject = jsonObject.getJSONObject("data");
            String dataTime = dataObject.getString("arrivalTime");
            arrivalTime = getArrivalTime(dataTime);

            JSONArray sectionsArray = dataObject.getJSONArray("sections");
            JSONObject sectionsObject = sectionsArray.getJSONObject(0);

            String str_duration = sectionsObject.getString("duration");
            double valor = Double.parseDouble(str_duration);
            long horas = (long) valor / 60;
            long minutos = (long) valor % 60;
            duration = horas + "h y " + minutos + "min";

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
                    markers.add(marker);
                    Log.i(TAG, "latitude: " + latitude + "longitude: " + longitude);
                }
            }
            Polyline polyline = new Polyline();
            polyline.setPoints(points);

            Paint paint = polyline.getOutlinePaint();
            paint.setColor(R.color.lavender);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(10f);
            PathEffect pathEffect = new DashPathEffect(new float[]{10, 10}, 0);
            paint.setPathEffect(pathEffect);

            map.getOverlayManager().add(polyline);
            map.invalidate();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showDescriptionPoint() {
        for (final Marker marker : markers) {
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    String description = marker.getSnippet();

                    Log.i(TAG, "description: " + description);
                    pointDescription.setText(description);
                    return true;
                }
            });
        }
    }
    private String getArrivalTime(String dataTime) {
        String time;
        try {
            SimpleDateFormat completedFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = completedFormat.parse(dataTime);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            time = timeFormat.format(date);
        } catch (ParseException e) {
            time = "";
        }
        return time;
    }
    @Override
    public void onBackPressed() {
        Log.i(TAG, "Botón de hacia atrás");
        goBackToPreviousActivity(RESULT_OK);
    }

    private void goBackToPreviousActivity(int resultStatus) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("activityCode", ACTIVITY_CODE);
        setResult(resultStatus, resultIntent);
        finish();
    }
}