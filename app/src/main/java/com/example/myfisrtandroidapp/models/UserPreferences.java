package com.example.myfisrtandroidapp.models;

import java.util.ArrayList;

public class UserPreferences {
    private ArrayList<String> preferences;

    public UserPreferences() {
    }

    private static volatile UserPreferences instance;

    public static UserPreferences getInstance() {
        UserPreferences localInstance = instance;
        if (localInstance == null) {
            synchronized (UserPreferences.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new UserPreferences();
                }
            }
        }
        return localInstance;
    }

    public UserPreferences(ArrayList<String> mPreferences) {
        this.preferences = mPreferences;
    }

    public ArrayList<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(ArrayList<String> mPreferences) {
        this.preferences = mPreferences;
    }
}
