package com.example.userapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userapp.DataClass.EventDetails;
import com.example.userapp.DataClass.NgoDetails;
import com.example.userapp.DataClass.SubscriptionDetails;
import com.example.userapp.DataClass.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NgoDashboard extends AppCompatActivity {

    private static String mNgoEmail = null;
    private static NgoDetails mNgo = null;
    private static UserDetails mUser = null;

    TextView tvName,tvAddress,tvEmail,tvPhoneNumber;

    Toolbar toolbar;
    TextView tvMessage;
    RecyclerView recyclerView;

    FirebaseApp ngoApp,userApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_dashboard);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("NGO Details");

        ngoApp = FirebaseApp.getInstance("ngoApp");
        userApp = FirebaseApp.getInstance("userApp");

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mNgoEmail = getIntent().getStringExtra("ngoEmail");
        updatePage();
    }

    public void updatePage() {
        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        getDetails(mNgoEmail);
    }

    private void getDetails(String ngoEmail) {

        Database database = Database.getInstance();

        mNgo = Database.getInstance().getNgoDetails(ngoEmail);
        mUser = database.getUser();

        if(mNgo==null) {
            getNgoDetails(ngoEmail);
            return;
        }

        if(mUser==null) {
            getUserDetails(FirebaseAuth.getInstance(userApp).getCurrentUser().getEmail());
            return;
        }

        init();
    }

    private void getUserDetails(String userEmail) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try{ progressDialog.show(); }
        catch(Exception e) { return; }

        userEmail = userEmail.replaceAll("[^A-Za-z0-9]", "-");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(userApp).getReference(getString(R.string.UserDetails)).child(userEmail);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                mUser = dataSnapshot.getValue(UserDetails.class);
                Database.getInstance().setUser(mUser);
                init();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(NgoDashboard.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNgoDetails(String ngoEmail) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try{ progressDialog.show(); }
        catch(Exception e) { return; }

        ngoEmail = ngoEmail.replaceAll("[^A-Za-z0-9]", "-");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.NgoDetails)).child(ngoEmail);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                mNgo = dataSnapshot.getValue(NgoDetails.class);
                Database.getInstance().addNgo(mNgo);
                init();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(NgoDashboard.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {

        tvName = (TextView) findViewById(R.id.tvName);
        tvName.setText("Name : "+mNgo.getName());

        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvAddress.setText("Address : "+mNgo.getAddress());

        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvEmail.setText("Email : "+mNgo.getEmail());

        tvPhoneNumber = (TextView) findViewById(R.id.tvPhoneNumber);
        tvPhoneNumber.setText("Phone Number : "+mNgo.getPhone_number());

        updateAllEvents();
    }

    public void updateAllEvents() {

        if(mNgo==null) {
            getNgoDetails(mNgoEmail);
            return;
        }

        if(isInternet()==false) {
            tvMessage.setText("No Internet Connection");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Dashboard .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try{ progressDialog.show(); }
        catch(Exception e) { return; }

        final ArrayList<EventDetails> allEvents = new ArrayList<>();

        String NGO = mNgo.getEmail().replaceAll("[^A-Za-z0-9]", "-");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.EventDetails)).child(NGO);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allEvents.clear();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    EventDetails temp = eventSnapshot.getValue(EventDetails.class);
                    allEvents.add(temp);
                }
                progressDialog.dismiss();
                setRecyclerView(allEvents);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(NgoDashboard.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


//------------------------------- recycler view --------------------------------------------//


    public void setRecyclerView(ArrayList<EventDetails> recyclerList) {

        if(recyclerList.size()==0) {
            tvMessage.setText("NGO Do Not Have Any Event !!!");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(NgoDashboard.this));
        recyclerView.setAdapter(new DataAdapter(NgoDashboard.this, recyclerList));
    }

    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

        ArrayList<EventDetails> eventDetailsList;
        Context context;

        public DataAdapter(Context context, ArrayList<EventDetails> eventDetailsList) {
            this.eventDetailsList = eventDetailsList;
            this.context = context;
        }

        @Override
        public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_recycler_view, parent, false);
            return new DataAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            EventDetails event = eventDetailsList.get(position);
            holder.tvCategory.setText("Category : " + event.getCategory());
            holder.tvName.setText("Event Name : "+event.getName());
            holder.tvOrganiseBy.setVisibility(View.GONE);
            holder.tvDescription.setText("Description : "+event.getDescription());
            holder.tvLocation.setText("Location : "+event.getLocation());
            holder.tvStartDate.setText("Start Date : "+event.getStart_date());
            holder.tvStartTime.setText("Start Time : "+event.getStart_time());
            holder.tvEndDate.setText("End Date : "+event.getEnd_date());
            holder.tvEndTime.setText("End Time : "+event.getEnd_time());
        }

        @Override
        public int getItemCount() {
            return eventDetailsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

            TextView tvName, tvCategory, tvOrganiseBy, tvDescription, tvLocation, tvStartDate, tvStartTime, tvEndDate, tvEndTime;

            public ViewHolder(View itemView) {
                super(itemView);
                tvCategory = (TextView) itemView.findViewById(R.id.tvCategory);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvOrganiseBy = (TextView) itemView.findViewById(R.id.tvOrganiseBy);
                tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
                tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
                tvStartDate = (TextView) itemView.findViewById(R.id.tvStartDate);
                tvStartTime = (TextView) itemView.findViewById(R.id.tvStartTime);
                tvEndDate = (TextView) itemView.findViewById(R.id.tvEndDate);
                tvEndTime = (TextView) itemView.findViewById(R.id.tvEndTime);

                itemView.setOnCreateContextMenuListener(this);
            }

            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

                contextMenu.setHeaderTitle("Select The Action");
                MenuItem share = contextMenu.add(0, 1, 1, "Share");
                share.setOnMenuItemClickListener(onMenuClicked);
            }

            public MenuItem.OnMenuItemClickListener onMenuClicked = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == 1)
                        shareEvent(eventDetailsList.get(getLayoutPosition()));
                    return false;
                }
            };
        }
    }

    private void shareEvent(EventDetails event) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "NGO Event");

            String sAux = "Name : "+event.getName();
            sAux = sAux + "\n\nDescription : "+event.getDescription();
            sAux = sAux + "\n\nLocation : "+event.getLocation();
            sAux = sAux + "\n\nStart Date : "+event.getStart_date();
            sAux = sAux + "\nStart Time : "+event.getStart_time();
            sAux = sAux + "\n\nEnd Date : "+event.getEnd_date();
            sAux = sAux + "\nEnd Time : "+event.getEnd_time();
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose One"));
        } catch(Exception e) {}
    }

