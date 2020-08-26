package com.example.covid_19map;

import androidx.fragment.app.FragmentActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.covid_19map.Model.Country;
import com.example.covid_19map.Utils.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCircleClickListener {

    private GoogleMap mMap;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        queue = Volley.newRequestQueue(this);

        getCovidCases();
    }

    private void getCovidCases() {


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject records = null;
                        JSONArray data = null;
                        JSONObject countryCode = null;
                        JSONObject oneData = null;
                        String country = "";
                        int cases = 0;
                        double lat;
                        double lon;
                        int i = 0;
                        Country countryClass = new Country();
                        double radius;
                        int maxRadius = 1200000;
                        double c2 = 0.00001120378;
                        double scale;

                        try {
                            records = response;

                                for(String code : Constants.countriesCodes) {

                                    lat = Constants.countriesCoordinates.get(i);
                                    lon = Constants.countriesCoordinates.get(i+1);
                                    i+=2;

                                    countryCode = records.getJSONObject(code);
                                    data = countryCode.getJSONArray("data");
                                    oneData = data.getJSONObject((data.length() - 1));

                                    countryClass.setLat(lat);
                                    countryClass.setLon(lon);
                                    countryClass.setName(countryCode.getString("location"));
                                    countryClass.setCases(oneData.getInt("total_cases"));

                                    Log.d("PRINTMESSAGE", "Codes size: " + Constants.countriesCodes.size() + ", Coordinates size: " +  Constants.countriesCoordinates.size() + ", Lat: " + lat + ", Lon " + lon + ", " + country + ": " + cases );

                                    CircleOptions circleOptions = new CircleOptions();
                                    circleOptions.center(new LatLng(countryClass.getLat(),countryClass.getLon()));

                                    // (1 + c2*(cos(2*f) - 1)) / cos(f) where c2 = 0.00001120378,  at latitude f

                                    lat = Math.toRadians(lat);
                                    scale = ((1+c2*(Math.cos((2*lat)) -1)) / (Math.cos(lat)));
                                    radius = countryClass.getCases()/scale;
                                    if(radius > maxRadius) radius = maxRadius;

                                    circleOptions.radius(radius);
                                    circleOptions.strokeWidth(10.0f);
                                    circleOptions.fillColor(Color.RED);
                                    circleOptions.clickable(true);
                                    Circle circle = mMap.addCircle(circleOptions);
                                    circle.setTag("Country: " + countryClass.getName() + ", cases: " + countryClass.getCases());

                                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(((countryClass.getLat() - (7/scale))), countryClass.getLon()))
                                    .title((String) circle.getTag()).alpha(0f));

                                    circle.setTag(marker);
                                }

                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.getMessage();
            }
        });
        queue.add(jsonObjectRequest);
        Toast.makeText(this, "Please wait, data is loading \nIt may take few minutes", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCircleClickListener(this);
    }


    @Override
    public void onCircleClick(Circle circle) {
        Marker marker1 = (Marker) circle.getTag();
        assert marker1 != null;
        marker1.showInfoWindow();
    }
}
