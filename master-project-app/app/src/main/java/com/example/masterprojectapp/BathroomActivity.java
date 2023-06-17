package com.example.masterprojectapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.base.Strings;

public class BathroomActivity extends AppCompatActivity {

    private int ACTIVITY_CODE;
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
    public static ImageView imageView;

    MyFirebaseStorage myStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bathroom);

        myStorage = new MyFirebaseStorage();
        Context context = getApplicationContext();
        ACTIVITY_CODE = Integer.parseInt(context.getString(R.string.bathroom_code));

        Intent intent = getIntent();
        if (intent != null) {
            imgTitle = intent.getStringExtra("title");
            restroom_x_post = intent.getDoubleExtra("restroom_x_post", 0);
            restroom_y_post = intent.getDoubleExtra("restroom_y_post", 0);
            user_x_post = intent.getDoubleExtra("user_x_post", 0);
            user_y_post = intent.getDoubleExtra("user_y_post", 0);
        } else {
            Log.e(TAG, "Error al obtener el intent");
        }

        buttonRuta = findViewById(R.id.como_ir);
        tituloTextView = findViewById(R.id.bath_title);
        takeFoto = findViewById(R.id.add_foto);
        imageView = findViewById(R.id.foto);

        tituloTextView.setText(imgTitle);

        setImage();

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

                    if(activityCode == Integer.parseInt(context.getString(R.string.camera_code))) {
                        Log.i(TAG, "CameraActivity Terminada Correctamente");
                    } else if(activityCode == Integer.parseInt(context.getString(R.string.emt_code))) {
                        Log.i(TAG, "EMTActivity Terminada Correctamente");
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

    private void setImage() {

        // Si imgTitle no tiene valor se sale
        if (Strings.isNullOrEmpty(imgTitle)) {
            return;
        }

        myStorage.retrievePicture(imgTitle, new MyFirebaseStorage.OnImageDownloadedListener() {
            @Override
            public void onImageDownloaded(Bitmap bitmap) {
                BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);

                Matrix matrix = new Matrix();
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
                    matrix.postRotate(90);
                }

                imageView.setImageDrawable(drawable);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setRotation(rotation * 90); // Girar la vista del ImageView según la orientación del dispositivo
            }

            @Override
            public void onImageDownloadedError(String errorMessage) {
                Log.e(TAG, "Error al recuperar la imagen: " + errorMessage);
            }
        });
    }
}