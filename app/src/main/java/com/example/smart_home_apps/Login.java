package com.example.smart_home_apps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login extends AppCompatActivity {

    EditText signInUsername, signInPassword;
    TextView signUpRedirectText;
    Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInUsername= findViewById(R.id.signInUsername);
        signInPassword = findViewById(R.id.signInPassword);
        signInButton = findViewById(R.id.signInButton);
        signUpRedirectText = findViewById(R.id.signUpRedirectText);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateUsername() | !validatePassword()){

                } else {
                    checkUser();
                }
            }
        });

        signUpRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

    }

    public Boolean validateUsername(){
        String val = signInUsername.getText().toString();
        if(val.isEmpty()){
            signInUsername.setError("Username cannot be empty");
            return false;
        } else {
            signInUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword(){
        String val = signInPassword.getText().toString();
        if(val.isEmpty()){
            signInPassword.setError("Password cannot be empty");
            return false;
        } else {
            signInPassword.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String userUsername = signInUsername.getText().toString().trim();
        String userPassword = signInPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    signInUsername.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    if(passwordFromDB.equals(userPassword)){
                        signInUsername.setError(null);

                        //pass the data using intent


                        String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);


                        Intent intent = new Intent(Login.this, Home.class);


                        intent.putExtra("username", usernameFromDB);


                        startActivity(intent);
                    } else {
                        signInPassword.setError("Invalid Credentials");
                        signInPassword.requestFocus();
                    }
                } else {
                    signInUsername.setError("user does not exist");
                    signInUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}