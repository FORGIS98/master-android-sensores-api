package com.example.masterprojectapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Calendar;

import com.google.gson.JsonObject;

public class BathroomActivity extends AppCompatActivity {

    private static final String TAG = "BathroomActivity";
    private ActivityResultLauncher<Intent> activityLauncher;

    private Double restroom_x_post;
    private Double restroom_y_post;
    private Double user_x_post;
    private Double user_y_post;

    Button buttonRuta;
    TextView tituloTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bathroom);
        String title = "";

        Context context = getApplicationContext();

        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra("title");
            restroom_x_post = intent.getDoubleExtra("restroom_x_post", 0);
            restroom_y_post = intent.getDoubleExtra("restroom_y_post", 0);
            user_x_post = intent.getDoubleExtra("user_x_post", 0);
            user_y_post = intent.getDoubleExtra("user_y_post", 0);
        }

        buttonRuta = findViewById(R.id.como_ir);
        tituloTextView = findViewById(R.id.bath_title);

        tituloTextView.setText(title);

        /* myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToPreviousActivity();
            }
        });*/

        buttonRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BathroomActivity.this, EMTActivity.class);
                intent.putExtra("restroom_x_post", restroom_x_post);
                intent.putExtra("restroom_y_post", restroom_y_post);
                intent.putExtra("user_x_post", user_x_post);
                intent.putExtra("user_y_post", user_y_post);
                activityLauncher.launch(intent);
            }
        });

        activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.i(TAG, "EMTActivity Terminada Correctamente");

                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Log.i(TAG, "EMTActivity Terminada Mal");
                }
            }
        );
    }

    private void goBackToPreviousActivity() {
        Intent resultIntent = new Intent();
        // resultIntent.putExtra("paramName", value); // Agrega los datos que deseas devolver a la actividad anterior
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}