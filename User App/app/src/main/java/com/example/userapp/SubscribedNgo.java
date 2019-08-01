package com.example.userapp;

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

import com.example.userapp.DataClass.SubscriptionDetails;
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

public class SubscribedNgo extends AppCompatActivity {

    FirebaseApp userApp;

    Toolbar toolbar;
    TextView tvMessage;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_ngo);

        init();
    }

    private void init() {

        userApp = FirebaseApp.getInstance("userApp");

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Subscribed NGOs");

        updatePage();
    }

    private void updatePage() {
        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        getAllSubscribedNgoDetails();
    }

    private void getAllSubscribedNgoDetails() {

        if(isInternet()==false) {
            tvMessage.setText("No Internet Connection");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        final ArrayList<SubscriptionDetails> allSubscribedNgoDetails = new ArrayList<>();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Subscribed NGO Details .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        String USER = FirebaseAuth.getInstance(userApp).getCurrentUser().getEmail().replaceAll("[^A-Za-z0-9]", "-");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(userApp).getReference(getString(R.string.SubscriptionDetails)).child(USER);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allSubscribedNgoDetails.clear();
                for(DataSnapshot ngoSnapshot : dataSnapshot.getChildren()) {
                    SubscriptionDetails temp = ngoSnapshot.getValue(SubscriptionDetails.class);
                    allSubscribedNgoDetails.add(temp);
                }
                progressDialog.dismiss();
                setRecyclerView(allSubscribedNgoDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(SubscribedNgo.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//------------------------------- recycler view --------------------------------------------//

    public void setRecyclerView(ArrayList<SubscriptionDetails> recyclerList) {

        if(recyclerList.size()==0) {
            tvMessage.setText("You Have not Subscribed Any NGOs !!!");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerList = getSortedList(recyclerList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new DataAdapter(SubscribedNgo.this, recyclerList));
    }

    public ArrayList<SubscriptionDetails> getSortedList(ArrayList<SubscriptionDetails> recyclerList) {
        Collections.sort(recyclerList, new Comparator<SubscriptionDetails>() {
            @Override
            public int compare(SubscriptionDetails o1, SubscriptionDetails o2) {
                if(o1.getNgoName().toLowerCase().compareTo(o2.getNgoName().toLowerCase())<0)
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ngo_recycler_view, parent, false);
            return new DataAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);

            holder.tvName.setText(ngoList.get(position).getNgoName());
            holder.tvDistance.setVisibility(View.GONE);
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

                Intent intent = new Intent(SubscribedNgo.this,NgoDashboard.class);
                intent.putExtra("ngoEmail",ngoList.get(getLayoutPosition()).getNgoEmail());
                startActivity(intent);
                return;
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
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;
        return false;
    }
}
