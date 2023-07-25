package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.telephony.SmsManager;


public class Admin_Panel extends AppCompatActivity {

    DatabaseReference db;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Button send;
     Exception exception;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        send = findViewById(R.id.email);
        db = FirebaseDatabase.getInstance().getReference().child("Details");
        ActivityCompat.requestPermissions(Admin_Panel.this, new String[]{Manifest.permission.SEND_SMS}, 1);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap: snapshot.getChildren()){
                            if(snap.child("type").exists()){
                                String type = snap.child("type").getValue(String.class);
                                assert type != null;
                                if(type.equals("donar")){
                                    String phoneNum = snap.child("phoneNum").getValue(String.class);
                                    String username = snap.child("username").getValue(String.class);
                                    phoneNum = phoneNum.substring(3);
                                    String message = "Hello " + username +  ", and welcome to " + "FoodieFest.\n" +
                                            "We appreciate your support. " +
                                            "Don't forget to donate non-perishable food items to help those in need." +
                                            "Thank you!";
                                    Toast.makeText(Admin_Panel.this, phoneNum, Toast.LENGTH_SHORT).show();
                                    sendSms(phoneNum,message);

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    private void sendSms(String phoneNumber,String message){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                try {
                    SmsManager smsManager = SmsManager.getDefault();

                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    Toast.makeText(Admin_Panel.this, "sent successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


