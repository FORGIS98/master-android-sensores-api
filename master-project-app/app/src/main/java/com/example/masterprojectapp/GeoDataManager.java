package com.example.masterprojectapp;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GeoDataManager {

    private static final String TAG = "GeoDataManager";

    public List<Marker> parseGeoFile(Context context, MapView mapView) {

        List<Marker> markerList = new ArrayList<>();

        try (InputStream inputStream = context.getAssets().open("public-restroom.geo")) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, "UTF-8");

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                String tagName = parser.getName();
                Marker wcMarker = new Marker(mapView);
                GeoPoint wcPoint = new GeoPoint(0.0, 0.0);;

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals("entry")) {
                            Log.d(TAG, "Se lee etiqueta <entry>");
                            wcMarker = new Marker(mapView);
                            wcPoint = new GeoPoint(0.0, 0.0);
                        } else  {
                            if (tagName.equals("geo:lat")) {
                                Log.d(TAG, "Se lee etiqueta <geo:lat>");
                                wcPoint.setLatitude(Double.parseDouble(parser.nextText()));
                            } else if (tagName.equals("geo:long")) {
                                Log.d(TAG, "Se lee etiqueta <geo:long>");
                                wcPoint.setLongitude(Double.parseDouble(parser.nextText()));
                            }

                            wcMarker.setPosition(wcPoint);
                        }

                        break;

                    case XmlPullParser.END_TAG:
                        Log.d(TAG, "Se lee etiqueta </entry>");
                        if (tagName.equals("entry") && wcMarker.getPosition() != null) {
                            Log.i(TAG, "Se registra una entrada en la lista.");
                            markerList.add(wcMarker);
                        }

                        break;
                }

                for (int i = 0; i < Math.min(markerList.size(), 10); i++) {
                    Marker mrk = markerList.get(i);
                    Log.d(TAG, "Marca: Long " + mrk.getPosition().getLongitude() + " y Lat " + mrk.getPosition().getLatitude());
                }

                eventType = parser.next();
            }

        } catch (IOException error) {
            Log.e(TAG, "Error al leer el archivo public-restroom.geo: " + error.getMessage());
        } catch (XmlPullParserException error) {
            Log.e(TAG, "Error al parsear el archivo public-restroom.geo: " + error.getMessage());
        }

        return markerList;
        // [END parseGeoFile()]
    }

}
