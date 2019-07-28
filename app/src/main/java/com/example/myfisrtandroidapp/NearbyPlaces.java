package com.example.myfisrtandroidapp;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class NearbyPlaces extends AsyncTask<Object, String, String> {

    private String googleplaceData, url;
    private GoogleMap mMap;

    @Override
    protected String doInBackground(Object... objects)
    {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try
        {
            googleplaceData = downloadUrl.ReadTheURL(url);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return googleplaceData;
    }


    @Override
    protected void onPostExecute(String s)
    {
        List<HashMap<String, String>> nearByPlacesList = null;
        DataParser dataParser = new DataParser();
        nearByPlacesList = dataParser.parse(s);

        DisplayNearbyPlaces(nearByPlacesList);
    }


    private void DisplayNearbyPlaces(List<HashMap<String, String>> nearByPlacesList)
    {
        for (int i=0; i<nearByPlacesList.size(); i++)
        {
            MarkerOptions markerOptions = new MarkerOptions();

            HashMap<String, String> googleNearbyPlace = nearByPlacesList.get(i);
            String nameOfPlace = googleNearbyPlace.get("place_name");
            String vicinity = googleNearbyPlace.get("vicinity");
            String open_hours = googleNearbyPlace.get("open_now");
            String types_string = googleNearbyPlace.get("types");
            String[] types = types_string.split(",");
            double lat = Double.parseDouble(googleNearbyPlace.get("lat"));
            double lng = Double.parseDouble(googleNearbyPlace.get("lng"));

            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            if (open_hours.equals("true")) {
                markerOptions.title(nameOfPlace + "\n" + vicinity + "\nOpen now");
            }
            markerOptions.title(nameOfPlace + "\n" + vicinity);
            markerOptions.snippet("Determine route to " + nameOfPlace + "?");

            float color = BitmapDescriptorFactory.HUE_GREEN;

            for (int j = 0; j < types.length; j++) {
                switch (types[j]) {
                    case "church":
                        color = BitmapDescriptorFactory.HUE_GREEN;
                        j = types.length + 1;
                        break;
                    case "museum":
                        color = BitmapDescriptorFactory.HUE_YELLOW;
                        j = types.length + 1;
                        break;
                    case "art_gallery":
                        color = BitmapDescriptorFactory.HUE_ORANGE;
                        j = types.length + 1;
                        break;
                    case "aquarium":
                        color = BitmapDescriptorFactory.HUE_BLUE;
                        j = types.length + 1;
                        break;
                    case "zoo":
                        color = BitmapDescriptorFactory.HUE_MAGENTA;
                        j = types.length + 1;
                        break;
                    case "amusement_park":
                        color = BitmapDescriptorFactory.HUE_ROSE;
                        j = types.length + 1;
                        break;
                    case "city_hall":
                        color = BitmapDescriptorFactory.HUE_VIOLET;
                        j = types.length + 1;
                        break;
                    case "mosque":
                        color = BitmapDescriptorFactory.HUE_AZURE;
                        j = types.length + 1;
                        break;
                    case "park":
                        color = BitmapDescriptorFactory.HUE_CYAN;
                        j = types.length + 1;
                        break;
                    case "synagogue":
                        color = BitmapDescriptorFactory.HUE_RED;
                        j = types.length + 1;
                        break;
                    default:
                        continue;
                }
            }

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }

}
