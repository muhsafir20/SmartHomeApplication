package com.example.smart_home_apps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainSmartSensor extends AppCompatActivity {

    TextView ToolbarMainSensor, TextMainTemperature, TextMainHumidity;
    Button BackMainSensor;
    DatabaseReference sensorsStatusRef;
    private Handler handler;
    private Runnable runnable;

    private static final String TAG = "MainSmartSensor"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_smart_sensor);

        ToolbarMainSensor = findViewById(R.id.ToolbarMainSensor);
        BackMainSensor = findViewById(R.id.BackMainSensor);

        TextMainHumidity = findViewById(R.id.TextMainHumidity);
        TextMainTemperature = findViewById(R.id.TextMainTemperature);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        sensorsStatusRef = FirebaseDatabase.getInstance().getReference("sensorData");

        // Read data from Firebase Realtime Database
        sensorsStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve sensor data from dataSnapshot
                    Double humidity = dataSnapshot.child("humidity").getValue(Double.class);
                    Double temperature = dataSnapshot.child("temperature").getValue(Double.class);

                    // Update UI with sensor data
                    TextMainHumidity.setText(String.valueOf(humidity));
                    TextMainTemperature.setText(String.valueOf(temperature));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });


        ShowUserData();

        BackMainSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackLamp();
            }
        });

        // Initialize the Handler and Runnable
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Execute the AsyncTask
                new GetSensorDataFromJson().execute();
                // Schedule the runnable to run again after 1 second
                handler.postDelayed(this, 500);
            }
        };

        // Start the Runnable
        handler.post(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload sensor data every time the activity becomes visible
        new GetSensorDataFromJson().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to stop the Runnable when the activity is destroyed
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    public void ShowUserData(){
        Intent intent = getIntent();

        String usernameUser = intent.getStringExtra("username");

        ToolbarMainSensor.setText(usernameUser);
    }

    public void BackLamp() {
        String userUsername = ToolbarMainSensor.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //pass the user data intent

                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                    Intent intent = new Intent(MainSmartSensor.this, Home.class);

                    intent.putExtra("username", usernameFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // AsyncTask to fetch sensor data from JSON
    private class GetSensorDataFromJson extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String sensorData = null;

            try {
                URL url = new URL("http://192.168.234.110/datasensor"); // Update the IP address here
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    reader.close();
                    sensorData = stringBuilder.toString();
                } else {
                    Log.e("MainSmartSensor", "Failed to fetch sensor data from JSON, response code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("MainSmartSensor", "Exception: " + e.getMessage());
            }
            return sensorData;
        }

        @Override
        protected void onPostExecute(String sensorData) {
            super.onPostExecute(sensorData);
            // Update UI or perform actions with sensorData
            if (sensorData != null) {
                try {
                    JSONObject jsonObject = new JSONObject(sensorData);
                    Double humidity = jsonObject.getDouble("humidity");
                    Double temperature = jsonObject.getDouble("temperature");

                    // Update UI with sensor data
                    TextMainHumidity.setText(humidity + "%");
                    TextMainTemperature.setText(temperature + "°C");

                    Log.d("MainSmartSensor", "Sensor data from JSON: Humidity - " + humidity + ", Temperature - " + temperature);
                } catch (Exception e) {
                    Log.e("MainSmartSensor", "JSON parsing error: " + e.getMessage());
                }
            } else {
                Log.e("MainSmartSensor", "Failed to retrieve sensor data from JSON");
            }
        }
    }
}