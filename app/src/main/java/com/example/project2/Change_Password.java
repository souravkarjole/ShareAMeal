package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.core.utilities.Validation;

import java.util.Objects;

public class Change_Password extends AppCompatActivity {

    String str_email,str_New_pass;

    FirebaseAuth  mAuth;
    FirebaseUser mUser;
    EditText enterEmail;
    AppCompatButton saveBtn;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        toolbar = findViewById(R.id.toolbar_resetPassword);
        enterEmail = findViewById(R.id.email);
        saveBtn = findViewById(R.id.saveBtn);

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Validation()){
                    checkIfEmailExists(str_email);
                }
            }
        });
    }
    private void checkIfEmailExists(String _email){
        mAuth.fetchSignInMethodsForEmail(_email)
                .addOnCompleteListener(task -> {
                    boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                    if (emailExists) {
                        // The email exists in Firebase Authentication
                        sendPasswordRequest(_email);
                    } else {
                        // The email does not exist in Firebase Authentication
                        enterEmail.setError("No such email exists");
                        enterEmail.requestFocus();
                    }
                });
    }

    private void sendPasswordRequest(String str_email){
        mAuth.sendPasswordResetEmail(str_email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password reset email sent successfully
                            Toast.makeText(getApplicationContext(),
                                    "Password reset email sent to " + str_email,
                                    Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            // Password reset email sending failed
                            Toast.makeText(getApplicationContext(),
                                    "Failed to send password reset email",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private boolean Validation() {
        str_email = enterEmail.getText().toString();

        if (str_email.isEmpty()) {
            enterEmail.setError("Please fill field");
            enterEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {
            enterEmail.setError("Please enter valid email");
            enterEmail.requestFocus();
            return false;
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}