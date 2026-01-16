package com.deven.virtualchips;
import com.deven.virtualchips.PlayerAdapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity {

    TextView tvRoomCode;
    Button btnStart;
    RecyclerView rvPlayers;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String roomId;
    String userId;

    PlayerAdapter adapter;
    List<Player> playerList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        tvRoomCode = findViewById(R.id.tvRoomCode);
        btnStart = findViewById(R.id.btnStart);
        rvPlayers = findViewById(R.id.rvPlayers);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        roomId = getIntent().getStringExtra("roomId");
        tvRoomCode.setText("Room Code: " + roomId);

        adapter = new PlayerAdapter(playerList);
        rvPlayers.setLayoutManager(new LinearLayoutManager(this));
        rvPlayers.setAdapter(adapter);

        listenToPlayers();
        checkOwner();
        listenForGameStart();
    }

    private void listenToPlayers() {
        db.collection("rooms")
                .document(roomId)
                .collection("players")
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots == null) return;

                    playerList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Player p = doc.toObject(Player.class);
                        playerList.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void checkOwner() {
        db.collection("rooms")
                .document(roomId)
                .get()
                .addOnSuccessListener(doc -> {
                    String ownerId = doc.getString("ownerId");
                    if (!userId.equals(ownerId)) {
                        btnStart.setVisibility(View.GONE);
                    } else {
                        btnStart.setOnClickListener(v -> startGame());
                    }
                });
    }

    private void startGame() {
        db.collection("rooms")
                .document(roomId)
                .update("status", "active");
    }

    private void listenForGameStart() {
        db.collection("rooms")
                .document(roomId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;

                    String status = snapshot.getString("status");
                    if ("active".equals(status)) {
                        Intent i = new Intent(this, RoomActivity.class);
                        i.putExtra("roomId", roomId);
                        startActivity(i);
                        finish();
                    }
                });
    }
}
