package com.example.masterprojectapp;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.content.Context;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class EMTApiHandler {

    private static final String API_BASE_URL = "https://openapi.emtmadrid.es/v1/";
    private static final String TAG = "EMTApiHandler";

    public void getAccessToken(final OnAccessTokenListener listener, Context context) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url(API_BASE_URL + "mobilitylabs/user/login/")
            .addHeader("email", context.getString(R.string.client_id))
            .addHeader("password", context.getString(R.string.client_secret))
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        JSONObject dataObject = dataArray.getJSONObject(0);
                        String accessToken = dataObject.getString("accessToken");

                        listener.onAccessTokenReceived(accessToken);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onAccessTokenError();
                    }
                } else {
                    Log.e(TAG, "" + response);
                    listener.onAccessTokenError();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                listener.onAccessTokenError();
            }
        });
    }

    public String makeApiRequest(final OnAccessTokenListener listener, String path, String accessToken, JsonObject params) {
        OkHttpClient client = new OkHttpClient();

        // Construir el cuerpo de la solicitud si es necesario
        RequestBody requestBody = null;
        if (params != null) {
            String paramsString = params.toString();
            requestBody = RequestBody.Companion.create(paramsString, MediaType.get("application/json"));
        }

        // Construir la solicitud
        Request.Builder requestBuilder = new Request.Builder()
                .url(API_BASE_URL + path)
                .header("accessToken", accessToken);

        if (requestBody != null) {
            requestBuilder.post(requestBody);
        }

        Request request = requestBuilder.build();

        try {
            // Ejecutar la solicitud
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnAccessTokenListener {
        void onAccessTokenReceived(String accessToken);
        void onAccessTokenError();
    }
}
