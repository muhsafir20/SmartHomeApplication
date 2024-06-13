package com.example.smart_home_apps;

import  androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.AsyncTask;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainSmartLamp extends AppCompatActivity {

    TextView ToolbarMainLamp;
    Button BackMainLamp;
    SwitchCompat SwitchButtonLamp;
    ImageView imageViewLight;

    private static String MY_PREFS = "switch_prefs"; //shared name preferences name
    private static String LIGHT_STATUS = "light_on";
    private static String SWITCH_STATUS = "switch_status";

    boolean switch_status;
    boolean light_status;

    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;
    DatabaseReference lampStatusRef;

    private Handler handler;
    private Runnable runnable;
    private static final int POLLING_INTERVAL = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_smart_lamp);

        ToolbarMainLamp = findViewById(R.id.ToolbarMainLamp);
        BackMainLamp = findViewById(R.id.BackMainLamp);

        SwitchButtonLamp = findViewById(R.id.SwitchButtonLamp);
        imageViewLight = findViewById(R.id.imageLamp);
        myPreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        myEditor = myPreferences.edit();

        switch_status = myPreferences.getBoolean(SWITCH_STATUS, false); //false is default value here
        light_status = myPreferences.getBoolean(LIGHT_STATUS, false);

        SwitchButtonLamp.setChecked(switch_status);

        if (light_status) { //if the light_status is true then the light will be on else it will be off
            imageViewLight.setImageResource(R.drawable.onn_transformed);
        } else {
            imageViewLight.setImageResource(R.drawable.off_transformed);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        lampStatusRef = database.getReference("lampStatus");

        SwitchButtonLamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    imageViewLight.setImageResource(R.drawable.onn_transformed);

                    myEditor.putBoolean(SWITCH_STATUS, true); //here we will set switch status true
                    myEditor.putBoolean(LIGHT_STATUS, true); //and also light status set to true if switch button is onn
                    myEditor.apply();

                } else {
                    imageViewLight.setImageResource(R.drawable.off_transformed);

                    myEditor.putBoolean(SWITCH_STATUS, false); //here we will set switch status false
                    myEditor.putBoolean(LIGHT_STATUS, false); //here we will make it false
                    myEditor.apply();

                }
                //update lamp status in Firebase
                lampStatusRef.child("status").setValue(b);

            }
        });

        ShowUserData();

        BackMainLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackLamp();
            }
        });

        // Initialize handler and runnable for periodic updates
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Fetch the relay status from NodeMCU
                new FetchRelayStatusTask().execute("http://192.168.234.150/datalamp");
                handler.postDelayed(this, POLLING_INTERVAL); // Schedule next run
            }
        };
        handler.postDelayed(runnable, POLLING_INTERVAL); // Start polling
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hentikan polling ketika aktivitas dihancurkan
        handler.removeCallbacks(runnable);
    }


    public void ShowUserData() {
        Intent intent = getIntent();

        String usernameUser = intent.getStringExtra("username");

        ToolbarMainLamp.setText(usernameUser);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Listen for changes in lamp status from Firebase
        lampStatusRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean lampStatus = dataSnapshot.getValue(Boolean.class);
                if (lampStatus != null) {
                    SwitchButtonLamp.setChecked(lampStatus);
                    imageViewLight.setImageResource(lampStatus ? R.drawable.onn_transformed : R.drawable.off_transformed);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    public void BackLamp() {
        String userUsername = ToolbarMainLamp.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(MainSmartLamp.this, Home.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private class FetchRelayStatusTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getInt("lamp_status") == 0; // Assuming lamp_status is 1 for ON and 0 for OFF
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean relayStatus) {
            if (relayStatus != null) {
                SwitchButtonLamp.setChecked(relayStatus);
                imageViewLight.setImageResource(relayStatus ? R.drawable.onn_transformed : R.drawable.off_transformed);
                lampStatusRef.child("status").setValue(relayStatus);
            }
        }
    }
}