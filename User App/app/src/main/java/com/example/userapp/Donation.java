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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.userapp.DataClass.DonationDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class Donation extends AppCompatActivity {

    Button btnDonate;
    EditText etAmount;
    String ngoEmail,ngoName;
    String userEmail,userName;

    FirebaseApp ngoApp,userApp;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Donate");

        userApp = FirebaseApp.getInstance("userApp");
        ngoApp = FirebaseApp.getInstance("ngoApp");

        ngoEmail=getIntent().getStringExtra("ngoEmail");
        ngoName=getIntent().getStringExtra("ngoName");
        userEmail=getIntent().getStringExtra("userEmail");
        userName=getIntent().getStringExtra("userName");

        btnDonate=(Button) findViewById(R.id.btnDonate);
        etAmount=(EditText) findViewById(R.id.etAmount);

        btnDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transferFunds(etAmount.getText().toString());
            }
        });
    }

    private void transferFunds(String amount){

        if(amount.isEmpty()) {
            Toast.makeText(this, "Amount Cannot Be Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(isInternet()==false) {
            Toast.makeText(this, "No Internet Connection !!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Database.getInstance().havePermissions(Donation.this)==false) {
            Database.getInstance().requestPermission(Donation.this);
            return;
        }

        String USER_EMAIL = userEmail;
        String NGO_EMAIL = ngoEmail;

        Date date = new Date();
        final DonationDetails donationDetails = new DonationDetails(ngoEmail,ngoName,userEmail,userName,amount,date.toString());

        String USER = USER_EMAIL.replaceAll("[^A-Za-z0-9]", "-");
        final String NGO = NGO_EMAIL.replaceAll("[^A-Za-z0-9]", "-");

        final int[] cnt = {0};

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Transferring Funds .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance(userApp).getReference(getString(R.string.DonationDetails)).child(USER);
        String key1=databaseReference1.push().getKey();
        databaseReference1.child(key1).setValue(donationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cnt[0]++;
                if (task.isSuccessful()) {
                    if(cnt[0]==2) {
                        progressDialog.dismiss();
                        createReceipt(donationDetails);
                        Toast.makeText(Donation.this, "Successfully Donated !!!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    if(cnt[0]==2){
                        progressDialog.dismiss();
                        Toast.makeText(Donation.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.DonationDetails)).child(NGO);
        String key2=databaseReference2.push().getKey();
        databaseReference2.child(key2).setValue(donationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cnt[0]++;
                if (task.isSuccessful()) {
                    if(cnt[0]==2) {
                        progressDialog.dismiss();
                        createReceipt(donationDetails);
                        Toast.makeText(Donation.this, "Successfully Donated !!!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    if(cnt[0]==2){
                        progressDialog.dismiss();
                        Toast.makeText(Donation.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void createReceipt(DonationDetails donationDetails) {
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
        String targetPdf = directory_path + "Receipts.pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Toast.makeText(this, "Failed : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
    }

    public boolean isInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }
}

