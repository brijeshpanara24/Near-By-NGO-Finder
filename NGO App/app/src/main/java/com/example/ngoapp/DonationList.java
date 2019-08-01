package com.example.ngoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngoapp.DataClass.DonationDetails;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class DonationList extends AppCompatActivity {

    public static boolean sortByName = false;
    public static boolean sortByAmount = false;
    public static boolean sortByDate = true;

    public static ArrayList<DonationDetails> allDonations = new ArrayList<>();

    FirebaseApp ngoApp,userApp;

    Toolbar toolbar;
    TextView tvMessage;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_list);

        init();
        updatePage();
    }

    public void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Donation History");

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        ngoApp = FirebaseApp.getInstance("ngoApp");
        userApp = FirebaseApp.getInstance("userApp");
    }

    private void updatePage() {
        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        getDonationList();
    }

    private void getDonationList() {

        if(isInternet()==false) {
            tvMessage.setText("No Internet Connection");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        String NGO = FirebaseAuth.getInstance(ngoApp).getCurrentUser().getEmail().replaceAll("[^A-Za-z0-9]", "-");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.DonationDetails)).child(NGO);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allDonations.clear();
                for(DataSnapshot donationSnapshot : dataSnapshot.getChildren()) {
                    DonationDetails temp = donationSnapshot.getValue(DonationDetails.class);
                    allDonations.add(temp);
                }
                progressDialog.dismiss();
                setRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(DonationList.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//------------------------------- recycler view --------------------------------------------//

    public void setRecyclerView() {

        if(allDonations.size()==0) {
            tvMessage.setText("No One Donated You Yet !!!");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        ArrayList<DonationDetails> recyclerList = getSortedList();
        recyclerView.setLayoutManager(new LinearLayoutManager(DonationList.this));
        recyclerView.setAdapter(new DataAdapter(DonationList.this, recyclerList));
    }

    public ArrayList<DonationDetails> getSortedList() {
        if(sortByName) {
            Collections.sort(allDonations, new Comparator<DonationDetails>() {
                @Override
                public int compare(DonationDetails o1, DonationDetails o2) {
                    if(o1.getUserName().toLowerCase().compareTo(o2.getUserName().toLowerCase())<0)
                        return -1;
                    else
                        return 1;
                }
            });
        } else if(sortByAmount) {
            Collections.sort(allDonations, new Comparator<DonationDetails>() {
                @Override
                public int compare(DonationDetails o1, DonationDetails o2) {
                    int amount1 = Integer.parseInt(o1.getAmount());
                    int amount2 = Integer.parseInt(o2.getAmount());
                    if(amount1>amount2)
                        return -1;
                    else
                        return 1;
                }
            });
        } else if(sortByDate) {
            Collections.sort(allDonations, new Comparator<DonationDetails>() {
                @Override
                public int compare(DonationDetails o1, DonationDetails o2) {
                    long d1 = Date.parse(o1.getDateTime());
                    long d2 = Date.parse(o2.getDateTime());
                    if(d1>d2)
                        return -1;
                    else
                        return 1;
                }
            });
        }
        return allDonations;
    }

    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

        ArrayList<DonationDetails> allDonation;
        Context context;

        public DataAdapter(Context context, ArrayList<DonationDetails> allDonation) {
            this.allDonation = allDonation;
            this.context = context;
        }

        @Override
        public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.donation_recycler_view, parent, false);
            return new DataAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            DonationDetails details = allDonation.get(position);
            holder.tvUser.setText("Donor : "+details.getUserName());
            holder.tvAmount.setText("Amount : "+details.getAmount()+" RS");
            holder.tvDate.setText("Date : "+details.getDateTime());
        }

        @Override
        public int getItemCount() {
            return allDonation.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView tvUser,tvAmount,tvDate;

            public ViewHolder(View itemView) {
                super(itemView);

                tvUser = (TextView) itemView.findViewById(R.id.tvUser);
                tvAmount = (TextView) itemView.findViewById(R.id.tvAmount);
                tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.donation_option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.updatePage)
            updatePage();
        else {
            sortByName = false;
            sortByAmount = false;
            sortByDate = false;
            if(item.getItemId() == R.id.sortByName)
                sortByName=true;
            else if(item.getItemId() == R.id.sortByAmount)
                sortByAmount=true;
            else if(item.getItemId() == R.id.sortByDate)
                sortByDate=true;
            setRecyclerView();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }
}
