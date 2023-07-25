package com.example.project2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyPhoneNo extends AppCompatActivity {
    String email,pass,type,phoneNo;
    String mVerificationId;

    FirebaseAuth mAuth;
    FirebaseUser mUser;


    AppCompatButton verify_Btn;
    EditText phoneNoEntered;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_no);

        verify_Btn = findViewById(R.id.verifyOtp);
        phoneNoEntered = findViewById(R.id.enterOtp);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        phoneNo = getIntent().getStringExtra("phoneNo");
        email = getIntent().getStringExtra("email");
        pass = getIntent().getStringExtra("password");
        type = getIntent().getStringExtra("type");


        mAuth = FirebaseAuth.getInstance();


        sendVerificationCodeToUser(phoneNo);

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
                verifyCode(code);
            }
        });
    }

    private void sendVerificationCodeToUser(String phoneNo){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + phoneNo)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(VerifyPhoneNo.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            String code = credential.getSmsCode();

            if(code != null){
                Toast.makeText(VerifyPhoneNo.this, "I am here", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }else{
                Toast.makeText(VerifyPhoneNo.this, "Something went Wrong!Please try again", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyPhoneNo.this, "Verification Failed!", Toast.LENGTH_SHORT).show();
            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(VerifyPhoneNo.this);
//            if(signInAccount != null){
//                deleteUser(signInAccount);
                deleteUser(signInAccount);
//            }else{
//                mAuth = FirebaseAuth.getInstance();
//                FirebaseAuth.getInstance().signOut();
//                mAuth.signOut();
                onBackPressed();
//            }
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            mVerificationId = verificationId;
        }
    };

    private void deleteUser(GoogleSignInAccount signInAccount){
        if (mUser != null) {
            AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
            mUser.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mUser.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Intent intent = new Intent(VerifyPhoneNo.this,LoginActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    SignOut();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(VerifyPhoneNo.this, "failed to reauthenticate user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void SignOut(){
        mAuth = FirebaseAuth.getInstance();

        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions googleSignInOptions;
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);

        googleSignInClient.signOut();
        mAuth.signOut();
    }

    private void verifyCode(String codeByUser){
        PhoneAuthCredential phonecredential = PhoneAuthProvider.getCredential(mVerificationId,codeByUser);

        signInWithGoogleOrBycCreation(phonecredential);
    }

    private void signInWithGoogleOrBycCreation(PhoneAuthCredential credential){
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(VerifyPhoneNo.this);

        if (signInAccount != null) {
            try {
                mAuth =  FirebaseAuth.getInstance();
                mUser = mAuth.getCurrentUser();
                linkBoth1(mUser,credential);
//                Toast.makeText(VerifyPhoneNo.this, "Registration Successful", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Handle the exception here
                Toast.makeText(VerifyPhoneNo.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where there is no signed-in account
            createAuth(credential);
        }
    }

    // link user with phone number

    private void createAuth(PhoneAuthCredential credential){
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
//                    Toast.makeText(VerifyPhoneNo.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    performAuth(credential);
                }else{
                    Toast.makeText(VerifyPhoneNo.this, ""+ task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void performAuth(PhoneAuthCredential credential){
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        mAuth = FirebaseAuth.getInstance();
                        mUser = mAuth.getCurrentUser();
                        linkBoth1(mUser,credential);
                        // User signed in successfully
                    } else {
                        // Sign-in failed
                        SignOut();
                        Toast.makeText(VerifyPhoneNo.this, "Unable to link", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        // User does not exist or has been disabled
                        Toast.makeText(VerifyPhoneNo.this, "user does not exists", Toast.LENGTH_SHORT).show();

                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid email or password
                        Toast.makeText(VerifyPhoneNo.this, "Invalid email and pass", Toast.LENGTH_SHORT).show();
                    } else {
                        // Other errors
                        Toast.makeText(VerifyPhoneNo.this, "Sign in failure", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void linkBoth1(FirebaseUser user,PhoneAuthCredential phonecredential){
        user.updatePhoneNumber(phonecredential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // phone number linked successfully
                        Toast.makeText(VerifyPhoneNo.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        sendUserToNextActivity();
                    } else {
                        // handle error
                        Toast.makeText(VerifyPhoneNo.this, "error linked phonecredential", Toast.LENGTH_SHORT).show();
                        SignOut();
                    }
                });
    }

    private void sendUserToNextActivity(){
        Intent intent = new Intent(getApplicationContext(), checkingType.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    // sign in with phone number

//    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(VerifyPhoneNo.this);
//
//                        } else {
//                            // Sign in failed, display a message and update the UI
//                            Toast.makeText(VerifyPhoneNo.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }


}