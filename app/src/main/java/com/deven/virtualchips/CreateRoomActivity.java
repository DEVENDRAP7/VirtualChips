package com.deven.virtualchips;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.*;

public class CreateRoomActivity extends AppCompatActivity {

    EditText  etMaxPlayers, etStartingBalance, etMinSpend;
    RadioGroup rgMode;
    Button btnCreate;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        etMaxPlayers = findViewById(R.id.etMaxPlayers);
        etStartingBalance = findViewById(R.id.etStartingBalance);
        etMinSpend = findViewById(R.id.etMinSpend);
        rgMode = findViewById(R.id.rgMode);
        btnCreate = findViewById(R.id.btnCreateRoom);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnCreate.setOnClickListener(v -> createRoom());
    }

    private void createRoom() {

        int maxPlayers = Integer.parseInt(etMaxPlayers.getText().toString());
        int startingBalance = Integer.parseInt(etStartingBalance.getText().toString());
        int minSpend = Integer.parseInt(etMinSpend.getText().toString());

        boolean fixedAmount =
                rgMode.getCheckedRadioButtonId() == R.id.rbFixed;

        String roomId = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );
        SharedPreferences prefs =
                getSharedPreferences("player_prefs", MODE_PRIVATE);

        String playerName = prefs.getString("player_name", "Player");


        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> rules = new HashMap<>();
        rules.put("maxPlayers", maxPlayers);
        rules.put("startingBalance", startingBalance);
        rules.put("minSpend", minSpend);
        rules.put("fixedAmount", fixedAmount);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("ownerId", userId);
        roomData.put("status", "waiting");
        roomData.put("poolAmount", 0);
        roomData.put("rules", rules);
        roomData.put("createdAt", System.currentTimeMillis());

        DocumentReference roomRef =
                db.collection("rooms").document(roomId);

        roomRef.set(roomData).addOnSuccessListener(unused -> {

            Map<String, Object> player = new HashMap<>();
            player.put("name", playerName);
            player.put("balance", startingBalance);


            roomRef.collection("players")
                    .document(userId)
                    .set(player)
                    .addOnSuccessListener(v -> {
                        Intent i = new Intent(this, LobbyActivity.class);
                        i.putExtra("roomId", roomId);
                        startActivity(i);
                        finish();
                    });
        });
    }
}

