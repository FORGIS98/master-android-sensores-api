package com.example.masterprojectapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BathroomActivity extends AppCompatActivity {

    private static final String TAG = "BathroomActivity";
    private ActivityResultLauncher<Intent> activityLauncher;

    private String imgTitle;
    private Double restroom_x_post;
    private Double restroom_y_post;
    private Double user_x_post;
    private Double user_y_post;

    Button buttonRuta;
    TextView tituloTextView;
    FloatingActionButton takeFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bathroom);

        Intent intent = getIntent();
        if (intent != null) {
            imgTitle = intent.getStringExtra("title");
            restroom_x_post = intent.getDoubleExtra("restroom_x_post", 0);
            restroom_y_post = intent.getDoubleExtra("restroom_y_post", 0);
            user_x_post = intent.getDoubleExtra("user_x_post", 0);
            user_y_post = intent.getDoubleExtra("user_y_post", 0);
        }

        // TODO: Acceder a Firebase para obtener la imagen de ese baño y actualizar el ImageView

        buttonRuta = findViewById(R.id.como_ir);
        tituloTextView = findViewById(R.id.bath_title);
        takeFoto = findViewById(R.id.add_foto);

        tituloTextView.setText(imgTitle);

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

        takeFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BathroomActivity.this, CameraActivity.class);
                intent.putExtra("title", imgTitle);
                activityLauncher.launch(intent);
            }
        });

        activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent resultIntent = result.getData();
                if (result.getResultCode() == RESULT_OK && resultIntent != null) {
                    int activityCode = resultIntent.getIntExtra("activityCode", 0);
                    switch (activityCode) {
                        case 1:
                            Log.i(TAG, "CameraActivity Terminada Correctamente");
                            MyFirebaseStorage myFirebaseStorage = new MyFirebaseStorage();
                            myFirebaseStorage.retrievePicture(imgTitle, new MyFirebaseStorage.OnImageDownloadedListener() {
                                @Override
                                public void onImageDownloaded(Bitmap bitmap) {

                                }

                                @Override
                                public void onImageDownloadedError(String errorMessage) {

                                }
                            });
                            break;
                        case 2:
                            Log.i(TAG, "EMTActivity Terminada Correctamente");
                            break;
                        default:
                            Log.e(TAG, "Actividad no reconocida.");
                            break;
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Log.e(TAG, "La activity ha terminado mal");
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