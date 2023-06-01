package com.example.masterprojectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    Button button_register, button_login;
    EditText email, password;

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email_register);
        password = findViewById(R.id.password_register);
        button_register = findViewById(R.id.button_register);
        button_login = findViewById(R.id.button_login);

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailUser = email.getText().toString().trim();
                String passwordUser = password.getText().toString().trim();
                
                registerUser(emailUser, passwordUser);
            }
        });

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailUser = email.getText().toString().trim();
                String passwordUser = password.getText().toString().trim();

                loginUser(emailUser, passwordUser);
            }
        });

        // [END onCreate()]
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.i(TAG, "El usuario ya existe: " + currentUser.getEmail());
            updateUI(currentUser);
        }

        // [END onStart()]
    }

    private void registerUser(String emailUser, String passwordUser) {
        Log.d(TAG, "Se va a registrar al usuario: " + emailUser);

        mAuth.createUserWithEmailAndPassword(emailUser, passwordUser).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Log.i(TAG, "Usuario " + emailUser + " registrado con éxito.");
                    FirebaseUser loggedUser = mAuth.getCurrentUser();
                    sendEmailVerification();
                    updateUI(loggedUser);
                } else {
                    Log.e(TAG, "Error registerUser:createUserWithEmail:onComplete:" + task.getException().getMessage());
                    Toast.makeText(RegisterActivity.this, R.string.fail_to_register, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error registerUser:createUserWithEmail:onFailureListener:" + e.getMessage());
                Toast.makeText(RegisterActivity.this, R.string.fail_to_register, Toast.LENGTH_SHORT).show();
            }
        });

        // [END registerUser()]
    }

    private void loginUser(String emailUser, String passwordUser) {
        Log.d(TAG, "Se va a iniciar sesion al usuario: " + emailUser);

        mAuth.signInWithEmailAndPassword(emailUser, passwordUser).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Se ha iniciado correctamente la sesion de: " + emailUser);
                    FirebaseUser loggedUser = mAuth.getCurrentUser();
                    updateUI(loggedUser);
                } else {
                    Log.e(TAG, "Error loginUser:signInWithEmail:onComplete: " + task.getException().getMessage());
                    Toast.makeText(RegisterActivity.this, R.string.fail_to_login, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error loginUser:signInWithEmail:onFailureListener:" + e.getMessage());
                Toast.makeText(RegisterActivity.this, R.string.fail_to_login, Toast.LENGTH_SHORT).show();
            }
        });


        // [END login()]
    }

    private void sendEmailVerification() {

        final FirebaseUser user = mAuth.getCurrentUser();

        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Se ha enviado un correo al usuario: " + user.getEmail());
                } else {
                    Log.d(TAG, "NO se ha enviado un correo al usuario: " + user.getEmail());
                    Log.e(TAG, "Error sendEmail:sendEmailVerification:onFailure: " + task.getException().getMessage());
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "NO se ha enviado un correo al usuario: " + user.getEmail());
                Log.e(TAG, "Error sendEmail:sendEmailVerification:onFailure: " + e.getMessage());
            }
        });

        // [END sendEmailVerification]
    }

    private void updateUI(FirebaseUser loggedUser) {
        Log.i(TAG, "Se actualiza la interfaz de usuario.");
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        startActivity(intent);

        Log.i(TAG, "LLAMANDO A FINISH()");

        finish();
        // [END updateUI()]
    }
}