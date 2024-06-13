package com.example.smart_home_apps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class Home extends AppCompatActivity {

    DrawerLayout drawerLayout;
    TextView nav_username;
    Button NavButtonHome, NavButtonManageUser, NavButtonSettings, NavButtonLogout,
            AboutSmartLamp, AboutSmartLock, AboutSmartGas, AboutSmartSensor,
            MainSmartLamp, MainSmartLock, MainSmartGas, MainSmartSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        drawerLayout = findViewById(R.id.drawer_lay);

        nav_username = findViewById(R.id.nav_username);

        NavButtonHome = findViewById(R.id.NavButtonHome);
        NavButtonManageUser = findViewById(R.id.NavButtonManageUser);
        NavButtonSettings = findViewById(R.id.NavButtonSettings);
        NavButtonLogout = findViewById(R.id.NavButtonLogout);

        AboutSmartLamp = findViewById(R.id.AboutSmartLamp);
        AboutSmartLock = findViewById(R.id.AboutSmartLock);
        AboutSmartGas = findViewById(R.id.AboutSmartGas);
        AboutSmartSensor = findViewById(R.id.AboutSmartSensor);

        MainSmartLamp = findViewById(R.id.MainSmartLamp);
        MainSmartLock = findViewById(R.id.MainSmartLock);
        MainSmartGas = findViewById(R.id.MainSmartGas);
        MainSmartSensor = findViewById(R.id.MainSmartSensor);


        ShowUserData();

        NavButtonManageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ManageUser();
            }
        });

        NavButtonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeUser();
            }
        });

        NavButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavSetting();
            }
        });

        NavButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Login.class);
                startActivity(intent);
            }
        });

        AboutSmartLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutLamp();
            }
        });

        AboutSmartLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutLock();
            }
        });

        AboutSmartGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutGas();
            }
        });

        AboutSmartSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutSensor();
            }
        });

        MainSmartLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainLamp();
            }
        });

        MainSmartLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainLock();
            }
        });

        MainSmartGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainGas();
            }
        });

        MainSmartSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainSensor();
            }
        });
    }

    //get data username from login to home page navigation side bar
    public void ShowUserData(){
        Intent intent = getIntent();

        String usernameUser = intent.getStringExtra("username");

        nav_username.setText(usernameUser);
    }

    public void ClickMenu(View view) {opeDrawer(drawerLayout);}

    private void opeDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    //Manage User Navigation
    public void ManageUser() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String emailFromDB = snapshot.child(userUsername).child("email").getValue(String.class);
                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);
                    String phoneFromDB = snapshot.child(userUsername).child("phone").getValue(String.class);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    Intent intent = new Intent(Home.this, Profile.class);

                    intent.putExtra("email", emailFromDB);
                    intent.putExtra("username", usernameFromDB);
                    intent.putExtra("phone", phoneFromDB);
                    intent.putExtra("password", passwordFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //Home Navigation
    public void HomeUser() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, Home.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //Setting Navigation
    public void NavSetting() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, NavSetting.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //About Gas
    public void AboutGas() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, AboutSmartGas.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //About Lamp
    public void AboutLamp() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, AboutSmartLamp.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //About Lock
    public void AboutLock() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, AboutSmartLock.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //About Sensor
    public void AboutSensor() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, AboutSmartSensor.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    //Main Lamp
    public void MainLamp() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, MainSmartLamp.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    //Main Lock
    public void MainLock() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, MainSmartLock.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    //Main Gas
    public void MainGas() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, MainSmartGas.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    //Main Sensor
    public void MainSensor() {
        String userUsername = nav_username.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(Home.this, MainSmartSensor.class);

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