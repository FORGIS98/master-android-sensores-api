package com.example.masterprojectapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BathroomActivity extends AppCompatActivity {

    private static final String TAG = "BathroomActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bathroom);
        String title = "";

        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra("title");
            String position = intent.getStringExtra("position");

            Log.i(TAG, "Title: " + title + ", Position: " + position.toString());
        }

        Button myButton = findViewById(R.id.map_back);
        TextView myTextView = findViewById(R.id.bath_title);

        myTextView.setText(title);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToPreviousActivity();
            }
        });

    }

    private void goBackToPreviousActivity() {
        Intent resultIntent = new Intent();
        // resultIntent.putExtra("paramName", value); // Agrega los datos que deseas devolver a la actividad anterior
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}