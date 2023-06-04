package com.example.masterprojectapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MyFirebaseStorage {

    private static final String TAG = "MyFirebaseStorage";

    private final FirebaseStorage myStorage = FirebaseStorage.getInstance();
    private final FirebaseFirestore myFirestore = FirebaseFirestore.getInstance();
    private final String PICTURE_ROUTE = "restroom-pictures";
    private final String METADATA_ROUTE = "metadata";

    public boolean savePicture(String pictureName, Bitmap pictureBitmap, String pictureLux) {

        // Ruta en la que se va a guardar la foto
        StorageReference storageRef = myStorage.getReference().child(PICTURE_ROUTE + "/" + pictureName + ".jpg");

        // Se convierte la foto bitmap a byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] pictureBytes = baos.toByteArray();

        // Se sube a firebase la foto
        UploadTask upTask = storageRef.putBytes(pictureBytes);
        AtomicBoolean success = new AtomicBoolean(false);
        upTask.addOnSuccessListener(taskSnapshot -> {
            Log.i(TAG, "Foto guardada correctamente.");

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "Imagen guardada en : " + uri.toString());
                success.set(saveImageMetadata(pictureName, pictureLux));
            }).addOnFailureListener(exception -> Log.e(TAG, "Error al obtener la URI: " + exception.getMessage()));

        }).addOnFailureListener(exception -> Log.e(TAG, "Fallo al guardar la foto: " + exception.getMessage()));

        return success.get();
        // [END - savePicture]
    }

    public boolean saveImageMetadata(String pictureName, String pictureLux) {

        Map<String, String> metadatos = new HashMap<>();
        metadatos.put(pictureName, pictureLux);

        AtomicBoolean success = new AtomicBoolean(false);
        myFirestore.collection(METADATA_ROUTE).add(metadatos).addOnSuccessListener(documentReference -> {
            Log.i(TAG, "Se han guardado los metadatos correctamente con ID: " + documentReference.getId());
            success.set(true);
        }).addOnFailureListener(exception -> Log.e(TAG, "Fallo al guardar los metadatos: " + exception.getMessage()));

        return success.get();
        // [END - saveImageMetadata]
    }

    public void retrievePicture(String pictureName, final OnImageDownloadedListener listener) {
        StorageReference storageRef = myStorage.getReference().child(PICTURE_ROUTE + "/" + pictureName + ".jpg");
        final long MAX_BYTES = 1024 * 1024;

        storageRef.getBytes(MAX_BYTES).addOnSuccessListener(bytes -> {
            Bitmap myImageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Log.i(TAG, "La imagen se descargado correctamente.");
            listener.onImageDownloaded(myImageBitmap);
        }).addOnFailureListener(exception -> listener.onImageDownloadedError(exception.getMessage()));

    }

    public String retrievePictureLux(String pictureName) {

        CollectionReference metadataCollection = myFirestore.collection(METADATA_ROUTE);
        AtomicReference<String> pictureLux = new AtomicReference<>("0");

        metadataCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot docSnap : queryDocumentSnapshots)
                pictureLux.set(docSnap.getString(pictureName));
        }).addOnFailureListener(exception -> Log.e(TAG, "Error al recuperar los metadatos: " + exception.getMessage()));

        return pictureLux.get();
    }

    public interface OnImageDownloadedListener {
        void onImageDownloaded (Bitmap bitmap);
        void onImageDownloadedError (String errorMessage);
    }

}
