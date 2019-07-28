package com.example.myfisrtandroidapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myfisrtandroidapp.ProfileActivity;
import com.example.myfisrtandroidapp.R;
import com.example.myfisrtandroidapp.UserClient;
import com.example.myfisrtandroidapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditNameActivity extends AppCompatActivity {
    private final String TAG = "EditNameActivity";

    private EditText mName;
    private User mUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        mName = findViewById(R.id.nameEdit);
    }

    public void saveName(View view) {
        mUser = ((UserClient)getApplicationContext()).getUser();
        mUser.setUsername(mName.getText().toString());

        FirebaseFirestore.getInstance()
                .collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid())
                .set(mUser);

        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}
