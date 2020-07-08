package com.ch.android.authenticationapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    TextView name, email, phone, verifyMessage;
    ImageView profileImage;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button verifyEmailButton, changePasswordButton, changeProfileImageButton;
    FirebaseUser fUser;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileImage = findViewById(R.id.profile_image);
        changeProfileImageButton = findViewById(R.id.change_profile_image);
        name = findViewById(R.id.profile_name);
        email = findViewById(R.id.profile_email);
        phone = findViewById(R.id.profile_phone);
        changePasswordButton = findViewById(R.id.change_password);

        verifyMessage = findViewById(R.id.not_verified);
        verifyEmailButton = findViewById(R.id.verify_email_button);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId = fAuth.getCurrentUser().getUid();
        fUser = fAuth.getCurrentUser();

        if(!fUser.isEmailVerified()){
            verifyMessage.setVisibility(View.VISIBLE);
            verifyEmailButton.setVisibility(View.VISIBLE);

            verifyEmailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "Verification email has been sent..", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MainActivity", "onFailure: Email not sent " + e.getMessage());
                        }
                    });
                }
            });
        }

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.d("MainActivity", "Error: " + e.getMessage());
                } else {
                    name.setText(documentSnapshot.getString("name"));
                    email.setText(documentSnapshot.getString("email"));
                    phone.setText(documentSnapshot.getString("phone"));
                }
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText changePassword = new EditText(v.getContext());
                AlertDialog.Builder passwordChangeDialog = new AlertDialog.Builder(v.getContext());
                passwordChangeDialog.setTitle("Change password ?");
                passwordChangeDialog.setMessage("Enter new password");
                passwordChangeDialog.setView(changePassword);

                passwordChangeDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPassword = changePassword.getText().toString();
                        fUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this,"Password changed successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,"Password change failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordChangeDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                passwordChangeDialog.create().show();
            }
        });


        changeProfileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000) {
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();
                profileImage.setImageURI(imageUri);

                uploadImageToFirebase(imageUri);

            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileReference = storageReference.child("profile.jpg");
        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this,"Image uploaded",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Image upload failed",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
}
