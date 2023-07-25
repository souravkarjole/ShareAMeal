package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TakeAwayFood extends AppCompatActivity {

    public static final double RADIUS_OF_EARTH_KM = 6371;
    View view;
    AlertDialog dialog;
    AlertDialog.Builder builder;
    Button cancel;
    String citystate;

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    Adapter_VOLUNTEER adapter;
    List<String> checkAllDonars;
    List<String> addUids;
    List<HandlerRecyclerViewClass_VOLUNTEER> itemList;

    Toolbar toolbar;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference volunteerDataRef;
    DatabaseReference donarDataRef;

    double latitude_VOLUNTEER, longitude_VOULNTEER;
    double latitude_DONAR, longitude_DONAR;
    double maxDistance = 7;
    Geocoder geocoder;

    @SuppressLint("UseSupportActionBar")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_away_food);

        geocoder = new Geocoder(this, Locale.getDefault());
        toolbar = findViewById(R.id.toolbarr);
        toolbar.findViewById(R.id.cardreload).setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        setDialog();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        volunteerDataRef = FirebaseDatabase.getInstance().getReference().child("Details").child(mUser.getUid());
        donarDataRef = FirebaseDatabase.getInstance().getReference().child("Donars");

//        checkIfAddressIsAdded();

        setNameAndStateCity();
        getVolunteerLocation();
        checkAllDonars = new ArrayList<>();
        addUids = new ArrayList<>();
    }
    private void checkIfProfileIsAdded(){
        volunteerDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(!snapshot.child("username").exists()){
                        Toast.makeText(TakeAwayFood.this, "Set your profile first", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(TakeAwayFood.this,ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkIfAddressIsAdded(){
        volunteerDataRef.child("Address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Toast.makeText(TakeAwayFood.this, "Set your address first", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),AddressActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    checkIfProfileIsAdded();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setNameAndStateCity(){

        volunteerDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("username").exists()){
                        String str_state = snapshot.child("state").getValue(String.class);
                        String str_city = snapshot.child("city").getValue(String.class);

                        citystate = str_city + "_" + str_state;
                        Query query1 = donarDataRef.orderByChild("statecity").equalTo(citystate);

                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot donorSnapshot : snapshot.getChildren()) {
                                    checkAllDonars.add(donorSnapshot.getKey());
                                }
                                for (int i = 0; i < checkAllDonars.size(); i++){
                                    getDonarLocation(checkAllDonars.get(i));
                                }
//                Toast.makeText(TakeAwayFood.this, "second", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error
                            }

                        });

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setDialog(){
        builder = new AlertDialog.Builder(this);
        view = getLayoutInflater().inflate(R.layout.searching_restaurants_dialogbox,null);
        cancel = view.findViewById(R.id.btn_cancel);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private static double distance(double lat_VOL, double lon_VOL, double lat_D, double lon_D){
        double latDist = Math.toRadians(lat_D - lat_VOL);
        double lonDist = Math.toRadians(lon_D - lon_VOL);
        double a = Math.sin(latDist / 2) * Math.sin(latDist / 2)
                + Math.cos(Math.toRadians(lat_VOL)) * Math.cos(Math.toRadians(lat_D))
                * Math.sin(lonDist  / 2) * Math.sin(lonDist / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIUS_OF_EARTH_KM * c;
    }

    private void getDonarLocation(String donarUid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Details");
        reference.child(donarUid).child("Address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String donarAddress = snapshot.child("userAddress").getValue(String.class);
//                Toast.makeText(TakeAwayFood.this, donarAddress, Toast.LENGTH_SHORT).show();
                try {
                    List<Address> addresses = geocoder.getFromLocationName(donarAddress, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address location = addresses.get(0);
                        latitude_DONAR = location.getLatitude();
                        longitude_DONAR = location.getLongitude();

                        // calculating nearby donars
                        double dist = distance(latitude_VOLUNTEER,longitude_VOULNTEER,latitude_DONAR,longitude_DONAR);
                        if(dist <= maxDistance){
                            dialog.dismiss();
                            getRestaurantUsername(donarUid);
                        }
                    } else {
                        Toast.makeText(TakeAwayFood.this, "Donar Address not found", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(TakeAwayFood.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getVolunteerLocation(){
        volunteerDataRef.child("Address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String volunteerAddress = snapshot.child("userAddress").getValue(String.class);
                try {
                    List<Address> addresses = geocoder.getFromLocationName(volunteerAddress, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address location = addresses.get(0);
                        latitude_VOLUNTEER = location.getLatitude();
                        longitude_VOULNTEER = location.getLongitude();
                    } else {
                        Toast.makeText(TakeAwayFood.this, "Volunteer Address not found", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(TakeAwayFood.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void getRestaurantUsername(String addUids){
        DatabaseReference checkFromDonar = FirebaseDatabase.getInstance().getReference().child("Details");
        itemList = new ArrayList<>();
            checkFromDonar.child(addUids).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        String str_res = snapshot.child("restaurant").getValue(String.class);
                        itemList.add(new HandlerRecyclerViewClass_VOLUNTEER(str_res,addUids));
                        intitRecyclerview();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void intitRecyclerview(){
        recyclerView = findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new Adapter_VOLUNTEER(itemList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new Adapter_VOLUNTEER.OnItemClickListener() {

            @Override
            public void onItemClick(int position,String id) {
                Intent intent = new Intent(getApplicationContext(),viewItemsForVolunteer.class);
                intent.putExtra("id",id);
                startActivity(intent);
            }

            @Override
            public void onClickDeleteItem(int position) {

            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle("Available Restaurants");
    }
}