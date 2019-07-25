package com.example.myfisrtandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.fragment.app.FragmentActivity;

import com.example.myfisrtandroidapp.models.UserPreferences;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UserPlacesActivity extends FragmentActivity {
    private final String TAG = "Chips Example";
    private HashMap<String, Boolean> mPreferences = new HashMap<>();
    private ArrayList<String> aPreferences = new ArrayList<>();
    private Set<String> setPref = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

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
                    mPreferences.replace("museum", true);
                    setPref.add("museum");
                }
                if (church_chip.isChecked()) {
                    mPreferences.replace("church", true);
                    setPref.add("church");
                }
                if (art_gallery_chip.isChecked()) {
                    mPreferences.replace("art_gallery", true);
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
        UserPreferences userPreferences = UserPreferences.getInstance();
        setPref = mPreferences.keySet();

        for (String s : setPref) {
            aPreferences.add(s);
        }

        userPreferences.setPreferences(aPreferences);
        Log.d(TAG,"Preferences LIST: "  + userPreferences.getPreferences().toString());

        Intent intent = new Intent(this, ProfileActivity.class);

        startActivity(intent);
    }
}
