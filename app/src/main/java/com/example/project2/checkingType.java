package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class checkingType extends AppCompatActivity {

    String user_id;
    String type;
    View view;

    AlertDialog dialog;
    AlertDialog.Builder builder;


    ProgressBar progressBar;
    AppCompatButton donarbtn;
    AppCompatButton volunteerbtn;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checking_type);

        builder = new AlertDialog.Builder(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Details");

        user_id = mUser.getUid();

        if(mUser.getPhoneNumber() == null){
            Toast.makeText(this, "No verification found with phone number", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }

        progressBar = findViewById(R.id.progressBarr);
        CheckTypeIsPresent();

        view = getLayoutInflater().inflate(R.layout.activity_dialogbox,null);
        donarbtn = view.findViewById(R.id.donar_btn);
        volunteerbtn = view.findViewById(R.id.volunteer_btn);


        donarbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "donar";
                insertData(type);
            }
        });

        volunteerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "volunteer";
                insertData(type);
            }
        });
    }

    private void CheckTypeIsPresent() {
        databaseReference.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot data: snapshot.getChildren()){
                        if (Objects.equals(data.getValue(), "donar")) {
                            //do ur stuff
                            Intent intent = new Intent(getApplicationContext(),Navigation_Drawer.class);
                            intent.putExtra("type","donar");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }
                        if(data.getValue().equals("volunteer")) {
                            Intent intent = new Intent(getApplicationContext(),Navigation_Drawer.class);
                            intent.putExtra("type","volunteer");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }
                }else{
                    progressBar.setVisibility(View.GONE);

                    builder.setView(view);
                    dialog = builder.create();
                    dialog.show();
                    dialog.setCancelable(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(checkingType.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void insertData(String type){
        databaseReference = firebaseDatabase.getReference("Details").child(user_id);
        Map<String,Object> updateDB = new HashMap<>();
        updateDB.put("type",type);
        updateDB.put("phoneNum",mUser.getPhoneNumber());
        databaseReference.updateChildren(updateDB);
    }
}