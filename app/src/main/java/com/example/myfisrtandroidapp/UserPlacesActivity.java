package com.example.myfisrtandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;

public class UserPlacesActivity extends FragmentActivity {
    private final String TAG = "Chips Example";
    private HashMap<String, Boolean> mPreferences = new HashMap<>();

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
            }
        };
        museum_chip.setOnCheckedChangeListener(filterChipListener);
        church_chip.setOnCheckedChangeListener(filterChipListener);
        art_gallery_chip.setOnCheckedChangeListener(filterChipListener);

        museum_chip.setChecked(mPreferences.get("museum"));
        church_chip.setChecked(mPreferences.get("church"));
        art_gallery_chip.setChecked(mPreferences.get("art_gallery"));
    }

    public void savePlaces(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);

        startActivity(intent);

    }
}
