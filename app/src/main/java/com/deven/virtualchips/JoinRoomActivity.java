package com.deven.virtualchips;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class JoinRoomActivity extends AppCompatActivity {

    EditText etRoomCode;
    Button btnJoin, back;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        etRoomCode = findViewById(R.id.etRoomCode);
        btnJoin = findViewById(R.id.btnJoin);
        back=findViewById(R.id.btnback);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnJoin.setOnClickListener(v -> joinRoom());
        back.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void joinRoom() {

        String roomId = etRoomCode.getText().toString().trim();

        if (roomId.isEmpty()) {
            Toast.makeText(this, "Enter room code", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        DocumentReference roomRef =
                db.collection("rooms").document(roomId);

        roomRef.get().addOnSuccessListener(roomDoc -> {

            if (!roomDoc.exists()) {
                Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String status = roomDoc.getString("status");
            if (!"waiting".equals(status)) {
                Toast.makeText(this, "Game already started", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> rules =
                    (Map<String, Object>) roomDoc.get("rules");

            int maxPlayers =
                    ((Long) rules.get("maxPlayers")).intValue();

            roomRef.collection("players")
                    .get()
                    .addOnSuccessListener(playersSnap -> {

                        if (playersSnap.size() >= maxPlayers) {
                            Toast.makeText(this, "Room is full", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int startingBalance =
                                ((Long) rules.get("startingBalance")).intValue();
                        SharedPreferences prefs =
                                getSharedPreferences("player_prefs", MODE_PRIVATE);

                        String playerName = prefs.getString("player_name", "Player");


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

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error joining room", Toast.LENGTH_SHORT).show()
        );
    }
}
