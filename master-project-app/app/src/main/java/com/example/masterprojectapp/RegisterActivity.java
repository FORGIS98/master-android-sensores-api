package com.example.masterprojectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    Button signupButton, button_login;
    EditText signupEmail, signupPassword;
    TextView loginText;

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        signupEmail = findViewById(R.id.email_register);
        signupPassword = findViewById(R.id.password_register);
        signupButton = findViewById(R.id.button_register);
        loginText = findViewById(R.id.loginText);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    signupEmail.setError("Es obligatorio rellenar este campo.");
                    return;
                }
                if (password.isEmpty()) {
                    signupPassword.setError("Es obligatorio rellenar este campo.");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    signupEmail.setError("Debe ser un correo electrónico.");
                    return;
                }
                if (password.length() < 6) {
                    signupPassword.setError("La contraseña debe tener al menos 6 caracteres.");
                    return;
                }
                Log.i(TAG, "Registrar usuario");
                registerUser(email, password);
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void registerUser(String email, String password) {
        Toast.makeText(RegisterActivity.this, "Registrando al nuevo usuario", Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Usuario " + email + " registrado con éxito.");
                    sendEmailVerification();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Log.e(TAG, "Error al registar el usuario " + task.getException().getMessage());
                    Toast.makeText(RegisterActivity.this, "Error al registar el usuario.", Toast.LENGTH_SHORT).show();
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
}