package com.example.smart_home_apps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AboutSmartLamp extends AppCompatActivity {


    TextView ToolbarLamp;
    Button BackAboutLamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_smart_lamp);

        ToolbarLamp = findViewById(R.id.ToolbarLamp);
        BackAboutLamp = findViewById(R.id.BackAboutLamp);

        ShowUserData();

        BackAboutLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackHomeUser();
            }
        });
    }

    public void ShowUserData(){
        Intent intent = getIntent();

        String usernameUser = intent.getStringExtra("username");

        ToolbarLamp.setText(usernameUser);
    }

    public void BackHomeUser() {
        String userUsername = ToolbarLamp.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(AboutSmartLamp.this, Home.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}