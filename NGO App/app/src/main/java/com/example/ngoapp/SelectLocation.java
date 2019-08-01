package com.example.ngoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ngoapp.SignIn_SignUp.SignUp;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectLocation extends AppCompatActivity implements OnMapReadyCallback {

    //google map
    private static GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15f;

    //xml
    EditText etSearch;
    Button btnSearch,btnSelectLocation;

    //selected location
    private static LatLng selectedLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(SelectLocation.this);

        init();
    }

    private void init() {
        etSearch = (EditText) findViewById(R.id.etSearch);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { locateAddress(etSearch.getText().toString()); }
        });

        btnSelectLocation = (Button) findViewById(R.id.btnSelectLocation);
        btnSelectLocation.setVisibility(View.GONE);
        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { selectLocation();
            }
        });
    }

    private void selectLocation() {
        if(selectedLocation==null)
            Toast.makeText(SelectLocation.this, "Please Select Location On Map", Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent(SelectLocation.this, SignUp.class);
            intent.putExtra("latitude",Double.toString(selectedLocation.latitude));
            intent.putExtra("longitude",Double.toString(selectedLocation.longitude));
            setResult(1,intent);
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        getDeviceLocation();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title("Selected Location");

                mMap.clear();
                mMap.addMarker(options);
                selectedLocation = latLng;
                btnSelectLocation.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getDeviceLocation() {

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                        moveCamera(latLng,DEFAULT_ZOOM);
                    }else
                        Toast.makeText(SelectLocation.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (SecurityException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void locateAddress(String searchString) {

        Geocoder geocoder = new Geocoder(SelectLocation.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            getDeviceLocation();
        }

        LatLng latLng = null;
        for (int i = 0; i < list.size(); i++) {
            Address address = list.get(i);
            if (address.hasLatitude() && address.hasLongitude()) {
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
                break;
            }
        }

        if (latLng == null)
            Toast.makeText(this, "Failed To Locate Address", Toast.LENGTH_SHORT).show();
        else
            moveCamera(latLng,DEFAULT_ZOOM);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}
