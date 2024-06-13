package com.example.smart_home_apps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Register extends AppCompatActivity {

    EditText signUpEmail, signUpUsername, signUpPhone, signUpPassword;
    TextView signInRedirectText;
    Button signUpButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signUpEmail = findViewById(R.id.signUpEmail);
        signUpUsername = findViewById(R.id.signUpUsername);
        signUpPhone = findViewById(R.id.signUpPhone);
        signUpPassword = findViewById(R.id.signUpPassword);
        signUpButton = findViewById(R.id.signUpButton);
        signInRedirectText = findViewById(R.id.signInRedirectText);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String email = signUpEmail.getText().toString();
                String username = signUpUsername.getText().toString();
                String phone = signUpPhone.getText().toString();
                String password = signUpPassword.getText().toString();

                HelperClass helperClass = new HelperClass(email, username, phone, password);
                reference.child(username).setValue(helperClass);

                Toast.makeText(Register.this, "You have Sign Up successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, com.example.smart_home_apps.Login.class);
                startActivity(intent);
            }
        });

        signInRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, com.example.smart_home_apps.Login.class);
                startActivity(intent);
            }
        });

    }
}