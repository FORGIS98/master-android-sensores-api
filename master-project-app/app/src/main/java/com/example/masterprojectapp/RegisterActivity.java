package com.example.masterprojectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    Button button_register;
    EditText email, password;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email_register);
        password = findViewById(R.id.password_register);
        button_register = findViewById(R.id.button_register);

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailUser = email.getText().toString().trim();
                String passwordUser = password.getText().toString().trim();
                
                registerUser(emailUser, passwordUser);
            }
        });

    }

    private void registerUser(String emailUser, String passwordUser) {

        Log.d(TAG, "Se va a registrar al usuario: " + emailUser);

        auth.createUserWithEmailAndPassword(emailUser, passwordUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.i(TAG, "Usuario " + emailUser + " registrado con éxito.");

                Map<String, Object> map = new HashMap<>();
                map.put("id", auth.getCurrentUser().getUid());
                map.put("email", emailUser);
                map.put("password", passwordUser);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, R.string.fail_to_register, Toast.LENGTH_SHORT).show();
            }
        });
    }
}