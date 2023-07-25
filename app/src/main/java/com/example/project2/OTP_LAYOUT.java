package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTP_LAYOUT extends AppCompatActivity {

    FirebaseAuth mAuth;
    String phone,mVerificationId;

    AppCompatButton verify_Btn;
    EditText phoneNoEntered;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_layout);

        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        verify_Btn = findViewById(R.id.verifyOtp);
        phoneNoEntered = findViewById(R.id.enterOtp);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        phone = intent.getStringExtra("phone");
        sendVerificationCodeToUser(phone);

        verify_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = phoneNoEntered.getText().toString();
                if(code.isEmpty() || code.length() < 6){
                    phoneNoEntered.setError("Wrong otp...");
                    phoneNoEntered.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                changePassword(code);
            }
        });
    }


    private void sendVerificationCodeToUser(String phoneNo){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + phoneNo)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(OTP_LAYOUT.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            String code = credential.getSmsCode();

            if(code != null){
                Toast.makeText(OTP_LAYOUT.this, "Complete....", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.VISIBLE);
                changePassword(code);
            }else{
                Toast.makeText(OTP_LAYOUT.this, "Something went Wrong!Please try again", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(OTP_LAYOUT.this, "Verification Failed!", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            mVerificationId = verificationId;
        }
    };

    private void changePassword(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Phone number verification successful
//                            Objects.requireNonNull(mAuth.getCurrentUser()).delete();
                            Intent intent = new Intent(OTP_LAYOUT.this,Change_Password.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Phone number verification failed
                            Toast.makeText(OTP_LAYOUT.this, "Verification Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}