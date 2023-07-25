package com.example.project2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.security.AuthProvider;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
        String str_email,str_pass;
        private static boolean flag = false;
        private static final int REQ_CODE = 100;

        TextView createAccount;
        TextView forgetPass;
        AppCompatButton loginbtn;
        EditText email;
        EditText password;


        CardView googleImg;
        GoogleSignInOptions gso;
        GoogleSignInClient gsc;

        FirebaseAuth mAuth;
        FirebaseUser mUser;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            googleImg = findViewById(R.id.googleSignIn);
            createAccount = findViewById(R.id.createAccount);
            loginbtn = findViewById(R.id.loginBtn);
            email = findViewById(R.id.loginEmail);
            password = findViewById(R.id.loginPass);
            forgetPass = findViewById(R.id.forgetPass);



            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
            mAuth = FirebaseAuth.getInstance();
            mUser = mAuth.getCurrentUser();


            if(mUser != null){
                Toast.makeText(LoginActivity.this,  mUser.getEmail(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, checkingType.class);
                startActivity(intent);
                finish();
            }else if (signInAccount != null) {
                    try {
                        Toast.makeText(LoginActivity.this, "Google: " + signInAccount.getEmail(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, checkingType.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        // Handle the exception here
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            }

            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            gsc = GoogleSignIn.getClient(this,gso);

            googleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SignIn();
                }
            });


            loginbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    str_email = email.getText().toString();
                    str_pass = password.getText().toString();

                    if(Validation()){
                        performAuth();
                    }
                }
            });


            createAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });

            forgetPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LoginActivity.this, Forget_Password.class);
                    startActivity(intent);
                }
            });
        }


        private void performAuth(){
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(str_email,str_pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        if(Objects.equals(str_email, "adminbibliophile22@gmail.com")){
                            Intent intent = new Intent(LoginActivity.this,Admin_Panel.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, checkingType.class);
                            startActivity(intent);
                            finish();
                        }
                    }else{
                        email.setError("Email is not registered");
                        email.requestFocus();
                    }
                }
            });
        }

        private boolean passwordValidation(String str_pass){
            if(str_pass.length() > 6){
                return true;
            }
            return false;
        }

        private boolean Validation(){
            str_email = email.getText().toString();
            str_pass = password.getText().toString();

            if (str_email.isEmpty()) {
                email.setError("Please fill field");
                email.requestFocus();
                return false;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {
                email.setError("Please enter valid email");
                email.requestFocus();
                return false;
            }

            if(str_pass.isEmpty()){
                password.setError("Please fill field");
                password.requestFocus();
                return false;
            }

            if(!passwordValidation(str_pass)){
                password.setError("Enter maximum 7 digits");
                password.requestFocus();
                return false;
            }
            return true;
        }

        public void SignIn(){
            Intent intent = gsc.getSignInIntent();
            startActivityForResult(intent,REQ_CODE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == 100){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    task.getResult(ApiException.class);
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    str_email = account.getEmail();
                    checkIfEmailExists(str_email,account.getIdToken());

                } catch (ApiException e) {
                    Toast.makeText(this, String.valueOf(resultCode), Toast.LENGTH_SHORT).show();
                }
            }
        }

    private void checkIfEmailExists(String _email,String idToken){
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.fetchSignInMethodsForEmail(_email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    if (result.getSignInMethods().size() > 0) {
                        // User exists
                        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
                        mAuth.signInWithCredential(credential)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            Intent intent = new Intent(LoginActivity.this, checkingType.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                    } else {
                        firebaseAuthWithGoogle(idToken);
                    }
                } else {
                    // Handle the error
                    Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
        private void firebaseAuthWithGoogle(String idToken){
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(LoginActivity.this, EnterYourPhoneNo.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


        @Override
        public void onBackPressed() {
            super.onBackPressed();
        }
}