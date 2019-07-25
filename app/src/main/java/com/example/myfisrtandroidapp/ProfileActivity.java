package com.example.myfisrtandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.TextViewCompat;

import com.example.myfisrtandroidapp.models.User;
import com.example.myfisrtandroidapp.models.UserPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private final String TAG = "ProfileActivity";

    private FirebaseFirestore mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UserPreferences userPreferences = UserPreferences.getInstance();
        ArrayList<String> all = userPreferences.getPreferences();

        TextView mPlacesLists = findViewById(R.id.prefDetails);;
        StringBuilder allPref = new StringBuilder();
        Log.d(TAG, "dgkkbfdgskb" + all);

        for (String s : all) {
            if (allPref.length() > 0) {
                allPref.append(", ");
            }
            allPref.append(s);
        }

        Log.d(TAG, "0000000" + allPref.toString());
        mPlacesLists.setText(allPref.toString());
        TextViewCompat.setAutoSizeTextTypeWithDefaults(mPlacesLists, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        mDb = FirebaseFirestore.getInstance();
        getUserDetails();
    }

    public void backButton(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void getUserDetails() {
        DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid());

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: successfully set the user client.");
                    User user = task.getResult().toObject(User.class);
                    ((UserClient)getApplicationContext()).getUser();
                    TextView mUserName = findViewById(R.id.nameDetails);
                    TextView mEmail = findViewById(R.id.emailDetails);
                    TextView mMainName = findViewById(R.id.nameMain);
                    TextView mMainEmail = findViewById(R.id.emailMain);
                    mUserName.setText(user.getUsername());
                    mMainName.setText(user.getUsername());
                    mEmail.setText(user.getEmail());
                    mMainEmail.setText(user.getEmail());
                }
            }
        });
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
