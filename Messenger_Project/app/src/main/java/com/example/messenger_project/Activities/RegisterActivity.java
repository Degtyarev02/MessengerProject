package com.example.messenger_project.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messenger_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private Button RegisterBtn, HaveAccount;
    private EditText NewEmail, NewPassword, ConfirmPassword;
    private TextView PhoneAuth;
    private FirebaseAuth mAuth;
    private DatabaseReference RootReference;
    private ProgressDialog progressBar;
    private Animation animAlpha;
    private String deviceToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootReference = FirebaseDatabase.getInstance().getReference();


        RegisterInitialize();


        HaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                 SendUserToLoginActivity();
            }
        });

        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                CreateNewAccount();
            }
        });

        PhoneAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                SendUserToPhoneRegActivity();
            }
        });
    }


    private void CreateNewAccount()
    {
        String email = NewEmail.getText().toString();
        String password = NewPassword.getText().toString();
        String ConfPas = ConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) )
        {
            Toasty.error(this, "Please enter Email and Password", Toast.LENGTH_SHORT).show();
        }

        else if(!password.equals(ConfPas))
        {
            Toasty.error(this, "Passwords are not equals", Toast.LENGTH_SHORT).show();
        }

        else
        {
            progressBar.setTitle("Creating new account");
            progressBar.setMessage("Please Wait");
            progressBar.setCanceledOnTouchOutside(true);
            progressBar.show();


            //установка введенного логина и пароля в учетную запись файрбэйз
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        deviceToken = task.getResult();
                                    }
                                });
                                String userId = mAuth.getCurrentUser().getUid();
                                RootReference.child("Users").child(userId).child("email").setValue(email);
                                RootReference.child("Users").child(userId).child("password").setValue(password);
                                RootReference.child("Users").child(userId).child("device_token")
                                        .setValue(deviceToken);
                                SendUserToMainActivity();
                                Toasty.success(RegisterActivity.this, "Successful!", Toast.LENGTH_LONG).show();
                                progressBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toasty.error(RegisterActivity.this, "Error" + message, Toast.LENGTH_LONG).show();
                                progressBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void RegisterInitialize()
    {
        RegisterBtn = findViewById(R.id.register_button);
        NewEmail = findViewById(R.id.register_email);
        NewPassword = findViewById(R.id.register_password);
        ConfirmPassword = findViewById(R.id.confirm_register_password);
        HaveAccount = findViewById(R.id.already_have_account);
        PhoneAuth = findViewById(R.id.Phone_autentification);
        animAlpha = AnimationUtils.loadAnimation(this, R.anim.button);
        progressBar = new ProgressDialog(this);

    }



    // Send user to another one activity

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class );
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class );
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToPhoneRegActivity()
    {
        Intent phoneAuth = new Intent(RegisterActivity.this, PhoneLoginActivity.class);
        startActivity(phoneAuth);
    }
}