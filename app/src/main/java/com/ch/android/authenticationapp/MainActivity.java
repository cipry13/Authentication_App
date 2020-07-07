package com.ch.android.authenticationapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {
    TextView name, email, phone, verifyMessage;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button verifyEmailButton, changePasswordButton;
    FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.profile_name);
        email = findViewById(R.id.profile_email);
        phone = findViewById(R.id.profile_phone);
        changePasswordButton = findViewById(R.id.change_password);


        verifyMessage = findViewById(R.id.not_verified);
        verifyEmailButton = findViewById(R.id.verify_email_button);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

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
    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
}
