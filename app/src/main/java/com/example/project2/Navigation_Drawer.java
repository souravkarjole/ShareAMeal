package com.example.project2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Navigation_Drawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    ImageView profile_photo;
    String user_ID;
    String str_res = "no res";
    String str_user = "no user";
    String random_id;
    String str_type;
    Toolbar toolbar;
    RecyclerView recyclerView;
    List<HandlerRecyclerViewClass_DONAR> itemList;
    LinearLayoutManager linearLayoutManager;
    Adapter_DONAR adapter;

    private ImageView reload;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toogle;
    private NavigationView navigationView;
    private int selectedItemId;
    private Map_fragment map_fragment;

    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference secondRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        map_fragment = new Map_fragment();
        toolbar = findViewById(R.id.toolbarr);
        setSupportActionBar(toolbar);


        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        assert mUser != null;
        user_ID = mUser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Details").child(mUser.getUid());
        secondRef = firebaseDatabase.getReference("Donars").child(mUser.getUid());

        reload = toolbar.findViewById(R.id.reload);
        progressBar = findViewById(R.id.progressBarr);
        progressBar.setVisibility(View.INVISIBLE);
        floatingActionButton = findViewById(R.id.add);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        profile_photo = findViewById(R.id.profile_photo);


        Menu menu = navigationView.getMenu();
        MenuItem tools = menu.findItem(R.id.business);
        SpannableString s = new SpannableString(tools.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance44), 0, s.length(), 0);
        tools.setTitle(s);

        toogle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toogle);
        toogle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        str_type = intent.getStringExtra("type");

        if (str_type.equals("donar")){
            reload.setVisibility(View.VISIBLE);
            checkIfFoodAreAdded();
        }else{
            reload.setVisibility(View.GONE);
        }

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                checkIfFoodAreAdded();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (str_type.equals("donar")) {
                    showBottomDialog_Donar();
                } else {
                    showBottomDialog_Volunteer();
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                bottomNavigationView.setVisibility(View.VISIBLE);

                int id = item.getItemId();
//                Toast.makeText(Navigation_Drawer.this, "fragment_" + id, Toast.LENGTH_SHORT).show();
                selectedItemId = bottomNavigationView.getSelectedItemId();

                // Check if the selected item is already the current fragment
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

                if (currentFragment != null && currentFragment.getTag() != null && currentFragment.getTag().equals("fragment_" + id)) {
                    // Show false when pooping out all the fragments
                    // Toast.makeText(Navigation_Drawer.this, "false", Toast.LENGTH_SHORT).show();
                    return false;
                }
                floatingActionButton.setVisibility(View.VISIBLE);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        toolbar.setVisibility(View.VISIBLE);
                        popEveryFragment();
                        break;
                    case R.id.nav_account:
                        hideToolBar();
                        replaceFragment(new Account_fragment(), id);
                        floatingActionButton.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.help:
                        Uri uri = Uri.parse("https://help.olioex.com/");
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        break;
                }
                return true;
            }
        });
    }
    private void setProfilePhoto(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.child("profilePhoto").exists()){
                    Uri uri = Uri.parse(snapshot.child("profilePhoto").getValue(String.class));
                    Glide.with(Navigation_Drawer.this)
                            .load(uri)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profile_photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkIfFoodAreAdded(){
       secondRef.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()) {
                   getAlltheFoods(secondRef);
               }else{
                   progressBar.setVisibility(View.GONE);
               }
           }
           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

