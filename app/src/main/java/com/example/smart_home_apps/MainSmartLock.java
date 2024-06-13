package com.example.smart_home_apps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.AsyncTask;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainSmartLock extends AppCompatActivity {

    TextView ToolbarMainLock;
    EditText editTextRFID;
    Button BackMainLock, buttonSubmitRFID;
    SwitchCompat SwitchButtonLock;
    ImageView imageViewLock;

    private static String MY_PREFS = "switch_prefs"; //shared name preferences name
    private static String LIGHT_STATUS = "light_on";
    private static String SWITCH_STATUS = "switch_status";

    boolean switch_status;
    boolean light_status;

    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;
    DatabaseReference lockStatusRef;
    DatabaseReference rfidRef;

    private Handler handler;
    private Runnable runnable;
    private static final int POLLING_INTERVAL = 1000; // 1 second
    private String rfidData = ""; // Deklarasikan variabel rfidData dan inisialisasi dengan string kosong


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_smart_lock);

        ToolbarMainLock = findViewById(R.id.ToolbarMainLock);
        BackMainLock = findViewById(R.id.BackMainLock);
        SwitchButtonLock = findViewById(R.id.SwitchButtonLock);
        imageViewLock = findViewById(R.id.imageLock);
        editTextRFID = findViewById(R.id.editTextRFID);
        buttonSubmitRFID = findViewById(R.id.buttonSubmitRFID);

        myPreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        myEditor = myPreferences.edit();

        switch_status = myPreferences.getBoolean(SWITCH_STATUS, false); // false is default value here
        light_status = myPreferences.getBoolean(LIGHT_STATUS, false);

        SwitchButtonLock.setChecked(switch_status);

        if (light_status) { // if the light_status is true then the light will be on else it will be off
            imageViewLock.setImageResource(R.drawable.lock1);
        } else {
            imageViewLock.setImageResource(R.drawable.unlock);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        lockStatusRef = database.getReference("lockStatus");
        rfidRef = database.getReference("rfidData");


        SwitchButtonLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    imageViewLock.setImageResource(R.drawable.lock1);

                    myEditor.putBoolean(SWITCH_STATUS, true); // here we will set switch status true
                    myEditor.putBoolean(LIGHT_STATUS, true); // and also light status set to true if switch button is on
                    myEditor.apply();

                } else {
                    imageViewLock.setImageResource(R.drawable.unlock);

                    myEditor.putBoolean(SWITCH_STATUS, false); // here we will set switch status false
                    myEditor.putBoolean(LIGHT_STATUS, false); // here we will make it false
                    myEditor.apply();
                }
                // update lock status in Firebase
                lockStatusRef.child("status").setValue(b);
            }
        });

        ShowUserData();

        BackMainLock.setOnClickListener(new View.OnClickListener() {
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
                new FetchDoorStatusTask().execute("http://192.168.234.130/datadoor"); // Adjust the URL as needed
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        };
        handler.postDelayed(runnable, POLLING_INTERVAL);

        // OnClickListener for submitting RFID data
        buttonSubmitRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rfidInput = editTextRFID.getText().toString().trim();
                if (!rfidData.isEmpty()) {
                    new SendRFIDDataTask().execute("http://192.168.234.140/datarfid", rfidData); // Adjust the URL as needed
                } else {
                    // Handle empty RFID data
                    Toast.makeText(MainSmartLock.this, "RFID data is empty", Toast.LENGTH_SHORT).show();
                }
                handleRFIDInput(rfidInput);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop polling when activity is destroyed
        handler.removeCallbacks(runnable);
    }

    public void ShowUserData() {
        Intent intent = getIntent();
        String usernameUser = intent.getStringExtra("username");
        ToolbarMainLock.setText(usernameUser);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for changes in lock status from Firebase
        lockStatusRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean lockStatus = dataSnapshot.getValue(Boolean.class);
                if (lockStatus != null) {
                    SwitchButtonLock.setChecked(lockStatus);
                    imageViewLock.setImageResource(lockStatus ? R.drawable.lock1 : R.drawable.unlock);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    public void BackLamp() {
        String userUsername = ToolbarMainLock.getText().toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // pass the user data intent
                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);
                    Intent intent = new Intent(MainSmartLock.this, Home.class);
                    intent.putExtra("username", usernameFromDB);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void handleRFIDInput(String rfidInput) {
        // Update your UI or perform actions based on RFID input
        // For example, you can display a message or change an image based on the RFID input

        // Here's a simple example of updating a TextView with RFID input
        editTextRFID.setText(rfidInput);

        // You can also update Firebase with RFID data if needed
        rfidRef.child("rfidInput").setValue(rfidInput);
    }


    private class FetchDoorStatusTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    bufferedReader.close();
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getInt("door_status");
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null; // Return null for JSONException
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer doorStatus) {
            if (doorStatus != null) {
                SwitchButtonLock.setChecked(doorStatus == 1); // Assuming 0 is locked, 1 is unlocked
                imageViewLock.setImageResource(doorStatus == 1 ? R.drawable.lock1 : R.drawable.unlock);
                lockStatusRef.child("status").setValue(doorStatus == 1);
            } else {
                Toast.makeText(MainSmartLock.this, "Failed to fetch door status", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SendRFIDDataTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rfid_data", params[1]); // assuming params[1] is the RFID data

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(jsonObject.toString());
                wr.flush();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Success handling if needed
                } else {
                    // Error handling if needed
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

