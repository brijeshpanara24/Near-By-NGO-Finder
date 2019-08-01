package com.example.userapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userapp.DataClass.NgoDetails;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FindNearByNgoList extends AppCompatActivity {

    static boolean sortByName = false;
    static boolean sortByDistance = true;
    static long distanceRange = -1;

    static LatLng mCurrentLocation = null;
    LocationListener locationListener;

    Toolbar toolbar;
    TextView tvMessage;
    RecyclerView recyclerView;

    FloatingActionButton btnShowOnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_near_by_ngo_list);

        init();
    }

    private void init() {

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        btnShowOnMap = (FloatingActionButton) findViewById(R.id.btnShowOnMap);
        btnShowOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindNearByNgoList.this, FindNearByNgoMap.class);
                startActivity(intent);
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Near By Ngo");

        updatePage();
    }

    private void updatePage() {
        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection !!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mCurrentLocation!=null)
            getAllNgoList();
        else
            getDeviceLocation();
    }

    private void getDeviceLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getAllNgoList();
            return;
        }
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastKnownLocation != null)
            mCurrentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        getAllNgoList();

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                mCurrentLocation=new LatLng(location.getLatitude(),location.getLongitude());
                locationManager.removeUpdates(locationListener);
                Log.d("WTF", "WTF");
                getAllNgoList();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) { }

            public void onProviderEnabled(String provider) { }

            public void onProviderDisabled(String provider) { }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void getAllNgoList() {

        if(isInternet()==false) {
            tvMessage.setText("No Internet Connection");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        final Database database = Database.getInstance();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Data .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        FirebaseApp ngoApp = FirebaseApp.getInstance("ngoApp");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.NgoDetails));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ngoSnapshot : dataSnapshot.getChildren()) {
                    NgoDetails temp = ngoSnapshot.getValue(NgoDetails.class);
                    database.addNgo(temp);
                }
                progressDialog.dismiss();
                setRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(FindNearByNgoList.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//------------------------------- recycler view --------------------------------------------//

    public void setRecyclerView() {

        ArrayList<NgoDetails> recyclerList = getFilteredList();
        recyclerList = getSortedList(recyclerList);

        if(recyclerList.size()==0) {
            tvMessage.setText("No Ngo Found !!!");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(FindNearByNgoList.this));
        recyclerView.setAdapter(new DataAdapter(FindNearByNgoList.this, recyclerList));
    }

    private ArrayList<NgoDetails> getFilteredList() {
        ArrayList<NgoDetails> allNgos = Database.getInstance().getNgoList();
        ArrayList<NgoDetails> recyclerList = new ArrayList<>();

        if(mCurrentLocation!=null && distanceRange!=-1) {
            for(int i=0;i<allNgos.size();i++) {
                NgoDetails ngo = allNgos.get(i);
                LatLng latLng = new LatLng(Double.parseDouble(ngo.getLatitude()) , Double.parseDouble(ngo.getLongitude()));
                double distance = (double)Math.round(getDistance(mCurrentLocation,latLng)) / 1000d;

                if(distance<=distanceRange)
                    recyclerList.add(ngo);
            }
        }
        else
            recyclerList = allNgos;
        return  recyclerList;
    }

    public ArrayList<NgoDetails> getSortedList(ArrayList<NgoDetails> recyclerList) {
        if(sortByDistance && mCurrentLocation!=null) {
            Collections.sort(recyclerList, new Comparator<NgoDetails>() {
                @Override
                public int compare(NgoDetails o1, NgoDetails o2) {
                    double dist1 = getDistance(mCurrentLocation,new LatLng( Double.parseDouble(o1.getLatitude()), Double.parseDouble(o1.getLongitude()) ));
                    double dist2 = getDistance(mCurrentLocation,new LatLng( Double.parseDouble(o2.getLatitude()), Double.parseDouble(o2.getLongitude()) ));

                    if(dist1<=dist2)
                        return -1;
                    else
                        return 1;
                }
            });
        } else if(sortByName) {
            Collections.sort(recyclerList, new Comparator<NgoDetails>() {
                @Override
                public int compare(NgoDetails o1, NgoDetails o2) {
                    String name1 = o1.getName().toLowerCase();
                    String name2 = o2.getName().toLowerCase();
                    if (name1.compareTo(name2) < 0)
                        return -1;
                    else
                        return 1;
                }
            });
        }
        return recyclerList;
    }

    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

        ArrayList<NgoDetails> ngoList;
        Context context;

        public DataAdapter(Context context, ArrayList<NgoDetails> ngoList) {
            this.ngoList = ngoList;
            this.context = context;
        }

        @Override
        public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ngo_recycler_view, parent, false);
            return new DataAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);

            NgoDetails ngo = ngoList.get(position);
            holder.tvName.setText("NGO Name : "+ngo.getName());
            if(mCurrentLocation!=null) {
                LatLng latLng = new LatLng( Double.parseDouble(ngo.getLatitude()) , Double.parseDouble(ngo.getLongitude()) );
                double distance = (double)Math.round(getDistance(mCurrentLocation,latLng)) / 1000d;
                holder.tvDistance.setText( "Distance : "+ Double.toString(distance) + " Km" );
                holder.tvDistance.setVisibility(View.VISIBLE);
            } else {

                holder.tvDistance.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return ngoList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView tvName,tvDistance;

            public ViewHolder(View itemView) {
                super(itemView);

                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvDistance = (TextView) itemView.findViewById(R.id.tvDistance);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(FindNearByNgoList.this,NgoDashboard.class);
                intent.putExtra("ngoEmail",ngoList.get(getLayoutPosition()).getEmail());
                startActivity(intent);
                return;
            }
        }
    }

//--------------------------------- Option Menu ----------------------------------------------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ngo_list_option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.updatePage) {
            distanceRange = -1;
            updatePage();
        } else if(item.getItemId() == R.id.filter) {
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.ngo_list_custom_dialog, null);
            final EditText etRange = alertLayout.findViewById(R.id.etRange);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Range Of Distance (In Km)");
            alert.setView(alertLayout);
            alert.setPositiveButton("Apply", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String temp = etRange.getText().toString();
                    if(temp.isEmpty())
                        Toast.makeText(FindNearByNgoList.this, "Please Enter Range !!!", Toast.LENGTH_SHORT).show();
                    else {
                        try {
                            distanceRange = Long.parseLong(temp);
                            setRecyclerView();
                        } catch (Exception e) {
                            Log.d("TAG", "unable to parse");
                        }
                    }
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();

        } else {
            sortByName = false;
            sortByDistance = false;
            if (item.getItemId() == R.id.sortByName) {
                sortByName = true;
            } else if (item.getItemId() == R.id.sortByDistance) {
                sortByDistance = true;
            }
            setRecyclerView();
        }

        return super.onOptionsItemSelected(item);
    }

//------------------------------- Helper Functions ------------------------------------------//

    public double getDistance(LatLng pos1,LatLng pos2) {

        Location start = new Location("start");
        start.setLatitude(pos1.latitude);
        start.setLongitude(pos1.longitude);

        Location end = new Location("end");
        end.setLatitude(pos2.latitude);
        end.setLongitude(pos2.longitude);

        return (1.25*start.distanceTo(end));
    }

    public boolean isInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }
}
