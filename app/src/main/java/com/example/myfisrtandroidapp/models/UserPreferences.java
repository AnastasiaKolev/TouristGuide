package com.example.myfisrtandroidapp.models;

import java.util.ArrayList;

public class UserPreferences {
    private User user;
    private ArrayList<String> preferences;

    public UserPreferences(User user, ArrayList<String> preferences) {
        this.user = user;
        this.preferences = preferences;
    }

    public UserPreferences() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(ArrayList<String> mPreferences) {
        this.preferences = mPreferences;
    }
}
