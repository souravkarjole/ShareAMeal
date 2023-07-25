package com.example.project2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.github.dhaval2404.imagepicker.ImagePicker;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.utilities.Validation;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GiveAwayFood extends AppCompatActivity {

    String str_title,str_description,str_feedCount,str_time_period;
    String user_ID;
    String randomId;
    Toolbar toolbar;
    Spinner time_spinner,time_period_spinner;

    Uri uri;
    ImageView setImage,small_imageview;
    EditText title,description;
    TextView addLocation;
    TextView mSelectedTextView = null;
    TextView one,two,three,four,five;
    EditText feedCountOther;
    LinearLayout giveAwayFood_layout;
    Button addBtn;

    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_away_food);

        toolbar = findViewById(R.id.toolbar_giveawayfood_backpressed);
        setSupportActionBar(toolbar);

        setImage = findViewById(R.id.giveAwayFood_setImage);
        small_imageview = findViewById(R.id.small_imageview);
        time_spinner = findViewById(R.id.time_spinner);
        time_period_spinner = findViewById(R.id.time_period_spinner);
        title = findViewById(R.id.giveAwayFood_title);
        description = findViewById(R.id.giveAwayFood_description);
        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        five = findViewById(R.id.five);
        feedCountOther = findViewById(R.id.giveAwayFood_othercount);
        giveAwayFood_layout = findViewById(R.id.giveAwayFood_layout);
        addBtn = findViewById(R.id.giveAwayFood_addFood);

        setFeedCountCheck();
        setSpinnerColour();
        setLayoutclick();
        addFoodImage();


        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        user_ID = mUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Details").child(mUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference().child("Donars").child(mUser.getUid());

        checkIfAddressIsAdded();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Validation()){
                    checkIfProfileIsAdded();

                    DatabaseReference secondRef = FirebaseDatabase.getInstance().getReference().child("Donars").child(mUser.getUid());
                    setNameAndStateCity(secondRef);
                    addFoodListing(secondRef);
                }
            }
        });
    }

    private void addFoodListing(DatabaseReference secondRef){
        initialize_details();
         secondRef.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 String order = secondRef.child("FoodListing").push().getKey();
                 Map<String, Object> updateData = new HashMap<>();
                 updateData.put("order",order);
                 updateData.put("imageview",uri.toString());
                 updateData.put("title",str_title);
                 updateData.put("description",str_description);
                 updateData.put("feedcount",str_feedCount);
                 String combineTime = time_spinner.getSelectedItem().toString() + "_" + time_period_spinner.getSelectedItem().toString();
                 updateData.put("pickupitem",combineTime);

                 randomId = secondRef.child("FoodListing").push().getKey();
                 assert randomId != null;
                 secondRef.child("FoodListing").child(randomId).setValue(updateData);
                 uploadImageInStorage();
                 Toast.makeText(GiveAwayFood.this, "Successfully added", Toast.LENGTH_SHORT).show();
                 onBackPressed();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
    }

    private void uploadImageInStorage(){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Compress the image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // adjust the compression quality as per your requirement
        byte[] data = baos.toByteArray();
        StorageReference imageRef = storage.getReference().child("FoodImages").child(user_ID).child(randomId).child("image.jpg");
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Image upload successful, do something here if needed
            Log.d(TAG, "Image upload successful");
        });

        // Add a failure listener to the UploadTask
        uploadTask.addOnFailureListener(exception -> {
            // Image upload failed, handle the error here
            Log.e(TAG, "Error uploading image to Firebase Storage", exception);
        });
    }

    private void setNameAndStateCity(DatabaseReference secondRef){
        Map<String, Object> updateData = new HashMap<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("username").exists()){
                        String str_state = snapshot.child("state").getValue(String.class);
                        String str_city = snapshot.child("city").getValue(String.class);

                        String stateCity = str_city + "_" + str_state;
                        updateData.put("username", snapshot.child("username").getValue(String.class));
                        updateData.put("statecity",stateCity);
                        secondRef.updateChildren(updateData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkIfAddressIsAdded(){
        databaseReference.child("Address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Toast.makeText(GiveAwayFood.this, "Set your address first", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),AddressActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkIfProfileIsAdded(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(!snapshot.child("username").exists()){
                        Toast.makeText(GiveAwayFood.this, "Set your profile first", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GiveAwayFood.this,ProfileActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initialize_details(){
        str_title = title.getText().toString();
        str_description = description.getText().toString();
        if(mSelectedTextView != null) {
            str_feedCount = mSelectedTextView.getText().toString();
        }else{
            str_feedCount = feedCountOther.getText().toString();
        }
    }

    private void addFoodImage(){
        setImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(GiveAwayFood.this)
                        .crop(22f,20f)
                        .start(10);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        uri = data.getData();

        if(uri != null){
            setImage.setImageURI(uri);
        }else{
            setImage.setImageResource(R.drawable.photo_camera);
        }

        small_imageview.setVisibility(View.INVISIBLE);
    }


    private boolean Validation(){
        initialize_details();
        if(uri == null){
            small_imageview.setVisibility(View.VISIBLE);
            small_imageview.setColorFilter(Color.RED);
            small_imageview.setImageResource(R.drawable.error);
            return false;
        }
        if (str_title.isEmpty()) {
            title.setError("Please fill field");
            title.requestFocus();
            return false;
        }

        if (str_description.isEmpty()) {
            description.setError("Please fill field");
            description.requestFocus();
            return false;
        }

        if (str_feedCount.isEmpty()) {
            feedCountOther.setError("Either select one / write the count");
            feedCountOther.requestFocus();
            return false;
        }

        if (time_spinner.getSelectedItem().equals("time")) {
            ((TextView)time_spinner.getSelectedView()).setError("Select the time");
            time_spinner.requestFocus();
            return false;
        }

        if (time_period_spinner.getSelectedItem().equals("-")) {
            ((TextView)time_period_spinner.getSelectedView()).setError("Select time period");
            time_period_spinner.requestFocus();
            return false;
        }

        return true;
    }

    private void setLayoutclick(){
        giveAwayFood_layout.setOnClickListener(new View.OnClickListener() {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public void onClick(View view) {
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(giveAwayFood_layout.getWindowToken(), 0);
                    title.clearFocus();
                    description.clearFocus();
                    feedCountOther.clearFocus();
                }
            }
        });
        feedCountOther.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(mSelectedTextView != null){
                    mSelectedTextView.setSelected(false);
                    mSelectedTextView = null;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setFeedCountCheck(){
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedCountOther.clearFocus();

                if(mSelectedTextView == one){
                    mSelectedTextView.setSelected(false);
                    one.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);

                    }
                    if(!feedCountOther.getText().toString().isEmpty()){
                        feedCountOther.setText("");

                    }
                    one.setSelected(true);
                    mSelectedTextView = one;
                }
            }
        });

        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedCountOther.clearFocus();
                if(mSelectedTextView == two){
                    mSelectedTextView.setSelected(false);
                    two.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);
                    }
                    if(!feedCountOther.getText().toString().isEmpty()){
                        feedCountOther.setText("");

                    }
                    two.setSelected(true);
                    mSelectedTextView = two;
                }
            }
        });
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedCountOther.clearFocus();
                if(mSelectedTextView == three){
                    mSelectedTextView.setSelected(false);
                    three.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);
                    }
                    if(!feedCountOther.getText().toString().isEmpty()){
                        feedCountOther.setText("");

                    }
                    three.setSelected(true);
                    mSelectedTextView = three;
                }
            }
        });
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedCountOther.clearFocus();
                if(mSelectedTextView == four){
                    mSelectedTextView.setSelected(false);
                    four.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);
                    }
                    if(!feedCountOther.getText().toString().isEmpty()){
                        feedCountOther.setText("");

                    }
                    four.setSelected(true);
                    mSelectedTextView = four;
                }
            }
        });
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedCountOther.clearFocus();
                if(mSelectedTextView == five){
                    mSelectedTextView.setSelected(false);
                    five.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);
                    }
                    if(!feedCountOther.getText().toString().isEmpty()){
                        feedCountOther.setText("");

                    }
                    five.setSelected(true);
                    mSelectedTextView = five;
                }
            }
        });
    }

    private void setSpinnerColour(){
        time_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(Color.parseColor("#707070"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        time_period_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(Color.parseColor("#707070"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}