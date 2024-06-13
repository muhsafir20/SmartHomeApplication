package com.example.smart_home_apps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONObject;

public class MainSmartGas extends AppCompatActivity {

    TextView ToolbarMainGas, TextMainGas;
    Button BackMainGas;
    DatabaseReference gasStatusRef;
    private Handler handler;
    private Runnable runnable;


    private static final String TAG = "MainSmartGas"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_smart_gas);

        ToolbarMainGas = findViewById(R.id.ToolbarMainGas);
        BackMainGas = findViewById(R.id.BackMainGas);
        TextMainGas = findViewById(R.id.TextMainGas);

        // Initialize Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        gasStatusRef = firebaseDatabase.getReference("gasData");

        // Read data from Firebase Realtime Database
        gasStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String gasStatus = dataSnapshot.getValue(String.class);
                    TextMainGas.setText(gasStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });

        showUserData();

        BackMainGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backLamp();
            }
        });

        // Initialize the Handler and Runnable
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Execute the AsyncTask
                new GetGasDataFromJson().execute();
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
        new GetGasDataFromJson().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to stop the Runnable when the activity is destroyed
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
    public void showUserData() {
        Intent intent = getIntent();
        String usernameUser = intent.getStringExtra("username");
        ToolbarMainGas.setText(usernameUser);
    }



    public void backLamp() {
        String userUsername = ToolbarMainGas.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Pass the user data intent
                    String usernameFromDB = snapshot.child(userUsername).child("username"

                    ).getValue(String.class);

                    Intent intent = new Intent(MainSmartGas.this, Home.class);
                    intent.putExtra("username", usernameFromDB);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    // Metode AsyncTask untuk mengambil data gas dari JSON
    private class GetGasDataFromJson extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String gasValue = null;

            try {
                URL url = new URL("http://192.168.234.100/datagas");
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

                    // Parsing JSON response to get gas_value
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    gasValue = jsonObject.getString("gas_value");
                } else {
                    Log.e(TAG, "Failed to fetch gas data from JSON, response code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }

            return gasValue;
        }

        @Override
        protected void onPostExecute(String gasValue) {
            super.onPostExecute(gasValue);
            // Update UI or perform actions with gasValue
            if (gasValue != null) {
                // Gas value retrieved successfully, update UI
                TextMainGas.setText(gasValue + "m³");  // Update the TextView with the gas value
                Log.d(TAG, "Gas value from JSON: " + gasValue);
            } else {
                Log.e(TAG, "Failed to retrieve gas value from JSON");
            }
        }
    }

}