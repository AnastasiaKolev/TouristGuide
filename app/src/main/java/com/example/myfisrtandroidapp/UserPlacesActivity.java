package com.example.myfisrtandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myfisrtandroidapp.models.User;
import com.example.myfisrtandroidapp.models.UserPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class UserPlacesActivity extends AppCompatActivity {
    private final String TAG = "Chips Example";
    private ArrayList<String> aPreferences = new ArrayList<>();
    private Set<String> setPref = new HashSet<>();

    private UserPreferences mUserPreferences;
    private FirebaseFirestore mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        mDb = FirebaseFirestore.getInstance();

        /*Filter Chip section*/
        ChipGroup filterChipGroup = findViewById(R.id.filter_chip_group);
        Chip museum_chip = findViewById(R.id.museum_chip);
        Chip church_chip = findViewById(R.id.church_chip);
        Chip art_gallery_chip = findViewById(R.id.art_gallery_chip);
        Chip aquarium_chip = findViewById(R.id.aquarium_chip);
        Chip zoo_chip = findViewById(R.id.zoo_chip);
        Chip amusement_park_chip = findViewById(R.id.amusement_park_chip);
        Chip city_hall_chip = findViewById(R.id.city_hall_chip);
        Chip mosque_chip = findViewById(R.id.mosque_chip);
        Chip park_chip = findViewById(R.id.park_chip);
        Chip synagogue_chip = findViewById(R.id.synagogue_chip);

        CompoundButton.OnCheckedChangeListener filterChipListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, buttonView.getId() + "");
                if (museum_chip.isChecked()) {
                    setPref.add("museum");
                }
                if (church_chip.isChecked()) {
                    setPref.add("church");
                }
                if (art_gallery_chip.isChecked()) {
                    setPref.add("art_gallery");
                }
                if (aquarium_chip.isChecked()) {
                    setPref.add("aquarium_chip");
                }
                if (zoo_chip.isChecked()) {
                    setPref.add("zoo_chip");
                }
                if (amusement_park_chip.isChecked()) {
                    setPref.add("amusement_park_chip");
                }
                if (city_hall_chip.isChecked()) {
                    setPref.add("city_hall_chip");
                }
                if (mosque_chip.isChecked()) {
                    setPref.add("mosque_chip");
                }
                if (park_chip.isChecked()) {
                    setPref.add("park_chip");
                }
                if (synagogue_chip.isChecked()) {
                    setPref.add("synagogue_chip");
                }
            }
        };

        museum_chip.setOnCheckedChangeListener(filterChipListener);
        church_chip.setOnCheckedChangeListener(filterChipListener);
        art_gallery_chip.setOnCheckedChangeListener(filterChipListener);
        aquarium_chip.setOnCheckedChangeListener(filterChipListener);
        zoo_chip.setOnCheckedChangeListener(filterChipListener);
        amusement_park_chip.setOnCheckedChangeListener(filterChipListener);
        city_hall_chip.setOnCheckedChangeListener(filterChipListener);
        mosque_chip.setOnCheckedChangeListener(filterChipListener);
        park_chip.setOnCheckedChangeListener(filterChipListener);
        synagogue_chip.setOnCheckedChangeListener(filterChipListener);

        museum_chip.setChecked(true);
    }

    public void savePlaces(View view) {
        if (mUserPreferences == null) {
            mUserPreferences = new UserPreferences();

            for (String s : setPref) {
                aPreferences.add(s);
            }

            Log.d(TAG, "Preferences LIST: " + aPreferences.toString());

            DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        User user = task.getResult().toObject(User.class);
                        mUserPreferences.setUser(user);
                        mUserPreferences.setPreferences(aPreferences);
                        saveUserPreferences();
                    }
                }
            });
        }

        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void saveUserPreferences(){
        if(mUserPreferences != null){
            DocumentReference preferencesRef = mDb
                    .collection(getString(R.string.collection_user_preferences))
                    .document(FirebaseAuth.getInstance().getUid());

            preferencesRef.set(mUserPreferences).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "saveUserPreferences: \ninserted user preferences into database." +
                                aPreferences.toString());
                    }
                }
            });
        }
    }
}
