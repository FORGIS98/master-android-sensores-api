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

        try (InputStream inputStream = context.getAssets().open("public-restrooms.geo")) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, "UTF-8");

            int eventType = parser.getEventType();
            GeoPoint wcPoint = new GeoPoint(0.0, 0.0);
            String title = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals("geo:lat")) {
                            wcPoint.setLatitude(Double.parseDouble(parser.nextText()));
                        } else if (tagName.equals("geo:long")) {
                            wcPoint.setLongitude(Double.parseDouble(parser.nextText()));
                        } else if (tagName.equals("title")) {
                            title = parser.nextText();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagName.equals("entry")) {
                            Marker wcMarker = new Marker(mapView);
                            wcMarker.setPosition(wcPoint);
                            wcMarker.setTitle(title);
                            markerList.add(wcMarker);
                        }
                        break;
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
