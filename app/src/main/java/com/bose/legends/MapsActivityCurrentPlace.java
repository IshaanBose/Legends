package com.bose.legends;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivityCurrentPlace extends AppCompatActivity implements OnMapReadyCallback
{
    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap map;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_current_place);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        location = new Location("");
        location.setLatitude(getIntent().getExtras().getDouble("latitude"));
        location.setLongitude(getIntent().getExtras().getDouble("longitude"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;

        updateLocationUI();

        map.addMarker(new MarkerOptions()
            .position(new LatLng(location.getLatitude(), location.getLongitude()))
            .title("Currently set location"));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()), DEFAULT_ZOOM));
    }

    private void updateLocationUI()
    {
        if (map == null)
            return;

        try
        {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        }
        catch (SecurityException e)
        {
            Log.d("maps", e.getMessage());
        }
    }
}