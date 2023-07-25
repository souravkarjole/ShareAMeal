package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddressActivity extends AppCompatActivity {
    String str_flatNo,str_area,str_landmark,str_pinPostalCode,str_city,str_state;
    boolean isButtonClicked = true;

    private Geocoder geocoder;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference databaseReference;

    View view;
    AlertDialog dialog;
    AlertDialog.Builder builder;

    TextView dialog_text;
    TextView useCurrentLocation;
    EditText address_flatNo,address_area,address_landmark,address_pincode_postal_code,address_city,address_state;
    AppCompatButton add_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());


        builder = new AlertDialog.Builder(this);
        view = getLayoutInflater().inflate(R.layout.address_dialogbox,null);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Details").child(mUser.getUid());

        useCurrentLocation = findViewById(R.id.address_currentLocation);
        address_flatNo = findViewById(R.id.address_flatNo);
        address_area = findViewById(R.id.address_Area);
        address_landmark = findViewById(R.id.address_landmark);
        address_pincode_postal_code = findViewById(R.id.address_pincode);
        address_city = findViewById(R.id.address_city);
        address_state = findViewById(R.id.address_state);
        add_address = findViewById(R.id.address_addAddress);
        dialog_text = view.findViewById(R.id.address_text);


        checkAddedAddress();

        add_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Validation()){
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("userAddress", str_flatNo + ", " + str_area + ", " + str_landmark + ", " + str_pinPostalCode
                    + ", " + str_city + ", " + str_state);
                    databaseReference.child("Address").updateChildren(updateData);
                    Toast.makeText(AddressActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });

        useCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(AddressActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                    return;
                }else {
                    Toast.makeText(AddressActivity.this, "inside", Toast.LENGTH_SHORT).show();
                    LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                            .setWaitForAccurateLocation(false)
                            .setMinUpdateIntervalMillis(10000)
                            .setMaxUpdateDelayMillis(100)
                            .build();
//                    LocationRequest locationRequest = LocationRequest.create()
//                            .setInterval(10000)
//                            .setFastestInterval(5000)
//                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

                    Toast.makeText(AddressActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                }
            }
        });
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
                if (location != null) {

                    // Do something with the location
                    isButtonClicked = false;
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    settingAddress(latitude,longitude);
                }
        }
    };


    private void checkAddedAddress(){
        initialize_input();

        databaseReference.child("Address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    builder.setView(view);
                    dialog = builder.create();
                    dialog.show();
                    dialog_text.setText(snapshot.child("userAddress").getValue(String.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Toast.makeText(this, "i am groot", Toast.LENGTH_SHORT).show();
                if (location != null) {
                    // Use the location
                    Toast.makeText(this, "i am groot accepted", Toast.LENGTH_SHORT).show();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    // Do something with latitude and longitude
                    settingAddress(latitude,longitude);

                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                // Permission denied
                // Handle accordingly
            }
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Update UI with new location
            Toast.makeText(AddressActivity.this, "updatined", Toast.LENGTH_SHORT).show();
            settingAddress(location.getLatitude(),location.getLongitude());
        }
    };

    private boolean Validation(){
        initialize_input();

        if (str_flatNo.isEmpty()) {
            address_flatNo.setError("Please fill field");
            address_flatNo.requestFocus();
            return false;
        }

        if (str_area.isEmpty()) {
            address_area.setError("Please fill field");
            address_area.requestFocus();
            return false;
        }

        if (str_pinPostalCode.isEmpty()) {
            address_pincode_postal_code.setError("Please fill field");
            address_pincode_postal_code.requestFocus();
            return false;
        }

        if(!validPincode(str_pinPostalCode)){
            address_pincode_postal_code.setError("must be 6 digits");
            address_pincode_postal_code.requestFocus();
            return false;
        }

        if (str_city.isEmpty()) {
            address_city.setError("Please fill field");
            address_city.requestFocus();
            return false;
        }

        if (str_state.isEmpty()) {
            address_state.setError("Please fill field");
            address_state.requestFocus();
            return false;
        }
        return true;
    }

    private void initialize_input(){
        str_flatNo = address_flatNo.getText().toString();
        str_area = address_area.getText().toString();
        str_landmark = address_landmark.getText().toString();
        str_pinPostalCode = address_pincode_postal_code.getText().toString();
        str_city = address_city.getText().toString();
        str_state = address_state.getText().toString();

    }
    private void settingAddress(double lat, double lon){
        try {
//            Toast.makeText(AddressActivity.this, "yooo", Toast.LENGTH_SHORT).show();
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                address_city.setText(address.getLocality());
//                address_city.setText(address.getLocale());
                address_state.setText(address.getAdminArea());
                address_pincode_postal_code.setText(address.getPostalCode());
                String str = "";
                updateProfileIfExists(address.getLocality());
                if(address.getSubLocality() != null){
                    str += address.getSubLocality() + ", ";
                }
                if(address.getSubThoroughfare() != null){
                    str += address.getSubThoroughfare() + ", ";
                }
                if(address.getThoroughfare() != null){
                    str += address.getThoroughfare();
                }

                address_area.setText(str);
            } else {
                Toast.makeText(this, "Something went wrong.\nPlease try again", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProfileIfExists(String city){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("username").exists()){
                        databaseReference.child("city").setValue(city);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private boolean validPincode(String pincode){
        if(pincode.length() == 6){
            return true;
        }
        return false;
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        this.finish();
//    }
}