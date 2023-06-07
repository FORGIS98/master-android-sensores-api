package com.example.masterprojectapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    Button loginButton;
    EditText loginEmail, loginPassword;
    TextView signupText;

    FirebaseAuth mAuth;
    FirebaseUser loggedUser;

    private ActivityResultLauncher<Intent> activityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Context context = getApplicationContext();

        mAuth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.email_login);
        loginPassword = findViewById(R.id.password_login);
        loginButton = findViewById(R.id.button_login);
        signupText = findViewById(R.id.register_text);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    if(!password.isEmpty()) {
                        loginUser(email, password);
                    } else {
                        loginPassword.setError("Es obligatorio rellenar este campo.");
                    }
                } else if(email.isEmpty()) {
                    loginEmail.setError("Es obligatorio rellenar este campo.");
                } else {
                    loginEmail.setError("Por favor introduce un correo valido.");
                }
            }
        });

        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.i(TAG, "ActivityResult");

                Intent resultIntent = result.getData();
                if (result.getResultCode() == RESULT_OK && resultIntent != null) {
                    int activityCode = resultIntent.getIntExtra("activityCode", 0);
                    if (activityCode == Integer.parseInt(context.getString(R.string.map_code))) {
                        Log.i(TAG, "MapActivity Terminada Correctamente");
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Log.e(TAG, "La activity ha terminado mal");
                    }
                }
            }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.i(TAG, "El usuario ya estaba logado: " + currentUser.getEmail());
            Intent intent = new Intent(LoginActivity.this, MapActivity.class);
            activityLauncher.launch(intent);
        }
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.i(TAG, "Se ha iniciado correctamente la sesion de: " + email);
                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                activityLauncher.launch(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error loginUser:signInWithEmail:onFailureListener:" + e.getMessage());
                Toast.makeText(LoginActivity.this, R.string.fail_to_login, Toast.LENGTH_SHORT).show();
            }
        });
    }
}