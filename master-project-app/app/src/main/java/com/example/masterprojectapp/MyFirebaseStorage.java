package com.example.masterprojectapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MyFirebaseStorage {

    private static final String TAG = "MyFirebaseStorage";

    private final FirebaseStorage myStorage = FirebaseStorage.getInstance();
    private final FirebaseDatabase myDatabase = FirebaseDatabase.getInstance();
    private final String PICTURE_ROUTE = "restroom-pictures";
    private final String METADATA_ROUTE = "metadata";

    public boolean savePicture(String pictureName, Bitmap pictureBitmap, String pictureLux) {

        pictureName = pictureName.toLowerCase().replaceAll(" ", "-").replaceAll(",", "-");

        // Ruta en la que se va a guardar la foto
        StorageReference storageRef = myStorage.getReference().child(PICTURE_ROUTE + "/" + pictureName + ".jpg");

        // Se convierte la foto bitmap a byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] pictureBytes = baos.toByteArray();

        // Se sube a firebase la foto
        UploadTask upTask = storageRef.putBytes(pictureBytes);
        AtomicBoolean success = new AtomicBoolean(false);
        String finalPictureName = pictureName;
        upTask.addOnSuccessListener(taskSnapshot -> {
            Log.i(TAG, "Foto guardada correctamente.");

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "Imagen guardada en : " + uri.toString());
                success.set(saveImageMetadata(finalPictureName, pictureLux));
            }).addOnFailureListener(exception -> Log.e(TAG, "Error al obtener la URI: " + exception.getMessage()));

        }).addOnFailureListener(exception -> Log.e(TAG, "Fallo al guardar la foto: " + exception.getMessage()));

        return success.get();
        // [END - savePicture]
    }

    private boolean saveImageMetadata(String pictureName, String pictureLux) {
        pictureName = pictureName.toLowerCase().replaceAll(" ", "-").replaceAll(",", "-");

        Map<String, String> metadatos = new HashMap<>();
        metadatos.put(pictureName, pictureLux);

        AtomicBoolean success = new AtomicBoolean(false);

        return success.get();
        // [END - saveImageMetadata]
    }

    public void retrievePicture(String pictureName, final OnImageDownloadedListener listener) {
        pictureName = pictureName.toLowerCase().replaceAll(" ", "-").replaceAll(",", "-");

        StorageReference storageRef = myStorage.getReference(PICTURE_ROUTE + "/" + pictureName + ".jpg");
        final long MAX_BYTES = 1024 * 1024;

        Log.d(TAG, "Se busca la foto en la ruta: " + storageRef.getPath());

        try {
            File tempFile = File.createTempFile(pictureName, ".jpg");

            storageRef.getFile(tempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "Archivo recuperado.");

                    Bitmap myImageBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                    listener.onImageDownloaded(myImageBitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Ocurrio un error al guardar el archivo: " + e.getMessage());
                }
            });

        } catch (IOException e) {
             e.printStackTrace();
        }

    }

    public interface OnImageDownloadedListener {
        void onImageDownloaded (Bitmap bitmap);
        void onImageDownloadedError (String errorMessage);
    }

}
