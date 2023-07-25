package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    String str_email,str_phone,str_pass,str_con_pass;
    String type;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    EditText phoneNum;
    EditText email;
    EditText password;
    EditText confirmPass;
    AppCompatButton registerAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        phoneNum = findViewById(R.id.phoneNo);
        registerAccount = findViewById(R.id.registerAccount);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPass = findViewById(R.id.confirmPass);
        type = intent.getStringExtra("type");

        registerAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Validation();
            }
        });
    }

    private void Validation() {
        str_email = email.getText().toString();
        str_phone = phoneNum.getText().toString();
        str_pass = password.getText().toString();
        str_con_pass = confirmPass.getText().toString();

        if (str_phone.isEmpty()) {
            phoneNum.setError("Please fill field");
            phoneNum.requestFocus();
            return;
        }

        if (!numberCheck(str_phone)) {
            phoneNum.setError("Invalid Mobile no.");
            phoneNum.requestFocus();
            return;
        }

        if (str_email.isEmpty()) {
            email.setError("Please fill field");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {
            email.setError("Please enter valid email");
            email.requestFocus();
            return;
        }
        if (str_pass.isEmpty()) {
            password.setError("Please fill field");
            password.requestFocus();
            return;
        } else if (!passwordValidation(str_pass)) {
            password.setError("Enter minimum 7 digits");
            password.requestFocus();
            return;
        }
        if (str_con_pass.isEmpty()) {
            confirmPass.setError("Please fill field");
            confirmPass.requestFocus();
            return;
        } else if (!passCheck(str_pass, str_con_pass)) {
            confirmPass.setError("password not matched");
            confirmPass.requestFocus();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        mAuth.fetchSignInMethodsForEmail(str_email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {

                    SignInMethodQueryResult result = task.getResult();
                    if (result.getSignInMethods().size() > 0) {
                        email.setError("Email already exists");
                        email.requestFocus();
                    } else {
                        Intent intent = new Intent(RegisterActivity.this, VerifyPhoneNo.class);
                        intent.putExtra("email", str_email);
                        intent.putExtra("password", str_pass);
                        intent.putExtra("type", type);
                        intent.putExtra("phoneNo", str_phone);
                        startActivity(intent);
                    }
                } else {
                    // Handle the error
                    Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    private boolean passCheck(String str_pass,String str_con_pass){
        if(str_pass.equals(str_con_pass)){
            return true;
        }
        return false;
    }
    private boolean passwordValidation(String str_pass){
        if(str_pass.length() > 6){
            return true;
        }
        return false;
    }

    private boolean numberCheck(String str_phone){
        Pattern p =Pattern.compile("[0-9]{10}");
        Matcher m = p.matcher(str_phone);
        return m.matches();
    }
}