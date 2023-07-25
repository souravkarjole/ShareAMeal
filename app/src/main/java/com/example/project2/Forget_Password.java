package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class Forget_Password extends AppCompatActivity {

    FirebaseAuth mAuth;
    String mVerificationId;

    Toolbar toolbar;
    AppCompatButton verifyBtn;
    EditText enterPhoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar_resetPassword);
        verifyBtn = findViewById(R.id.verifyBtn);
        enterPhoneNum = findViewById(R.id.phoneNo);

        setSupportActionBar(toolbar);
        toolbar.findViewById(R.id.cardreload).setVisibility(View.GONE);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("Reset Password");


        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = enterPhoneNum.getText().toString();

                if(phone.isEmpty()){
                    enterPhoneNum.requestFocus();
                    enterPhoneNum.setError("fill the field");
                }else {
                    Intent intent = new Intent(Forget_Password.this, OTP_LAYOUT.class);
                    intent.putExtra("phone", phone);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}