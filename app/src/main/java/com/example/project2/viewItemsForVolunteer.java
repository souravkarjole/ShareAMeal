package com.example.project2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class viewItemsForVolunteer extends AppCompatActivity {
    String str_res = "no res";
    String str_user = "no user";
    String random_id;
    String user_ID;

    FloatingActionButton accept_donation;
    Button takeAway_food,cancle_btn;
    Toolbar toolbar;
    RecyclerView recyclerView;
    List<HandlerRecyclerViewClass_VOLUNTEER_ITEMS> itemList;
    LinearLayoutManager linearLayoutManager;
    Adapter_VOLUNTEER_ITEMS adapter;
    ProgressBar progressBar;
    AlertDialog dialog;
    AlertDialog.Builder builder;
    View view_dialogue;
    ConstraintLayout constraintLayout;

    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference databaseReference_DONARS;
    DatabaseReference databaseReference_DETAILS;
    DatabaseReference databaseReference_VOLUNTEERS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_items_for_volunteer);


        builder = new AlertDialog.Builder(this);
        toolbar = findViewById(R.id.toolbarr);
        setSupportActionBar(toolbar);
        toolbar.findViewById(R.id.cardreload).setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBar);
        accept_donation = findViewById(R.id.addBtn);

        view_dialogue = getLayoutInflater().inflate(R.layout.accept_donation_dialogue_box,null);
        takeAway_food = view_dialogue.findViewById(R.id.takeAway_btn);
        cancle_btn = view_dialogue.findViewById(R.id.cancel_button);

        Intent intent = getIntent();
        user_ID = intent.getStringExtra("id");
//        Toast.makeText(this, user_ID, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        databaseReference_DETAILS = FirebaseDatabase.getInstance().getReference("Details").child(user_ID);
        databaseReference_DONARS = FirebaseDatabase.getInstance().getReference().child("Donars").child(user_ID);
        databaseReference_VOLUNTEERS = FirebaseDatabase.getInstance().getReference().child("VolunteerAccepted");
        checkIfFoodAreAdded();
        cancle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        takeAway_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderList_DONARS();
            }
        });
        accept_donation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept_donation.setVisibility(View.INVISIBLE);

                // Remove the view from its current parent

                builder.setView(view_dialogue);
                dialog = builder.create();

                ViewGroup parent = (ViewGroup) view_dialogue.getParent();
                if (parent != null) {
                    parent.removeView(view_dialogue);
                }

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        accept_donation.setVisibility(View.VISIBLE);
                    }
                });
                dialog.show();
                dialog.setCancelable(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle("FoodList");
        accept_donation.setVisibility(View.VISIBLE);
    }

    private void accecptDonation(){
        databaseReference_DETAILS.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Map<String,Object> updateFood = new HashMap<>();
                    String res = snapshot.child("restaurant").getValue(String.class);
                    String phone = snapshot.child("phoneNum").getValue(String.class);

                    updateFood.put("restaurant",res);
                    updateFood.put("phoneNum",phone);

                    databaseReference_VOLUNTEERS.child(user_ID).updateChildren(updateFood);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void orderList_DONARS(){
        databaseReference_DONARS.child("FoodListing").orderByChild("order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        Map<String,Object> updateFood = new HashMap<>();
                        String id = dataSnapshot.getKey();

                        String descrip = dataSnapshot.child("description").getValue(String.class);
                        String feedcount = dataSnapshot.child("feedcount").getValue(String.class);
                        String uri = dataSnapshot.child("imageview").getValue(String.class);
                        String order = dataSnapshot.child("order").getValue(String.class);
                        String pick_up_time = dataSnapshot.child("pickupitem").getValue(String.class);
                        String title = dataSnapshot.child("title").getValue(String.class);

                        updateFood.put("description",descrip);
                        updateFood.put("feedcount",feedcount);
                        updateFood.put("imageview",uri);
                        updateFood.put("order",order);
                        updateFood.put("pickupitem",pick_up_time);
                        updateFood.put("title",title);

                        databaseReference_DONARS.child("AcceptedDonation").child(id).updateChildren(updateFood);
                        dialog.dismiss();
                    }
                    accecptDonation();

                }else{
                    Toast.makeText(viewItemsForVolunteer.this, "Donation is unavailable", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkIfFoodAreAdded(){
        databaseReference_DONARS.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    getAlltheFoods(databaseReference_DONARS);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAlltheFoods(DatabaseReference databaseReference_DONARS){
        setUsernameRestaurant();

        itemList = new ArrayList<>();

        databaseReference_DONARS.child("FoodListing").orderByChild("order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<HandlerRecyclerViewClass_VOLUNTEER_ITEMS> sortedList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String feedcount = dataSnapshot.child("feedcount").getValue(String.class);
                        String order = dataSnapshot.child("order").getValue(String.class);
                        random_id = dataSnapshot.getKey();

                        assert random_id != null;
                        StorageReference imageRef = storageRef.child("FoodImages").child(user_ID).child(random_id).child("image.jpg");

                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                HandlerRecyclerViewClass_VOLUNTEER_ITEMS item = new HandlerRecyclerViewClass_VOLUNTEER_ITEMS(order,uri, feedcount, str_res, str_user);
                                sortedList.add(item);
                                if (sortedList.size() == snapshot.getChildrenCount()) {
                                    Collections.sort(sortedList, new Comparator<HandlerRecyclerViewClass_VOLUNTEER_ITEMS>() {
                                        @Override
                                        public int compare(HandlerRecyclerViewClass_VOLUNTEER_ITEMS o1, HandlerRecyclerViewClass_VOLUNTEER_ITEMS o2) {
                                            return (o1.getOrder().compareTo(o2.getOrder()));
                                        }
                                    });
                                    itemList.addAll(sortedList);
                                    intitRecyclerview();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    accept_donation.setVisibility(View.VISIBLE);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting download URL", e);
                            }
                        });
                    }
                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    accept_donation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUsernameRestaurant(){
        databaseReference_DETAILS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("restaurant").exists() && snapshot.child("username").exists()){
                        str_res = snapshot.child("restaurant").getValue(String.class);
                        str_user = snapshot.child("username").getValue(String.class);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void intitRecyclerview(){
        recyclerView = findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new Adapter_VOLUNTEER_ITEMS(itemList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new Adapter_VOLUNTEER_ITEMS.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(viewItemsForVolunteer.this,ViewItemsFor_Donar.class);
                intent.putExtra("position",String.valueOf(position));
                intent.putExtra("user_id",user_ID);
                startActivity(intent);
            }
        });
    }

}