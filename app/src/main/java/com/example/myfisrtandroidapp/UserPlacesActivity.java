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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UserPlacesActivity extends AppCompatActivity {
    private final String TAG = "Chips Example";
    private HashMap<String, Boolean> mPreferences = new HashMap<>();
    private ArrayList<String> aPreferences = new ArrayList<>();
    private Set<String> setPref = new HashSet<>();

    private UserPreferences mUserPreferences;
    private FirebaseFirestore mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        mDb = FirebaseFirestore.getInstance();

        mPreferences.put("museum", true);
        mPreferences.put("church", false);
        mPreferences.put("art_gallery", false);

        /*Filter Chip section*/
        ChipGroup filterChipGroup = findViewById(R.id.filter_chip_group);
        Chip museum_chip = findViewById(R.id.museum_chip);
        Chip church_chip = findViewById(R.id.church_chip);
        Chip art_gallery_chip = findViewById(R.id.art_gallery_chip);
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
            }
        };
        museum_chip.setOnCheckedChangeListener(filterChipListener);
        church_chip.setOnCheckedChangeListener(filterChipListener);
        art_gallery_chip.setOnCheckedChangeListener(filterChipListener);

        museum_chip.setChecked(mPreferences.get("museum"));
    }

    public void savePlaces(View view) {
        if (mUserPreferences == null) {
            mUserPreferences = new UserPreferences();
            //UserPreferences userPreferences = UserPreferences.getInstance();
            setPref = mPreferences.keySet();

            for (String s : setPref) {
                aPreferences.add(s);
            }
//
//            userPreferences.setPreferences(aPreferences);
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
