package com.example.userapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.userapp.DataClass.NgoDetails;
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

import java.util.ArrayList;

public class FindNearByNgoMap extends AppCompatActivity implements OnMapReadyCallback {

    //google map
    private static GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_near_by_ngo_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(FindNearByNgoMap.this);
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
        markAllNgoOnMap();
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
                        Toast.makeText(FindNearByNgoMap.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (SecurityException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void markAllNgoOnMap() {

        ArrayList<NgoDetails> ngoList = Database.getInstance().getNgoList();

        for(int i=0;i<ngoList.size();i++) {

            NgoDetails ngo = ngoList.get(i);
            LatLng latLng = new LatLng( Double.parseDouble(ngo.getLatitude()) , Double.parseDouble(ngo.getLongitude()) );

            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(ngo.getName())
                    .snippet(ngo.getAddress());

            mMap.addMarker(options);
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

}
