package com.example.userapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userapp.DataClass.EventDetails;
import com.example.userapp.DataClass.SubscriptionDetails;
import com.example.userapp.SignIn_SignUp.SignIn;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    TextView tvMessage;
    RecyclerView recyclerView;
    String category;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    boolean isCategory;
    FirebaseApp userApp,ngoApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        isCategory = false;
        init();
    }

    private void init() {

        Database database = Database.getInstance();
        database.initialiseUserApp(getApplicationContext());
        database.initialiseNgoApp(getApplicationContext());
        isCategory = false;
        userApp = FirebaseApp.getInstance("userApp");
        ngoApp = FirebaseApp.getInstance("ngoApp");

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setNavigationDrawer();

        updatePage();
    }

    public void setNavigationDrawer() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(UserDashboard.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

//------------------------------------------------------------------------------------------//

    private void updatePage() {
        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        getAllSubscribedNgoDetails();
    }
    private void getAllSubscribedNgoDetails() {

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
                if(isInternet())
                    getAllSubscribedNgoEvents(allSubscribedNgoDetails);
                else {
                    tvMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tvMessage.setText("No Internet Connection !!!");
                    Toast.makeText(UserDashboard.this, "No Internet Connection !!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(UserDashboard.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAllSubscribedNgoEvents(final ArrayList<SubscriptionDetails> allSubscribedNgoDetails) {

        if(allSubscribedNgoDetails.size()==0) {
            recyclerView.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("You Have Not Subscribe Any NGO !!!");
            return;
        }

        final ArrayList<EventDetails> allSubscribedEventDetails = new ArrayList<>();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Subscribed NGO Events .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.EventDetails));

        final int[] cnt = {0};
        for(int i=0;i<allSubscribedNgoDetails.size();i++) {

            String key = allSubscribedNgoDetails.get(i).getNgoEmail().replaceAll("[^A-Za-z0-9]", "-");
            DatabaseReference tempDatabaseReference = databaseReference.child(key);
            tempDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        EventDetails temp = eventSnapshot.getValue(EventDetails.class);
                        if (isCategory) {
                            if (category.equalsIgnoreCase(temp.getCategory())) {
                                allSubscribedEventDetails.add(temp);
                            }
                        } else {
                            allSubscribedEventDetails.add(temp);
                        }
                    }
                    cnt[0]++;

                    if(cnt[0] == allSubscribedNgoDetails.size()) {
                        progressDialog.dismiss();
                        setRecyclerView(allSubscribedEventDetails);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    cnt[0]++;

                    if(cnt[0] == allSubscribedNgoDetails.size()) {
                        progressDialog.dismiss();
                        Toast.makeText(UserDashboard.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

//------------------------------- Recycler View --------------------------------------------//

    public void setRecyclerView(ArrayList<EventDetails> recyclerList) {

        if(recyclerList.size()==0) {
            recyclerView.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Subscribed NGOs Do Not Have Any Events !!!");
            return;
        }

        tvMessage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(UserDashboard.this));
        recyclerView.setAdapter(new DataAdapter(UserDashboard.this, recyclerList));
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
            holder.tvOrganisedBy.setText("Organised By : "+event.getOrganisedBy());
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

            TextView tvName, tvCategory, tvOrganisedBy, tvDescription, tvLocation, tvStartDate, tvStartTime, tvEndDate, tvEndTime;

            public ViewHolder(View itemView) {
                super(itemView);
                tvCategory = (TextView) itemView.findViewById(R.id.tvCategory);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvOrganisedBy = (TextView) itemView.findViewById(R.id.tvOrganiseBy);
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

                //getLayoutPosition();
                contextMenu.setHeaderTitle("Select The Action");

                MenuItem share;
                share = contextMenu.add(0, 1, 1, "Share");
                share.setOnMenuItemClickListener(onMenuClicked);
            }

            public MenuItem.OnMenuItemClickListener onMenuClicked = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    //getLayoutPosition()
                    if (menuItem.getItemId() == 1) {
                        shareEvent(eventDetailsList.get(getLayoutPosition()));
                    }
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

//------------------------------- Navigation Layout -----------------------------------------//

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id==R.id.navNearByNgo) {
            if(!Database.getInstance().havePermissions(UserDashboard.this)) {
                Database.getInstance().requestPermission(UserDashboard.this);
                return false;
            }
            Intent intent = new Intent(UserDashboard.this,FindNearByNgoList.class);
            startActivity(intent);

        } else if(id==R.id.navDonationHistory) {
            Intent intent=new Intent(UserDashboard.this,DonationList.class);
            startActivity(intent);
        } else if(id==R.id.navSubscribedNgo) {
            Intent intent = new Intent(UserDashboard.this,SubscribedNgo.class);
            startActivity(intent);
        } else if(id==R.id.navSignOut) {
            signOut();
        } else if(id==R.id.navShare) {
            shareApp();
        } else if(id==R.id.navRateUs) {
            openLink("http://play.google.com/store/apps/details?id=g119.da2016.CodingSchedule");
        } else if(id==R.id.navSuggestUs) {
            openLink("http://play.google.com/store/apps/details?id=g119.da2016.CodingSchedule");
        } else if(id==R.id.navDevlopers) {
            Intent intent = new Intent(UserDashboard.this, DevelopersList.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Are you sure you want to sign out ?");

        builder.setPositiveButton(" YES ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                FirebaseAuth.getInstance(userApp).signOut();
                startActivity(new Intent(UserDashboard.this, SignIn.class));
                finish();
            }
        });
        builder.setNegativeButton(" NO ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.create().show();
    }

    private void shareApp() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Near By NGO Finder");

            String sAux = "Near By NGO Finder";
            sAux = sAux + "\n\nHelp you to easily locate and donate to near by NGOs and keep you updated with their upcoming events or drives" ;
            sAux = sAux + "\n\nhttp://play.google.com/store/apps/details?id=g119.da2016.CodingSchedule";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose One"));
        } catch(Exception e) {}
    }

    private void openLink(String url) {

        if(url.isEmpty()) {
            Toast.makeText(this, "Url Not Found", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!url.contains("http"))
            url = "http://" + url;

        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
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

        if (item.getItemId() == R.id.updatePage) {
            isCategory = false;
            updatePage();
        }
        if (item.getItemId() == R.id.applyFilter) {
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.user_dashboard_custom_dialog, null);
            final EditText etCategory = alertLayout.findViewById(R.id.etRange);

            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
            alert.setTitle("Category");
            alert.setView(alertLayout);
            alert.setPositiveButton("Apply", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String temp = etCategory.getText().toString();
                    if (temp.isEmpty())
                        Toast.makeText(UserDashboard.this, "Please provide category", Toast.LENGTH_SHORT).show();
                    else {
                        try {
                            category = temp;
                            isCategory = true;
                            updatePage();
                        } catch (Exception e) {
                        }
                    }
                }
            });
            android.app.AlertDialog dialog = alert.create();
            dialog.show();

        }


        return super.onOptionsItemSelected(item);
    }

//------------------------------- Helper Functions ------------------------------------------//

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)==true)
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    public boolean isInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }
}
