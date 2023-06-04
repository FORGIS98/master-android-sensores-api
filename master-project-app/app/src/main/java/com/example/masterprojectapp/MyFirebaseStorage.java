package com.example.masterprojectapp;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class MyFirebaseStorage {

    private static final String TAG = "MyFirebaseStorage";

    private FirebaseStorage myStorage = FirebaseStorage.getInstance();
    private final String PICTURE_ROUTE = "restroom-pictures";

    public boolean savePicture(String pictureName, Bitmap pictureBitmap) {

        // Ruta en la que se va a guardar la foto
        StorageReference storageRef = myStorage.getReference().child(PICTURE_ROUTE + "/" + pictureName + ".jpg");

        // Se convierte la foto bitmap a byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] pictureBytes = baos.toByteArray();

        // Se sube a firebase la foto
        UploadTask upTask = storageRef.putBytes(pictureBytes);
        upTask.addOnSuccessListener(taskSnapshot -> {
            Log.i(TAG, "Foto guardada correctamente.");

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageURL = uri.toString();
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Error al obtener la URI: " + exception.getMessage());
            });

        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Fallo al guardar la foto: " + exception.getMessage());
        });



        return false;
        // [END - savePicture]
    }
}
