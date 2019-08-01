package com.example.userapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userapp.DataClass.DonationDetails;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class DonationList extends AppCompatActivity {

    static boolean sortByName = false;
    static boolean sortByAmount = false;
    static boolean sortByDate = true;

    FirebaseApp userApp;

    Toolbar toolbar;
    TextView tvMessage;
    RecyclerView recyclerView;

    ArrayList<DonationDetails> allDonations=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Donation History");

        userApp = FirebaseApp.getInstance("userApp");

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        updatePage();
    }

    public void updatePage() {
        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        getDonationList();
    }

    public void getDonationList(){

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

        String USER= FirebaseAuth.getInstance(userApp).getCurrentUser().getEmail().replaceAll("[^A-Za-z0-9]", "-");;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(userApp).getReference(getString(R.string.DonationDetails)).child(USER);
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

//------------------------------------------Recycler view---------------

    public void setRecyclerView() {

        if(allDonations.size()==0) {
            tvMessage.setText("No Donation Made !!!");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        ArrayList<DonationDetails> recyclerList = getSortedList();
        recyclerView.setLayoutManager(new LinearLayoutManager(DonationList.this));
        recyclerView.setAdapter(new DonationList.DataAdapter(DonationList.this, recyclerList));
    }

    private ArrayList<DonationDetails> getSortedList() {
        if(sortByName) {
            Collections.sort(allDonations, new Comparator<DonationDetails>() {
                @Override
                public int compare(DonationDetails o1, DonationDetails o2) {
                    String name1 = o1.getNgoName().toLowerCase();
                    String name2 = o2.getNgoName().toLowerCase();
                    if (name1.compareTo(name2) < 0)
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
                    if(amount1<amount2)
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
                    if(d1>=d2)
                        return -1;
                    else
                        return 1;
                }
            });
        }
        return allDonations;
    }

    public class DataAdapter extends RecyclerView.Adapter<  DonationList.DataAdapter.ViewHolder> {

        ArrayList<DonationDetails> donationList;
        Context context;

        public DataAdapter(Context context, ArrayList<DonationDetails> donationList) {
            this.donationList = donationList;
            this.context = context;
        }

        @Override
        public  DonationList.DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.donation_recycler_view, parent, false);
            return new  DonationList.DataAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(   DonationList.DataAdapter.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            DonationDetails donationDetails = donationList.get(position);
            holder.tvNgoName.setText("NGO Name : "+donationDetails.getNgoName());
            holder.tvAmount.setText("Amount : "+donationDetails.getAmount()+" RS");
            holder.tvDate.setText("Date : "+donationDetails.getDateTime());
        }

        @Override
        public int getItemCount() {
            return donationList.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

            TextView tvNgoName,tvAmount,tvDate;

            public ViewHolder(View itemView) {
                super(itemView);

                tvNgoName = (TextView) itemView.findViewById(R.id.tvNgoName);
                tvAmount = (TextView) itemView.findViewById(R.id.tvAmount);
                tvDate = (TextView) itemView.findViewById(R.id.tvDate);
                itemView.setOnCreateContextMenuListener(this);
            }

            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                MenuItem downloadReceipt;
                contextMenu.setHeaderTitle("Select The Action");

                downloadReceipt = contextMenu.add(0, 1, 1, "Download Receipt");
                downloadReceipt.setOnMenuItemClickListener(onMenuClicked);
            }

            public MenuItem.OnMenuItemClickListener onMenuClicked = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==1){
                        createReceipt(donationList.get(getLayoutPosition()));
                    }
                    return false;
                }
            };
        }
    }

    private void createReceipt(DonationDetails donationDetails) {

        if(Database.getInstance().havePermissions(DonationList.this)==false) {
            Database.getInstance().requestPermission(DonationList.this);
            return;
        }

        PdfDocument document = new PdfDocument();

        // create a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(900, 900, 1).create();
        String someText = "We " + donationDetails.getNgoName() + "hereby acknowledge that we have received donation of Rs." + donationDetails.getAmount() + "from Mr/Ms." + donationDetails.getUserName() + "on " + donationDetails.getDateTime();

        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        canvas.drawText(someText, 80, 50, paint);

        // finish the page
        document.finishPage(page);

        // write the document content
        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/UserApp/";
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetPdf = directory_path + "Donation" + donationDetails.getDateTime();
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Receipt Downloaded", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
    }

//--------------------------------- Option Menu ----------------------------------------------//

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
