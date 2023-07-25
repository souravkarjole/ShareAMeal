package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EnterYourPhoneNo extends AppCompatActivity {

    EditText enterPhone;
    AppCompatButton saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_your_phone_no);

        enterPhone = findViewById(R.id.enterPhoneNo);
        saveBtn = findViewById(R.id.saveNum);


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = enterPhone.getText().toString();
                Intent intent = new Intent(getApplicationContext(),VerifyPhoneNo.class);
                intent.putExtra("phoneNo",phone);
                startActivity(intent);
                finish();
            }
        });
    }
}