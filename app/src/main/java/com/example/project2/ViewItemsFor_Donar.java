package com.example.project2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ViewItemsFor_Donar extends AppCompatActivity {
    int pos;
    String str_title,str_description,str_feedCount;
    String user_ID,phone_Num;

    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference databaseReference;

    TextView title, item_pickUptime, item_description, item_feedcount, item_address, item_phoneno;
    ImageView item_backgroundImage;
    Button item_editBtn;

    Uri uri;
    ImageView edit_setImage,edit_small_imageview;
    EditText edit_title, edit_description, edit_othercount;
    TextView edit_one, edit_two, edit_three, edit_four, edit_five;
    Button edit_makeChanges;
    TextView mSelectedTextView = null;
    LinearLayout edit_fooditemLayout;
    Spinner edit_time_spinner, edit_time_period_spinner;

    @SuppressLint("UseSupportActionBar")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_items_for_donar);
//        changeState();

        title = findViewById(R.id.item_title);
        item_pickUptime = findViewById(R.id.item_pickUptime);
        item_description = findViewById(R.id.item_description);
        item_feedcount = findViewById(R.id.item_feedcount);
        item_address = findViewById(R.id.item_address);
        item_phoneno = findViewById(R.id.item_phoneno);
        item_editBtn = findViewById(R.id.item_editBtn);
        item_backgroundImage = findViewById(R.id.item_backgroundImage);


        Toolbar toolbar = findViewById(R.id.item_toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        pos = Integer.parseInt(intent.getStringExtra("position"));
        String user_id = intent.getStringExtra("user_id");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        assert mUser != null;
        if(user_id != null){
            user_ID = user_id;
            item_editBtn.setVisibility(View.INVISIBLE);
        }else {
            user_ID = mUser.getUid();

        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Donars").child(user_ID);

        fetch_Phone();
        fetchdetails_VIEW(pos);

        item_editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(ViewItemsFor_Donar.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.bottomsheet_donar_foodedit);
                dialog.show();

                ViewGroup.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 650, dialog.getWindow().getContext().getResources().getDisplayMetrics());
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,layoutParams.height);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);

                edit_title = dialog.findViewById(R.id.edit_title);
                edit_description = dialog.findViewById(R.id.edit_description);
                edit_othercount = dialog.findViewById(R.id.edit_othercount);
                edit_one = dialog.findViewById(R.id.edit_one);
                edit_two = dialog.findViewById(R.id.edit_two);
                edit_three = dialog.findViewById(R.id.edit_three);
                edit_four = dialog.findViewById(R.id.edit_four);
                edit_five = dialog.findViewById(R.id.edit_five);
                edit_makeChanges = dialog.findViewById(R.id.edit_makeChange);
                edit_fooditemLayout = dialog.findViewById(R.id.edit_fooditemLayout);
                edit_setImage = dialog.findViewById(R.id.edit_Image);
                edit_small_imageview =  dialog.findViewById(R.id.edit_small_imageview);
                edit_time_spinner = dialog.findViewById(R.id.edit_time_spinner);
                edit_time_period_spinner = dialog.findViewById(R.id.edit_time_period_spinner);

                edit_title.setText(title.getText());
                edit_description.setText(item_description.getText());

                setLayoutclick();
                setSpinnerColour();
                setFeedCountCheck();
                addFoodImage();

                edit_makeChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Validation()) {
                            editDetails_DIALOG(pos);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    private void fetch_Phone(){
        DatabaseReference refbase = FirebaseDatabase.getInstance().getReference("Details").child(user_ID);
        refbase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists() && snapshot.child("phoneNum").exists()){

                    phone_Num = snapshot.child("phoneNum").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void changeState(){
        CoordinatorLayout nestedScrollView = findViewById(R.id.s);
        AppBarLayout appBarLayout = findViewById(R.id.appbarlayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    // fully collapsed
                    nestedScrollView.setBackgroundColor(Color.WHITE);
                } else {
                    // not collapsed
                    nestedScrollView.setBackgroundColor(Color.BLACK);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        setTitle("Added Food");
    }

    private void fetchdetails_VIEW(int pos){
        DatabaseReference refbase = FirebaseDatabase.getInstance().getReference("Details").child(user_ID).child("Address");

        refbase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    item_address.setText(snapshot.child("userAddress").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child("FoodListing").orderByChild("order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int i = 0;
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        if(i == pos){
                            title.setText(dataSnapshot.child("title").getValue(String.class));
                            item_phoneno.setText(phone_Num);
                            item_description.setText(dataSnapshot.child("description").getValue(String.class));
                            item_feedcount.setText(dataSnapshot.child("feedcount").getValue(String.class));
                            item_pickUptime.setText(dataSnapshot.child("pickupitem").getValue(String.class));
                            Uri imageUri = Uri.parse(dataSnapshot.child("imageview").getValue(String.class));
                            item_backgroundImage.setImageURI(imageUri);
                            break;
                        }
                        i++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void editDetails_DIALOG(int pos){
        initialize_details();

        databaseReference.child("FoodListing").orderByChild("order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int i = 0;
                    String randomId = "";
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        if(i == pos){
                            randomId = dataSnapshot.getKey();
                            break;
                        }
                        i++;
                    }

                    assert randomId != null;
                    if(!randomId.isEmpty()){
                        Map<String, Object> updateData = new HashMap<>();
                        String order = databaseReference.child("FoodListing").push().getKey();
                        updateData.put("order",order);
                        updateData.put("imageview",uri.toString());
                        updateData.put("title",str_title);
                        updateData.put("description",str_description);
                        updateData.put("feedcount",str_feedCount);
                        String combineTime = edit_time_spinner.getSelectedItem().toString() + "_" + edit_time_period_spinner.getSelectedItem().toString();;
                        updateData.put("pickupitem",combineTime);

                        databaseReference.child("FoodListing").child(randomId).updateChildren(updateData);
                        updateInTheFireBaseStorage(randomId);
                        Toast.makeText(ViewItemsFor_Donar.this, "Successfully added", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void updateInTheFireBaseStorage(String ramdomID){
        String user_ID = mUser.getUid();
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
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("FoodImages").child(user_ID).child(ramdomID).child("image.jpg");
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

    private void initialize_details(){
        str_title = edit_title.getText().toString();
        str_description = edit_description.getText().toString();
        if(mSelectedTextView != null) {
            str_feedCount = mSelectedTextView.getText().toString();
        }else{
            str_feedCount = edit_othercount.getText().toString();
        }
    }


    private void addFoodImage(){
        edit_setImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(ViewItemsFor_Donar.this)
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
            edit_setImage.setImageURI(uri);
        }else{
            edit_setImage.setImageResource(R.drawable.photo_camera);
        }

        edit_small_imageview.setVisibility(View.INVISIBLE);
    }

    private boolean Validation(){
        initialize_details();
        if(uri == null){
            edit_small_imageview.setVisibility(View.VISIBLE);
            edit_small_imageview.setColorFilter(Color.RED);
            edit_small_imageview.setImageResource(R.drawable.error);
            return false;
        }
        if (str_title.isEmpty()) {
            edit_title.setError("Please fill field");
            edit_title.requestFocus();
            return false;
        }

        if (str_description.isEmpty()) {
            edit_description.setError("Please fill field");
            edit_description.requestFocus();
            return false;
        }

        if (str_feedCount.isEmpty()) {
            edit_othercount.setError("Either select one / write the count");
            edit_othercount.requestFocus();
            return false;
        }

        if (edit_time_spinner.getSelectedItem().equals("time")) {
            ((TextView)edit_time_spinner.getSelectedView()).setError("Select the time");
            edit_time_spinner.requestFocus();
            return false;
        }

        if (edit_time_period_spinner.getSelectedItem().equals("-")) {
            ((TextView)edit_time_period_spinner.getSelectedView()).setError("Select time period");
            edit_time_period_spinner.requestFocus();
            return false;
        }

        return true;
    }

    private void setLayoutclick(){
        edit_fooditemLayout.setOnClickListener(new View.OnClickListener() {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public void onClick(View view) {
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(edit_fooditemLayout.getWindowToken(), 0);
                    edit_title.clearFocus();
                    edit_description.clearFocus();
                    edit_othercount.clearFocus();
                }
            }
        });
        edit_othercount.addTextChangedListener(new TextWatcher() {
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
        edit_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_othercount.clearFocus();

                if(mSelectedTextView == edit_one){
                    mSelectedTextView.setSelected(false);
                    edit_one.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);

                    }
                    if(!edit_othercount.getText().toString().isEmpty()){
                        edit_othercount.setText("");

                    }
                    edit_one.setSelected(true);
                    mSelectedTextView = edit_one;
                }
            }
        });

        edit_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_othercount.clearFocus();
                if(mSelectedTextView == edit_two){
                    mSelectedTextView.setSelected(false);
                    edit_two.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);
                    }
                    if(!edit_othercount.getText().toString().isEmpty()){
                        edit_othercount.setText("");

                    }
                    edit_two.setSelected(true);
                    mSelectedTextView = edit_two;
                }
            }
        });
        edit_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_othercount.clearFocus();
                if(mSelectedTextView == edit_three){
                    mSelectedTextView.setSelected(false);
                    edit_three.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);
                    }
                    if(!edit_othercount.getText().toString().isEmpty()){
                        edit_othercount.setText("");

                    }
                    edit_three.setSelected(true);
                    mSelectedTextView = edit_three;
                }
            }
        });
        edit_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_othercount.clearFocus();
                if(mSelectedTextView == edit_four){
                    mSelectedTextView.setSelected(false);
                    edit_four.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);
                    }
                    if(!edit_othercount.getText().toString().isEmpty()){
                        edit_othercount.setText("");

                    }
                    edit_four.setSelected(true);
                    mSelectedTextView = edit_four;
                }
            }
        });
        edit_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_othercount.clearFocus();
                if(mSelectedTextView == edit_five){
                    mSelectedTextView.setSelected(false);
                    edit_five.setSelected(false);
                    mSelectedTextView = null;
                }else {
                    if (mSelectedTextView != null) {
                        mSelectedTextView.setSelected(false);
                    }
                    if(!edit_othercount.getText().toString().isEmpty()){
                        edit_othercount.setText("");

                    }
                    edit_five.setSelected(true);
                    mSelectedTextView = edit_five;
                }
            }
        });
    }

    private void setSpinnerColour(){
        edit_time_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(Color.parseColor("#707070"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        edit_time_period_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(Color.parseColor("#707070"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


}