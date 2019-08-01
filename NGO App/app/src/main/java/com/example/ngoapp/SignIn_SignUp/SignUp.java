package com.example.ngoapp.SignIn_SignUp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ngoapp.DataClass.NgoDetails;
import com.example.ngoapp.R;
import com.example.ngoapp.SelectLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SignUp extends AppCompatActivity {
    
    EditText etName,etAddress,etPhoneNumber,etEmail,etPassword,etConfirmPassword;
    Button btnSignUp,btnSelectLocation;

    static String name,address,phone_number,email,password,confirm_password;
    static boolean isValidLocation = false;
    static String latitude, longitude;

    FirebaseApp ngoApp,userApp;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
    }

    private void init() {

        ngoApp = FirebaseApp.getInstance("ngoApp");
        userApp = FirebaseApp.getInstance("userApp");

        etName = (EditText) findViewById(R.id.etName);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);

        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        btnSelectLocation = (Button) findViewById(R.id.btnSelectLocation);
        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLocation();
            }
        });
    }

    private void getDetails() {

        name = etName.getText().toString();
        address = etAddress.getText().toString();
        phone_number = etPhoneNumber.getText().toString();
        email = etEmail.getText().toString().replaceAll("\\s+$", "");
        password = etPassword.getText().toString();
        confirm_password = etConfirmPassword.getText().toString();
    }

    private void signUp() {

        getDetails();

        if(name.isEmpty() || address.isEmpty() || phone_number.isEmpty() || email.isEmpty() || password.isEmpty() || confirm_password.isEmpty()) {
            Toast.makeText(this, "All Details Are Mandatory", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isValidLocation) {
            Toast.makeText(this, "Please Select Location On Map", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.compareTo(confirm_password)!=0) {
            Toast.makeText(this, "Both Passwords Must Be Same", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(SignUp.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Authenticating ...");
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        FirebaseAuth.getInstance(ngoApp).createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                            uploadDetails();
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(SignUp.this, "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadDetails() {

        NgoDetails ngo = new NgoDetails(name,address,phone_number,email,latitude,longitude);
        String key = ngo.getEmail().replaceAll("[^A-Za-z0-9]", "-");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(ngoApp).getReference(getString(R.string.NgoDetails));
        databaseReference.child(key).setValue(ngo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()) {
                    finish();
                }
                else
                    Toast.makeText(SignUp.this, "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectLocation() {

        if(!havePermissions(this)) {
            requestPermission(this);
            return;
        }

        Intent intent = new Intent(SignUp.this, SelectLocation.class);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1) {
            if(resultCode==1) {
                isValidLocation = true;
                latitude = data.getStringExtra("latitude");
                longitude = data.getStringExtra("longitude");
            }
        }
    }

    public static boolean havePermissions(Activity caller) {

        int permissionCheck = ContextCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck== PackageManager.PERMISSION_GRANTED)
        {
            permissionCheck = ContextCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_COARSE_LOCATION);

            if(permissionCheck == PackageManager.PERMISSION_GRANTED)
                return true;
        }

        return false;
    }

    public static void requestPermission(Activity caller) {

        List<String> permissionList = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if(ContextCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permissionList.size()>0)
        {
            String [] permissionArray = new String[permissionList.size()];

            for (int i=0;i<permissionList.size();i++)
                permissionArray[i] = permissionList.get(i);

            ActivityCompat.requestPermissions(caller, permissionArray,101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==101) {
            for(int i=0;i<grantResults.length;i++)
                if(grantResults[i]!=PackageManager.PERMISSION_GRANTED)
                    return ;

            selectLocation();
        }
    }
}
