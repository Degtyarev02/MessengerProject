package com.example.messenger_project.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.example.messenger_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {

    private EditText UserName, UserStatus;
    private Button UpdateInfo;
    private CircleImageView UserIcon;
    private ImageView setInstagramprofileButton;
    private NiceSpinner statusSpinnerSelector;
    private String photoURL, instagramURL;
    private TextView instagramName;

    private FirebaseAuth mAuth;
    private String currentUserId, retrieveEmail, retrievePassword;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;
    private StorageReference userProfImageRef;

    private Toolbar setToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userProfImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        retrieveEmail = snapshot.child("email").getValue().toString();
                        retrievePassword = snapshot.child("password").getValue().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        Initialize();
        RetrieveUserInfo();

        statusSpinnerSelector.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                UserStatus.setText(item);
            }
        });

        UpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        UserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });

        setInstagramprofileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlatDialog flatDialog = new FlatDialog(SettingsActivity.this);
                flatDialog
                        .setTitle("Instagram")
                        .setFirstTextFieldHint("Enter your instagram name...")
                        .setFirstButtonText("Save")
                        .setSecondButtonText("Cancel")
                        .setBackgroundColor(getResources().getColor(R.color.white))
                        .setFirstButtonColor(getResources().getColor(R.color.DarkBlue))
                        .setFirstButtonTextColor(getResources().getColor(R.color.whity_gray))
                        .setSecondButtonColor(getResources().getColor(R.color.DarkRed))
                        .setSecondButtonTextColor(getResources().getColor(R.color.whity_gray))
                        .setTitleColor(getResources().getColor(R.color.DarkBlue))
                        .setFirstTextFieldBorderColor(getResources().getColor(R.color.DarkRed))
                        .setFirstTextFieldTextColor(getResources().getColor(R.color.blackyGray))
                        .setFirstTextFieldHintColor(getResources().getColor(R.color.blackyGray))
                        .withFirstButtonListner(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                instagramURL = "https://www.instagram.com/" + flatDialog.getFirstTextField() + "/";
                                instagramName.setText(flatDialog.getFirstTextField());
                                flatDialog.dismiss();
                            }
                        })
                        .withSecondButtonListner(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                flatDialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }


    private void Initialize() {
        UserIcon = findViewById(R.id.profile_image);
        UserName = findViewById(R.id.set_username);
        UserStatus = findViewById(R.id.set_user_status);
        UpdateInfo = findViewById(R.id.update_button);
        statusSpinnerSelector = findViewById(R.id.status_select_spinner);
        setInstagramprofileButton = findViewById(R.id.instagram_set_profile);
        instagramName = findViewById(R.id.instagram_name);

        setToolBar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(setToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                final StorageReference filePath = userProfImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toasty.success(SettingsActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                            final String downloaedUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloaedUrl = uri.toString();/*
                                    RootRef.child("Users").child(currentUserId).child("image")
                                            .setValue(downloaedUrl);*/
                                    photoURL = downloaedUrl;
                                    Picasso.get().load(photoURL).into(UserIcon);
                                }
                            });
                        }
                    }
                });
            }
        }

    }

    private void UpdateSettings() {
        String setUserName = UserName.getText().toString();
        String setStatus = UserStatus.getText().toString();

        HashMap<String, String> ProfileMap = new HashMap<>();
        ProfileMap.put("uid", currentUserId);
        ProfileMap.put("name", setUserName);
        ProfileMap.put("status", setStatus);
        ProfileMap.put("image", photoURL);
        ProfileMap.put("email", retrieveEmail);
        ProfileMap.put("password", retrievePassword);
        ProfileMap.put("instagram", instagramURL);

        if (!TextUtils.isEmpty(setUserName)) {
            RootRef.child("Users").child(currentUserId).setValue(ProfileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toasty.success(SettingsActivity.this, "Update is Successfully", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                            }
                        }
                    });
        } else
            Toasty.error(SettingsActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if ((snapshot.exists()) && (snapshot.hasChild("name") && snapshot.hasChild("image"))) {

                            String retrieveUserName = snapshot.child("name").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();
                            photoURL = snapshot.child("image").getValue().toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveUserStatus);
                            Picasso.get().load(photoURL).into(UserIcon);

                            UserName.setEnabled(false);
                        } else if ((snapshot.exists()) && (snapshot.hasChild("name"))) {
                            String retrieveUserName = snapshot.child("name").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveUserStatus);

                            UserName.setEnabled(false);
                        }

                        if((snapshot.exists()) && (snapshot.hasChild("instagram")))
                        {
                            String name = snapshot.child("instagram").getValue().toString();
                            String[] words = name.split("/");
                            instagramName.setText(words[3]);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}