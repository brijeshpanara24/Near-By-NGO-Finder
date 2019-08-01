package com.example.ngoapp.SignIn_SignUp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ngoapp.Database;
import com.example.ngoapp.NgoDashboard;
import com.example.ngoapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignIn extends AppCompatActivity {


    EditText etEmail,etPassword;
    Button btnSignIn;
    TextView tvSignUp,tvForgotPassword;

    FirebaseApp ngoApp,userApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Database database = Database.getInstance();
        database.initialiseNgoApp(getApplicationContext());
        database.initialiseUserApp(getApplicationContext());

        ngoApp = FirebaseApp.getInstance("ngoApp");
        userApp = FirebaseApp.getInstance("userApp");

        if(FirebaseAuth.getInstance(ngoApp).getCurrentUser()!=null)
            startUserDashboard();

        init();
    }

    private void init() {

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        tvSignUp = (TextView) findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignUp();
            }
        });

        tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });
    }

    private void signIn() {

        String email = etEmail.getText().toString().replaceAll("\\s+$", "");
        String password = etPassword.getText().toString();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All Fields Are Mandatory", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(SignIn.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Authenticating ...");
        try { progressDialog.show(); }
        catch(Exception e) { return; }

        FirebaseAuth.getInstance(ngoApp).signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(SignIn.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            startUserDashboard();
                        }else
                            Toast.makeText(SignIn.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void forgotPassword() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        final String ngoEmail = etEmail.getText().toString();

        if (ngoEmail.isEmpty())
            Toast.makeText(this, "Please Enter Email Id", Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Send password reset link to my registered email address");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    progressDialog.setMessage("Sending Mail .....");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    FirebaseAuth.getInstance(ngoApp).sendPasswordResetEmail(ngoEmail).addOnCompleteListener(SignIn.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            progressDialog.dismiss();

                            if (task.isSuccessful())
                                Toast.makeText(SignIn.this, "Password Reset Link Is Successfully Sent To Your Mail", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(SignIn.this, "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });
            builder.create().show();
        }
    }

    private void startSignUp() {
        Intent intent = new Intent(SignIn.this, SignUp.class);
        startActivity(intent);
    }

    private void startUserDashboard() {
        Intent intent = new Intent(SignIn.this, NgoDashboard.class);
        startActivity(intent);
        finish();
    }
}
