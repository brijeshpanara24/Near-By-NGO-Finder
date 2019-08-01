package com.example.ngoapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ngoapp.DataClass.EventDetails;
import com.example.ngoapp.DataClass.NgoDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateEvent extends AppCompatActivity {

    private static String NGO_NAME = null;
    private static String NGO = null;
    private static String TAG = null;

    EditText etName,etDescription,etStartDate,etStartTime,etEndDate,etEndTime,etLocation,etCategory;
    Button btnCreateEvent;
    Toolbar toolbar;

    static String name,description,location,startDate,startTime,endDate,endTime,category;

    FirebaseApp ngoApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        init();
    }

    private void init() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Event");

        ngoApp = FirebaseApp.getInstance("ngoApp");

        NGO = FirebaseAuth.getInstance(ngoApp).getCurrentUser().getEmail().replaceAll("[^A-Za-z0-9]", "-");
        TAG = getIntent().getStringExtra("tag");

        etName = (EditText) findViewById(R.id.etName);
        etDescription = (EditText) findViewById(R.id.etDescription);
        etStartDate = (EditText) findViewById(R.id.etStartDate);
        etStartTime = (EditText) findViewById(R.id.etStartTime);
        etEndDate = (EditText) findViewById(R.id.etEndDate);
        etEndTime = (EditText) findViewById(R.id.etEndTime);
        etLocation = (EditText) findViewById(R.id.etLocation);
        etCategory=(EditText)findViewById(R.id.etCategory);

        if(TAG.compareTo("edit")==0) {
            EventDetails event = Database.getInstance().getEditEventDetails();
            etCategory.setText(event.getCategory());
            etName.setText(event.getName());
            etDescription.setText(event.getDescription());
            etLocation.setText(event.getLocation());
            etStartDate.setText(event.getStart_date());
            etStartTime.setText(event.getStart_time());
            etEndDate.setText(event.getEnd_date());
            etEndTime.setText(event.getEnd_time());
        }

        btnCreateEvent = (Button) findViewById(R.id.btnCreateEvent);
        if(TAG.compareTo("edit")==0)
            btnCreateEvent.setText("Edit Event");
        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEvent();
            }
        });
    }

    private void getDetails() {
        name = etName.getText().toString();
        description = etDescription.getText().toString();
        location = etLocation.getText().toString();
        startDate = etStartDate.getText().toString();
        startTime = etStartTime.getText().toString();
        endDate = etEndDate.getText().toString();
        endTime = etEndTime.getText().toString();
        category=etCategory.getText().toString();

    }

    private void AddEvent() {

        getDetails();

        if(name.isEmpty() || description.isEmpty() || location.isEmpty() || startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty()||category.isEmpty()) {
            Toast.makeText(this, "All Details Are Mandatory", Toast.LENGTH_SHORT).show();
            return;
        }

        if(NGO_NAME==null) {
            Database database = Database.getInstance();
            if(database.getNgo()==null) {
                getNgoDetails();
                return;
            } else {
                NGO_NAME = database.getNgo().getName();
            }
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.EventDetails)).child(NGO);

        EventDetails event = new EventDetails(name,category,NGO_NAME,description,location,startDate,startTime,endDate,endTime);
        String key;
        if(TAG.compareTo("edit")==0)
            key = Database.getInstance().getEditNgoEventKey();
        else
            key = databaseReference.push().getKey();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Event .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        databaseReference.child(key).setValue(event).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()) {
                    setResult(1);
                    finish();
                }
                else
                    Toast.makeText(CreateEvent.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNgoDetails() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data .....");
        progressDialog.setCanceledOnTouchOutside(false);
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        String NGO_EMAIL = NGO.replaceAll("[^A-Za-z0-9]", "-");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.NgoDetails)).child(NGO_EMAIL);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                Database.getInstance().setNgo(dataSnapshot.getValue(NgoDetails.class));
                AddEvent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(CreateEvent.this, "Failed : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
