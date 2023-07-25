package com.example.project2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Map_fragment extends Fragment implements LocationListener {

    private ProgressBar updating_progressBar;
    private boolean isLocationUpdated;
    private Location previousLocation;
    private float MIN_DISTANCE = 1000;

    private LocationManager locationManager;
    private DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String user_id;

    AlertDialog dialog;
    AlertDialog.Builder builder;
    View view2;
    View view;

    private Navigation_Drawer navigation_drawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) navigation_drawer.getSystemService(Context.LOCATION_SERVICE);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        user_id = mUser.getUid();


        databaseReference = FirebaseDatabase.getInstance().getReference("Details").child(user_id);

        checkIfLocationAdded();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_map_fragment, container, false);

        builder = new AlertDialog.Builder(requireActivity());
        view2 = getLayoutInflater().inflate(R.layout.updating_dialogbox,null);
        updating_progressBar = view2.findViewById(R.id.update_progressBar);

        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Navigation_Drawer) {
            navigation_drawer = (Navigation_Drawer) context;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (navigation_drawer != null) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50000, 1000, this);
            } else {
                ActivityCompat.requestPermissions(requireActivity(),new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        100);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isLocationUpdated = false;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (navigation_drawer != null) {
            if (dialog != null && dialog.isShowing()) {

                dialog.dismiss();
                isLocationUpdated = true;
                locationManager.removeUpdates(this);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
            // Save the location to Firebase

        if (previousLocation == null) {
            previousLocation = location;
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("latitude", location.getLatitude());
            updateData.put("longitude", location.getLongitude());
            databaseReference.child("Directions").updateChildren(updateData);
        } else {
            float distance = location.distanceTo(previousLocation);
            if (distance >= MIN_DISTANCE) {
                previousLocation = location;
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("latitude", location.getLatitude());
                updateData.put("longitude", location.getLongitude());
                databaseReference.child("Directions").updateChildren(updateData);
//                requireActivity().onBackPressed();
            }
        }
        if(getActivity() != null){
            requireActivity().onBackPressed();
        }

    }


    private void checkIfLocationAdded() {
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                        builder.setView(view2);
                        dialog = builder.create();
                        ViewGroup parent = (ViewGroup) view2.getParent();
                        if (parent != null) {
                            parent.removeView(view2);
                        }
                        if(!isLocationUpdated) {
                            isLocationUpdated = true;
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}