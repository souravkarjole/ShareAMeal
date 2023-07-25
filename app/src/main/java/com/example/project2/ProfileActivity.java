package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.utilities.Validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    String type;
    String str_username,str_state,str_city,str_restaurant;
    String user_id;

    Spinner state_spinner,city_spinner;
    EditText profile_username;
    EditText profile_restaurantName;
    AppCompatButton saveBtn;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        user_id = mUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Details").child(user_id);

        getType();
        profile_username = findViewById(R.id.profile_username);
        profile_restaurantName = findViewById(R.id.profile_restaurantName);
        state_spinner = findViewById(R.id.state_spinner);
        city_spinner = findViewById(R.id.city_spinner);
        saveBtn = findViewById(R.id.profile_saveBtn);

        setSpinnerColour();
        checkIfUserIsSaved();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Validation()){
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("username", str_username);
                    updateData.put("state", str_state);
                    updateData.put("city", str_city);
                    if(type.equals("donar")) {
                        updateData.put("restaurant", str_restaurant);
                    }
                    databaseReference.updateChildren(updateData);
                    onBackPressed();
                }
            }
        });
    }

    private void getType(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                type = snapshot.child("type").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkIfUserIsSaved(){
        initialize_details();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("username").exists()){
                        profile_username.setText(snapshot.child("username").getValue(String.class));
                        if(type.equals("donar")) {
                            profile_restaurantName.setText(snapshot.child("restaurant").getValue(String.class));
                        }
                        setSpinnerForState(snapshot);
                        setSpinnerForCity(snapshot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setSpinnerForState(DataSnapshot dataSnapshot){

        for (int position = 0; position < state_spinner.getCount(); position++) {

            if(state_spinner.getItemAtPosition(position).toString().equals(dataSnapshot.child("state").getValue(String.class))) {
                state_spinner.setSelection(position);
                return;
            }
        }
    }
    private void setSpinnerForCity(DataSnapshot dataSnapshot){
        for (int position = 0; position < city_spinner.getCount(); position++) {
            if(city_spinner.getItemAtPosition(position).toString().equals(dataSnapshot.child("city").getValue(String.class))) {
                city_spinner.setSelection(position);
                return;
            }
        }
    }
    private boolean Validation(){
        initialize_details();

        if (str_username.isEmpty()) {
            profile_username.setError("Please fill field");
            profile_username.requestFocus();
            return false;
        }

        if (state_spinner.getSelectedItem().equals("Select states")) {
            ((TextView)state_spinner.getSelectedView()).setError("Select your state");
            state_spinner.requestFocus();
            return false;
        }

        if (city_spinner.getSelectedItem().equals("Select cities")) {
            ((TextView)city_spinner.getSelectedView()).setError("Select your city");
            city_spinner.requestFocus();
            return false;
        }

        if (str_restaurant.isEmpty() && type.equals("donar")) {
            profile_restaurantName.setError("Please fill field");
            profile_restaurantName.requestFocus();
            return false;
        }

        return true;
    }
    private void initialize_details(){
        str_username = profile_username.getText().toString();
        str_state = state_spinner.getSelectedItem().toString();
        str_city = city_spinner.getSelectedItem().toString();
        str_restaurant = profile_restaurantName.getText().toString();
    }

    private void setSpinnerColour(){
        state_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        city_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }



    @Override
    public void onBackPressed() {
//        if(getSupportFragmentManager().getBackStackEntryCount() >= 0){
            super.onBackPressed();
//        }
    }
}