package com.example.myfisrtandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myfisrtandroidapp.models.UserPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private final String TAG = "ProfileActivity";
    public static final String EXTRA_MESSAGE = "com.example.myfisrtandroidapp.MESSAGE";

    private FirebaseFirestore mDb;
    private UserPreferences mUserPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDb = FirebaseFirestore.getInstance();
        getUserPrefDetails();
    }

    public void backButton(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, mUserPreferences.getUser().getUsername());
        startActivity(intent);
    }

    private void getUserPrefDetails() {
        DocumentReference preferencesRef = mDb.collection(getString(R.string.collection_user_preferences))
                .document(FirebaseAuth.getInstance().getUid());

        preferencesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: successfully set the user client.");
                    mUserPreferences = task.getResult().toObject(UserPreferences.class);
                    mUserPreferences.getUser();
                    mUserPreferences.getPreferences();
                    setUserProfile(mUserPreferences);
                }
            }
        });
    }

    private void setUserProfile(UserPreferences userPreferences) {
        TextView mUserName = findViewById(R.id.nameDetails);
        mUserName.setText(userPreferences.getUser().getUsername());

        TextView mEmail = findViewById(R.id.emailDetails);
        mEmail.setText(userPreferences.getUser().getEmail());

        TextView mMainName = findViewById(R.id.nameMain);
        mMainName.setText(userPreferences.getUser().getUsername());

        TextView mMainEmail = findViewById(R.id.emailMain);
        mMainEmail.setText(userPreferences.getUser().getEmail());

        TextView mPlacesLists = findViewById(R.id.prefDetails);
        String preferences = TextUtils.join(", ", userPreferences.getPreferences());
        mPlacesLists.setText(preferences);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
