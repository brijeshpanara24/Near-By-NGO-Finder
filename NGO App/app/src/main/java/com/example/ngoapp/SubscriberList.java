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
import com.example.ngoapp.DataClass.SubscriptionDetails;
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

public class SubscriberList extends AppCompatActivity {

    FirebaseApp ngoApp;

    Toolbar toolbar;
    TextView tvMessage;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber_list);

        init();
    }

    private void init() {

        ngoApp = FirebaseApp.getInstance("ngoApp");

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Subscribed Users");

        updatePage();
    }

    private void updatePage() {
        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        getAllSubscribedUserDetails();
    }

    private void getAllSubscribedUserDetails() {

        if(isInternet()==false) {
            tvMessage.setText("No Internet Connection");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        final ArrayList<SubscriptionDetails> allSubscribedUserDetails = new ArrayList<>();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Subscribed USER Details .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        String NGO = FirebaseAuth.getInstance(ngoApp).getCurrentUser().getEmail().replaceAll("[^A-Za-z0-9]", "-");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.SubscriptionDetails)).child(NGO);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allSubscribedUserDetails.clear();
                for(DataSnapshot ngoSnapshot : dataSnapshot.getChildren()) {
                    SubscriptionDetails temp = ngoSnapshot.getValue(SubscriptionDetails.class);
                    allSubscribedUserDetails.add(temp);
                }
                progressDialog.dismiss();
                setRecyclerView(allSubscribedUserDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(SubscriberList.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


//------------------------------- recycler view --------------------------------------------//


    public void setRecyclerView(ArrayList<SubscriptionDetails> recyclerList) {

        if(recyclerList.size()==0) {
            tvMessage.setText("No One Subscribed Your NGO !!!");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerList = getSortedList(recyclerList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new DataAdapter(SubscriberList.this, recyclerList));
    }

    public ArrayList<SubscriptionDetails> getSortedList(ArrayList<SubscriptionDetails> recyclerList) {
        Collections.sort(recyclerList, new Comparator<SubscriptionDetails>() {
            @Override
            public int compare(SubscriptionDetails o1, SubscriptionDetails o2) {
                if(o1.getUserName().toLowerCase().compareTo(o2.getUserName().toLowerCase())<0)
                    return -1;
                else
                    return 1;
            }
        });
        return recyclerList;
    }

    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

        ArrayList<SubscriptionDetails> ngoList;
        Context context;

        public DataAdapter(Context context, ArrayList<SubscriptionDetails> ngoList) {
            this.ngoList = ngoList;
            this.context = context;
        }

        @Override
        public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_recycler_view, parent, false);
            return new DataAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            holder.tvName.setText(ngoList.get(position).getUserName());
        }

        @Override
        public int getItemCount() {
            return ngoList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvName;
            public ViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
            }
        }
    }


//--------------------------------- Option Menu ----------------------------------------------//


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.updatePage)
            updatePage();
        return super.onOptionsItemSelected(item);
    }

    public boolean isInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;
        return false;
    }

}
