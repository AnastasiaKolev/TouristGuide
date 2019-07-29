package com.example.myfisrtandroidapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.myfisrtandroidapp.R;
import com.example.myfisrtandroidapp.models.PolylineData;
import com.example.myfisrtandroidapp.models.User;
import com.example.myfisrtandroidapp.models.UserLocation;
import com.example.myfisrtandroidapp.models.UserPreferences;
import com.example.myfisrtandroidapp.utils.NearbyPlaces;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener {

    private static final String TAG = "MapActivity";
    private Location currentLocation;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private FirebaseFirestore mDb;
    private UserPreferences mUserPreferences;
    private UserLocation mUserLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();

    //widgets
    private EditText mSearchText;
    private ImageView mGps;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
        mMap.setOnPolylineClickListener(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mSearchText = findViewById(R.id.input_search);
        mGps = findViewById(R.id.ic_gps);


        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationPermission();
        getUserPrefDetails();
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
                }
            }
        });
    }

    private void init() {
        Log.d(TAG, "init: initializing");

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        hideSoftKeyboard();
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void getUserDetails() {
        if (mUserLocation == null) {
            mUserLocation = new UserLocation();
            DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        User user = task.getResult().toObject(User.class);
                        mUserLocation.setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        } else {
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mUserLocation.setGeo_point(geoPoint);
                    mUserLocation.setTimestamp(null);
                    saveUserLocation();
                }
            }
        });

    }

    private void saveUserLocation() {

        if (mUserLocation != null) {
            DocumentReference locationRef = mDb
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + mUserLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + mUserLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
                getUserDetails();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private int ProximityRadius = 2000;

    public void onMuseumClick(View v) {
        String museum = "museum", church = "church", art_gallery = "art_gallery";
        String aquarium = "aquarium", zoo = "zoo", amusement_park = "amusement_park";
        String city_hall = "city_hall", mosque = "mosque", park = "park", synagogue = "synagogue";
        Object transferData[] = new Object[2];
        NearbyPlaces getNearbyPlaces = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces1 = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces2 = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces3 = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces4 = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces5 = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces6 = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces7 = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces8 = new NearbyPlaces();
        NearbyPlaces getNearbyPlaces9 = new NearbyPlaces();


        double latitude = 59.934280199999996, longitude = 30.335098600000002;
        /*latitude = currentLocation.getLatitude();
        longitude = currentLocation.getLongitude();*/

        switch (v.getId()) {
            case R.id.museumsNearby:
                mMap.clear();
                for (String s : mUserPreferences.getPreferences()) {
                    if (s.equals("museum")) {
                        String url = getUrl(latitude, longitude, museum);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Museums...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Museums...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("church")) {
                        String url = getUrl(latitude, longitude, church);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces2.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Churches...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Churches...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("art_gallery")) {
                        String url = getUrl(latitude, longitude, art_gallery);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces3.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Art Gallery...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Art Gallery...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("aquarium")) {
                        String url = getUrl(latitude, longitude, aquarium);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces4.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Aquarium...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Aquarium...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("zoo")) {
                        String url = getUrl(latitude, longitude, zoo);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces5.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Zoo...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Zoo...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("amusement_park")) {
                        String url = getUrl(latitude, longitude, amusement_park);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces6.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Amusement park...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Amusement park...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("city_hall")) {
                        String url = getUrl(latitude, longitude, city_hall);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces7.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby City Hall...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby City Hall...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("mosque")) {
                        String url = getUrl(latitude, longitude, mosque);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces8.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Mosque...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Mosque...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("park")) {
                        String url = getUrl(latitude, longitude, park);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces1.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Parks...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Parks...", Toast.LENGTH_SHORT).show();
                    }
                    if (s.equals("synagogue")) {
                        String url = getUrl(latitude, longitude, synagogue);
                        transferData[0] = mMap;
                        transferData[1] = url;

                        getNearbyPlaces9.execute(transferData);
                        Toast.makeText(this, "Searching for Nearby Synagogue...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Showing Nearby Synagogue...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

        mMap.setOnMarkerClickListener(marker -> {
            Toast.makeText(MapsActivity.this, "hey!", Toast.LENGTH_SHORT).show();
            if(mPolyLinesData.size() > 0) {
                tripDialog(marker);
            } else {
                dialogInfo(marker); //get value from marker
            }
            return true;
        });
    }

    private String getUrl(double latitide, double longitude, String nearbyPlace) {
        StringBuilder googleURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location=" + latitide + "," + longitude);
        googleURL.append("&radius=" + ProximityRadius);
        googleURL.append("&type=" + nearbyPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key=" + getString(R.string.google_map_api_key));

        Log.d("GoogleMapsActivity", "url = " + googleURL.toString());

        return googleURL.toString();
    }

    public void dialogInfo(final Marker marker) {
        final View layout = View.inflate(this, R.layout.activity_dialog, null);

        final TextView title = layout.findViewById(R.id.myTitle);
        title.setText(marker.getTitle());

        final TextView snippet = layout.findViewById(R.id.mySnippet);
        snippet.setText("\n\nDetermine route?");

        final RatingBar ratingBar = layout.findViewById(R.id.ratingBar);
        if (marker.getSnippet().equals("")) {
            ratingBar.setRating(0);
        } else {
            ratingBar.setRating(Float.parseFloat(marker.getSnippet()));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(layout);

        builder.setMessage("")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        calculateDirections(marker);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private GeoApiContext mGeoApiContext;
    private double previousLat, previousLon;

    private void calculateDirections(Marker marker) {
        Log.d(TAG, "calculateDirections: calculating directions.");
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_map_api_key))
                    .build();
        }

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );

        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        if(mPolyLinesData.size() > 0) {
            directions.origin(
                    new com.google.maps.model.LatLng(
                            previousLat,
                            previousLon
                    )
            );
        } else {
            directions.origin(
                    new com.google.maps.model.LatLng(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude()
                    )
            );

        }

        previousLat = marker.getPosition().latitude;
        previousLon = marker.getPosition().longitude;

        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage());

            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
//                if (mPolyLinesData.size() > 0) {
//                    tripDialog(marker);
//                }

                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                    polyline.setClickable(true);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));
                }
            }
        });
    }

    public void tripDialog(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Add place to the route?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        calculateDirections(marker);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        for (PolylineData polylineData : mPolyLinesData) {
                            polylineData.getPolyline().remove();
                        }
                        mPolyLinesData.clear();
                        mPolyLinesData = new ArrayList<>();
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for (PolylineData polylineData : mPolyLinesData) {
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.linkBlue));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration
                        ));


                marker.showInfoWindow();
            } else {
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }
}
