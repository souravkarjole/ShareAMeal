package com.example.project2;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import java.util.Map;
import java.util.Objects;

public class Account_fragment extends Fragment {


    EditText acc_PhoneNo;
    TextView acc_userName;
    TextView acc_profile;
    TextView acc_email;
    TextView acc_address;
    ImageView profile_Photo;
    String uid;
    Uri uri;

    BottomNavigationView bottomNavigationView;
    LinearLayout linearLayout;


    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference databaseReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_account_fragment, container, false);
        profile_Photo = view.findViewById(R.id.profile_photo);
        acc_PhoneNo = view.findViewById(R.id.account_phoneNo);
        acc_profile = view.findViewById(R.id.account_profile);
        acc_userName = view.findViewById(R.id.account_userName);
        acc_email = view.findViewById(R.id.account_email);
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
        acc_address = view.findViewById(R.id.account_address);

        linearLayout = view.findViewById(R.id.linear_touch);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Details").child(mUser.getUid());

        fetchProfilePhoto();
        String str_state = "maharashtra";
        String str_city = "solapur";

        String stateCity = str_state + "_" + str_city;

//        rootRef.child("donar5").child("statecity").setValue(stateCity);
//        Query query1 = rootRef.orderByChild("statecity").equalTo(str_state + "_" + str_city);
//
//
//        query1.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot donorSnapshot : snapshot.getChildren()) {
//                    // Retrieve donor information from the snapshot
//                    uid = donorSnapshot.child("uid").getValue(String.class);
//
//                    // etc.
//                    Toast.makeText(getActivity(), uid, Toast.LENGTH_SHORT).show();
//
//                    // Display the donor information in the app
//                    // ...
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Handle error
//            }
//        });



//        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                // Get the children of the root node
//                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
//
//                // Iterate over the children
//                for (DataSnapshot child : children) {
//                    // Get the value of the child node
//                    Object value = child.getValue();
//                    Toast.makeText(getActivity(), "ch: " + child.getKey(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle error
//            }
//        });

        profile_Photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(Account_fragment.this)
                        .crop(22f,20f)
                        .start(10);
            }
        });
        acc_PhoneNo.setText(mUser.getPhoneNumber());
        acc_email.setText(mUser.getEmail());

        acc_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),ProfileActivity.class);
                startActivity(intent);
            }
        });


        linearLayout.setOnClickListener(new View.OnClickListener() {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public void onClick(View view) {

                if (acc_PhoneNo != null) {
                    imm.hideSoftInputFromWindow(acc_PhoneNo.getWindowToken(), 0);
                    acc_PhoneNo.clearFocus();
                }
            }
        });

        acc_PhoneNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    bottomNavigationView.setVisibility(View.GONE);
                }else{
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });

        acc_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),AddressActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        uri = data.getData();

        if(uri != null){
            Glide.with(Account_fragment.this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_Photo);
            addProfilePhoto();
        }else{
            profile_Photo.setImageResource(R.drawable.profile);
            fetchProfilePhoto();
        }
    }
    private void fetchProfilePhoto(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.child("profilePhoto").exists()){
                    uri = Uri.parse(snapshot.child("profilePhoto").getValue(String.class));
                    Glide.with(Account_fragment.this)
                            .load(uri)
                            .encodeQuality(80)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profile_Photo);
                    addProfilePhoto();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addProfilePhoto(){
        if(uri != null){
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String,Object> objectMap = new HashMap<>();
                    objectMap.put("profilePhoto",uri.toString());
                    databaseReference.updateChildren(objectMap);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            // Show the fragment
            requireView().setVisibility(View.VISIBLE);
            setUserName();
        } else {
            // Hide the fragment
            requireView().setVisibility(View.GONE);
        }
    }

    private void setUserName(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("username").exists()){
                        acc_userName.setText(snapshot.child("username").getValue(String.class));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}