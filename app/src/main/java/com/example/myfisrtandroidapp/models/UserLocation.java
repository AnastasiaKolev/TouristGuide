package com.example.myfisrtandroidapp.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class UserLocation {

    private User user;
    private GeoPoint geo_point;
    private @ServerTimestamp Date timestamp;

    private UserPreferences preferences;

    public UserLocation(User user, GeoPoint geo_point, Date timestamp, UserPreferences userPreferences) {
        this.user = user;
        this.geo_point = geo_point;
        this.timestamp = timestamp;
        this.preferences = userPreferences;
    }

    public UserLocation() {

    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user=" + user +
                ", geo_point=" + geo_point +
                ", timestamp=" + timestamp +
                '}';
    }

}