private void getAlltheFoods(DatabaseReference secondRef) {
    setUsernameRestaurant();
    itemList = new ArrayList<>();

    secondRef.child("FoodListing").orderByChild("order").addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                List<HandlerRecyclerViewClass_DONAR> sortedList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String feedcount = dataSnapshot.child("feedcount").getValue(String.class);
                    String order = dataSnapshot.child("order").getValue(String.class);
                    random_id = dataSnapshot.getKey();

                    assert random_id != null;
                    StorageReference imageRef = storageRef.child("FoodImages").child(user_ID).child(random_id).child("image.jpg");

                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HandlerRecyclerViewClass_DONAR item = new HandlerRecyclerViewClass_DONAR(order,uri, feedcount, str_res, str_user);
                            sortedList.add(item);
                            if (sortedList.size() == snapshot.getChildrenCount()) {
                                Collections.sort(sortedList, new Comparator<HandlerRecyclerViewClass_DONAR>() {
                                    @Override
                                    public int compare(HandlerRecyclerViewClass_DONAR o1, HandlerRecyclerViewClass_DONAR o2) {
                                        return (o1.getOrder().compareTo(o2.getOrder()));
                                    }
                                });
                                itemList.addAll(sortedList);
                                intitRecyclerview();
                                progressBar.setVisibility(View.GONE);
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
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
}


    private void setUsernameRestaurant(){
        databaseReference.addValueEventListener(new ValueEventListener() {
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
        adapter = new Adapter_DONAR(itemList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new Adapter_DONAR.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(Navigation_Drawer.this,ViewItemsFor_Donar.class);
                intent.putExtra("position",String.valueOf(position));
                startActivity(intent);
            }

            @Override
            public void onClickDeleteItem(int position) {
                deleteItem(position);
                itemList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });
    }


    private void deleteItem(int pos){
        DatabaseReference fordelete = secondRef.child("FoodListing");
        secondRef.child("FoodListing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int i = 0;
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        if(i == pos){
                            random_id = dataSnapshot.getKey();
                            deleteFromFireStorage();
                            assert random_id != null;
                            fordelete.child(random_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {}
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Navigation_Drawer.this, "failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        }
                       i++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void deleteFromFireStorage(){
        StorageReference imageRef = storage.getReference().child("FoodImages").child(user_ID).child(random_id).child("image.jpg");
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error deleting file
            }
        });
    }

    private void popEveryFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();

        for (int i = 0; i < count; i++) {
            fragmentManager.popBackStackImmediate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle(str_type.toUpperCase());
        floatingActionButton.setVisibility(View.VISIBLE);
        setProfilePhoto();
        progressBar.setVisibility(View.INVISIBLE);
//        if(str_type.equals("donar")) {
//            checkIfFoodAreAdded();
//        }
    }

    public void replaceFragment(Fragment fragment, int id){
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment f = fragmentManager.findFragmentByTag("fragment_" + id);

        FragmentTransaction fragmentTransaction;
//        Toast.makeText(this, "added", Toast.LENGTH_SHORT).show();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment, "fragment_" + id);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        selectedItemId = bottomNavigationView.getSelectedItemId();

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

        if (currentFragment != null && currentFragment.getTag() != null && currentFragment.getTag().equals("fragment_" + id)) {
            // Show false when pooping out all the fragments
            // Toast.makeText(Navigation_Drawer.this, "false", Toast.LENGTH_SHORT).show();
            return false;
        }


        switch(item.getItemId()){
            case R.id.nav_listing:
                floatingActionButton.setVisibility(View.INVISIBLE);
                bottomNavigationView.setVisibility(View.INVISIBLE);
                replaceFragment(new MyListing_fragment(),id);
                break;
            case R.id.nav_home:
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
                break;
//            case R.id.nav_setting:
//                replaceFragment(new SettingsFragment());
////                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new SettingsFragment()).commit();
//                break;
            case R.id.nav_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String body = "Download this app";
                intent.putExtra(Intent.EXTRA_TEXT,body);
                startActivity(Intent.createChooser(intent,"Share using"));
                break;
//            case R.id.nav_map:
//                bottomNavigationView.setSelectedItemId(R.id.nav_map);
////                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new UsersNearMeFragment()).commit();
//                break;
//            case R.id.nav_about:
//                replaceFragment(new AboutUsFragment());
////                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new AboutUsFragment()).commit();
//                break;
//            case R.id.nav_listing:
//                replaceFragment(new MyListingsFragment());
////                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new MyListingsFragment()).commit();
//                break;
            case R.id.help:
                Uri uri = Uri.parse("https://help.olioex.com/");
                startActivity(new Intent(Intent.ACTION_VIEW,uri));
                break;
            case R.id.nav_logout:
                SignOut();
                Toast.makeText(Navigation_Drawer.this,"Logged Out!", Toast.LENGTH_LONG).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        item.setCheckable(true);
        return true;
    }

    public void hideToolBar() {
        toolbar.setVisibility(View.INVISIBLE);
    }

    private void SignOut(){
        GoogleSignInOptions googleSignInOptions;
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);
//
        googleSignInClient.signOut();
        mAuth.signOut();
        startActivity(new Intent(Navigation_Drawer.this,LoginActivity.class));
        finish();
    }

    private void showBottomDialog_Donar(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout_donar);

        LinearLayout giveAway_layout = dialog.findViewById(R.id.giveAway_layout);
        LinearLayout feast_food_layout = dialog.findViewById(R.id.feast_food_layout);
        LinearLayout help_layout = dialog.findViewById(R.id.help_layout);


        giveAway_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(getApplicationContext(),GiveAwayFood.class);
                startActivity(intent);
            }
        });

        feast_food_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
//                Intent intent = new Intent(getApplicationContext(),FeastGiving.class);
//                startActivity(intent);
            }
        });

        help_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://help.olioex.com/article/100-what-am-i-not-allowed-to-share-on-olio");
                startActivity(new Intent(Intent.ACTION_VIEW,uri));
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    private void showBottomDialog_Volunteer(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout_volunteer);

        LinearLayout takeAway_layout = dialog.findViewById(R.id.takeAway_layout);
        LinearLayout enjoy_feast_food_layout = dialog.findViewById(R.id.enjoy_feast_food_layout);
        LinearLayout help_layout_volunteer = dialog.findViewById(R.id.help_layout_volunteer);

        takeAway_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                floatingActionButton.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(),TakeAwayFood.class);
                startActivity(intent);
            }
        });

        enjoy_feast_food_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
//                Intent intent = new Intent(getApplicationContext(),FeastGiving.class);
//                startActivity(intent);
            }
        });

        help_layout_volunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://help.olioex.com/article/100-what-am-i-not-allowed-to-share-on-olio");
                startActivity(new Intent(Intent.ACTION_VIEW,uri));
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    public void onBackPressed() {
        // Get the current fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.frame_layout);

        if(fm.getBackStackEntryCount() > 1){
            fm.popBackStackImmediate();

            switch (selectedItemId) {
                case R.id.nav_listing:
                    navigationView.setCheckedItem(R.id.nav_listing);
                    navigationView.getMenu().findItem(R.id.nav_listing).setChecked(true);
                    break;
//                case R.id.nav_map:
//                    bottomNavigationView.setSelectedItemId(R.id.nav_map);
//                    bottomNavigationView.getMenu().findItem(R.id.nav_map).setChecked(true);
//                    break;
                case R.id.nav_account:
                    bottomNavigationView.setSelectedItemId(R.id.nav_account);
                    bottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
                    break;
                // Add more cases for each item in your bottom navigation
            }

        } else if(fm.getBackStackEntryCount() == 1){
            fm.popBackStackImmediate();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            navigationView.setCheckedItem(R.id.nav_home);
        }else{
            super.onBackPressed();
        }

    }

}