//---------------------------------------------------------------------------------------------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ngo_dashboard_option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.updatePage)
            updatePage();
        else if(item.getItemId() == R.id.share)
            shareNgo();
        else if(item.getItemId() == R.id.subscribe)
            subscribe();
        else if(item.getItemId() == R.id.unsubscribe)
            unsubscribe();
        else if(item.getItemId() == R.id.donate)
            donate();

        return super.onOptionsItemSelected(item);
    }

    private void shareNgo() {

        if(mNgo==null) {
            getNgoDetails(mNgoEmail);
            return;
        }

        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "NGO Event");

            String sAux = "Name : "+mNgo.getName();
            sAux = sAux + "\n\nAddress : "+mNgo.getAddress();
            sAux = sAux + "\n\nEmail : "+mNgo.getEmail();
            sAux = sAux + "\n\nPhone Number : "+mNgo.getPhone_number();
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose One"));
        } catch(Exception e) {}
    }

    private void subscribe() {

        if(mNgo==null) {
            getNgoDetails(mNgoEmail);
            return;
        }

        if(mUser==null) {
            getUserDetails(FirebaseAuth.getInstance(userApp).getCurrentUser().getEmail());
            return;
        }

        String USER_EMAIL = mUser.getEmail();
        String USER_NAME = mUser.getName();

        String NGO_EMAIL = mNgo.getEmail();
        String NGO_NAME = mNgo.getName();

        String USER = USER_EMAIL.replaceAll("[^A-Za-z0-9]", "-");
        String NGO = NGO_EMAIL.replaceAll("[^A-Za-z0-9]", "-");

        SubscriptionDetails detail = new SubscriptionDetails(NGO_EMAIL,NGO_NAME,USER_EMAIL,USER_NAME);

        final int[] cnt = {0};

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subscribing .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance(userApp).getReference(getString(R.string.SubscriptionDetails)).child(USER);
        String key = databaseReference1.push().getKey();
        databaseReference1.child(NGO).setValue(detail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cnt[0]++;
                if (task.isSuccessful()) {
                    if(cnt[0]==2) {
                        progressDialog.dismiss();
                        Toast.makeText(NgoDashboard.this, "Successfully Subscribed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(cnt[0]==2){
                        progressDialog.dismiss();
                        Toast.makeText(NgoDashboard.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.SubscriptionDetails)).child(NGO);
        databaseReference2.child(USER).setValue(detail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cnt[0]++;
                if (task.isSuccessful()) {
                    if(cnt[0]==2) {
                        progressDialog.dismiss();
                        Toast.makeText(NgoDashboard.this, "Successfully Subscribed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(cnt[0]==2){
                        progressDialog.dismiss();
                        Toast.makeText(NgoDashboard.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void unsubscribe() {

        if(mNgo==null) {
            getNgoDetails(mNgoEmail);
            return;
        }

        if(mUser==null) {
            getUserDetails(FirebaseAuth.getInstance(userApp).getCurrentUser().getEmail());
            return;
        }

        String USER = mUser.getEmail().replaceAll("[^A-Za-z0-9]", "-");
        String NGO = mNgo.getEmail().replaceAll("[^A-Za-z0-9]", "-");

        final int[] cnt = {0};

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Unsubscribing .....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance(userApp).getReference(getString(R.string.SubscriptionDetails)).child(USER);
        databaseReference1.child(NGO).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cnt[0]++;
                if (task.isSuccessful()) {
                    if(cnt[0]==2) {
                        progressDialog.dismiss();
                        Toast.makeText(NgoDashboard.this, "Successfully Unsubscribed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(cnt[0]==2){
                        progressDialog.dismiss();
                        Toast.makeText(NgoDashboard.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.SubscriptionDetails)).child(NGO);
        databaseReference2.child(USER).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cnt[0]++;
                if (task.isSuccessful()) {
                    if(cnt[0]==2) {
                        progressDialog.dismiss();
                        Toast.makeText(NgoDashboard.this, "Successfully Unsubscribed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(cnt[0]==2){
                        progressDialog.dismiss();
                        Toast.makeText(NgoDashboard.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void donate() {

        if(mNgo==null) {
            getNgoDetails(mNgoEmail);
            return;
        }

        if(mUser==null) {
            getUserDetails(FirebaseAuth.getInstance(userApp).getCurrentUser().getEmail());
            return;
        }

        Intent intent = new Intent(this, Donation.class);
        intent.putExtra("ngoEmail",mNgo.getEmail());
        intent.putExtra("ngoName",mNgo.getName());
        intent.putExtra("userEmail",mUser.getEmail());
        intent.putExtra("userName",mUser.getName());
        startActivity(intent);
    }

    public boolean isInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }

}
