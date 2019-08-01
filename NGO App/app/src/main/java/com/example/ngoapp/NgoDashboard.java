package com.example.ngoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngoapp.DataClass.EventDetails;
import com.example.ngoapp.SignIn_SignUp.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NgoDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static int REQUEST_CREATE_EVENT = 1;
    private static int REQUEST_EDIT_EVENT = 2;

    TextView tvMessage;
    RecyclerView recyclerView;

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    FirebaseApp ngoApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_dashboard);

        init();
    }

    private void init() {

        Database database = Database.getInstance();
        database.initialiseNgoApp(getApplicationContext());
        database.initialiseUserApp(getApplicationContext());

        ngoApp  = FirebaseApp.getInstance("ngoApp");

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setNavigationDrawer();

        updatePage();
    }

    public void setNavigationDrawer() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(NgoDashboard.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CREATE_EVENT) {
            if(resultCode==1)
                updatePage();
        }
        else if(requestCode==REQUEST_EDIT_EVENT) {
            if(resultCode==1)
                updatePage();
        }
    }

    private void updatePage() {

        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        updateAllEvents();
    }

    public void updateAllEvents() {

        if(isInternet()==false) {
            tvMessage.setText("No Internet Connection");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        final ArrayList<EventDetails> allEvents = new ArrayList<>();
        final ArrayList<String> allEventsKey = new ArrayList<>();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Dashboard .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        String NGO = FirebaseAuth.getInstance(ngoApp).getCurrentUser().getEmail().replaceAll("[^A-Za-z0-9]", "-");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.EventDetails)).child(NGO);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allEvents.clear();
                allEventsKey.clear();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    EventDetails temp = eventSnapshot.getValue(EventDetails.class);
                    allEvents.add(temp);
                    allEventsKey.add(eventSnapshot.getKey());
                }
                Database.getInstance().setAllEvents(allEvents,allEventsKey);
                progressDialog.dismiss();
                setRecyclerView(allEvents,allEventsKey);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(NgoDashboard.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


//------------------------------- recycler view --------------------------------------------//


    public void setRecyclerView(ArrayList<EventDetails> recyclerList, ArrayList<String> recyclerListKey) {

        if(recyclerList.size()==0) {
            tvMessage.setText("Your NGO Do Not Have Events !!!");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new DataAdapter(getApplicationContext(), recyclerList, recyclerListKey));
    }

    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

        ArrayList<EventDetails> eventDetailsList;
        ArrayList<String> ngoEventListKey;
        Context context;

        public DataAdapter(Context context, ArrayList<EventDetails> eventDetailsList, ArrayList<String> ngoEventListKey) {
            this.eventDetailsList = eventDetailsList;
            this.ngoEventListKey = ngoEventListKey;
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
            holder.tvCategory.setText("Event Category: "+event.getCategory());
            holder.tvName.setText("Event Name : "+event.getName());
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

            TextView tvName, tvDescription, tvLocation, tvStartDate, tvStartTime, tvEndDate, tvEndTime,tvCategory;

            public ViewHolder(View itemView) {
                super(itemView);
                tvCategory=(TextView)itemView.findViewById(R.id.tvCategory);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
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
                MenuItem edit, remove, share;

                contextMenu.setHeaderTitle("Select The Action");

                edit = contextMenu.add(0, 1, 1, "Edit");
                remove = contextMenu.add(0, 2, 2, "Remove");
                share = contextMenu.add(0, 3, 3, "Share");

                edit.setOnMenuItemClickListener(onMenuClicked);
                remove.setOnMenuItemClickListener(onMenuClicked);
                share.setOnMenuItemClickListener(onMenuClicked);
            }

            public MenuItem.OnMenuItemClickListener onMenuClicked = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    //getLayoutPosition()
                    if (menuItem.getItemId() == 1) {
                        Intent intent = new Intent(NgoDashboard.this,CreateEvent.class);
                        intent.putExtra("tag","edit");
                        Database.getInstance().setEditNgoEvent(eventDetailsList.get(getLayoutPosition()),ngoEventListKey.get(getLayoutPosition()));
                        startActivityForResult(intent,REQUEST_EDIT_EVENT);
                    }
                    else if (menuItem.getItemId() == 2) {
                        removeEvent(ngoEventListKey.get(getLayoutPosition()));
                    }
                    else if (menuItem.getItemId() == 3) {
                        shareEvent(eventDetailsList.get(getLayoutPosition()));
                    }
                    return false;
                }
            };
        }
    }

    private void removeEvent(String key) {

        String NGO = FirebaseAuth.getInstance(ngoApp).getCurrentUser().getEmail().replaceAll("[^A-Za-z0-9]", "-");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.EventDetails));
        databaseReference.child(NGO).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    updatePage();
                else
                    Toast.makeText(NgoDashboard.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        if(id==R.id.navCreateEvent) {
            Intent intent = new Intent(NgoDashboard.this,CreateEvent.class);
            intent.putExtra("tag","add");
            startActivityForResult(intent,REQUEST_CREATE_EVENT);
        } else if(id==R.id.navDonationHistory) {
            Intent intent = new Intent(NgoDashboard.this,DonationList.class);
            startActivity(intent);
        } else if(id==R.id.navSubscribedUser) {
            Intent intent = new Intent(NgoDashboard.this,SubscriberList.class);
            startActivity(intent);
        } else if(id==R.id.navSignOut) {
            signOut();
        } else if(id==R.id.navShare) {
            shareApp();
        } else if(id==R.id.navRateUs) {
            openLink("http://play.google.com/store/apps/details?id=g119.da2016.CodingSchedule");
        } else if(id==R.id.navSuggestUs) {
            openLink("http://play.google.com/store/apps/details?id=g119.da2016.CodingSchedule");
        } else if(id==R.id.navDevelopers) {
            Intent intent = new Intent(NgoDashboard.this,DevelopersList.class);
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

                FirebaseAuth.getInstance(ngoApp).signOut();
                startActivity(new Intent(NgoDashboard.this, SignIn.class));
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

        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection !!!", Toast.LENGTH_SHORT).show();
            return;
        }

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
