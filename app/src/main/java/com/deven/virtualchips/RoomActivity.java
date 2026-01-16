package com.deven.virtualchips;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomActivity extends AppCompatActivity {

    // UI
    TextView tvPool;
    RecyclerView rvBalances;
    EditText etAmount;
    Button btnAddPool, btnTakePool, btnAllIn, btnEndGame, btnLeaveRoom;

    // Firebase
    FirebaseFirestore db;
    FirebaseAuth auth;

    // Data
    String roomId;
    String userId;

    PlayerAdapter adapter;
    List<Player> playerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // Bind UI
        tvPool = findViewById(R.id.tvPool);
        rvBalances = findViewById(R.id.rvBalances);
        etAmount = findViewById(R.id.etAmount);
        btnAddPool = findViewById(R.id.btnAddPool);
        btnTakePool = findViewById(R.id.btnTakePool);
        btnAllIn = findViewById(R.id.btnAllIn);
        btnEndGame = findViewById(R.id.btnEndGame);
        btnLeaveRoom = findViewById(R.id.btnLeaveRoom);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        roomId = getIntent().getStringExtra("roomId");

        // RecyclerView
        adapter = new PlayerAdapter(playerList);
        rvBalances.setLayoutManager(new LinearLayoutManager(this));
        rvBalances.setAdapter(adapter);

        // Listeners
        listenToRoom();
        listenToPlayers();
        setupOwnerControls();

        // Actions
        btnAddPool.setOnClickListener(v -> addToPool());
        btnTakePool.setOnClickListener(v -> takeFromPool());
        btnAllIn.setOnClickListener(v -> allIn());
        btnEndGame.setOnClickListener(v -> endGame());
        btnLeaveRoom.setOnClickListener(v -> leaveRoom());
    }

    // ================= REAL-TIME LISTENERS =================

    private void listenToRoom() {
        db.collection("rooms")
                .document(roomId)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot == null || !snapshot.exists()) return;

                    Long pool = snapshot.getLong("poolAmount");
                    if (pool != null) {
                        tvPool.setText("Pool: " + pool);
                    }

                    String status = snapshot.getString("status");
                    if ("ended".equals(status)) {
                        Toast.makeText(this, "Game ended", Toast.LENGTH_SHORT).show();
                        deleteRoomIfOwner();
                        finish();
                    }
                });
    }

    private void listenToPlayers() {
        db.collection("rooms")
                .document(roomId)
                .collection("players")
                .addSnapshotListener((snapshots, error) -> {
                    if (snapshots == null) return;

                    playerList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Player p = doc.toObject(Player.class);
                        playerList.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // ================= OWNER CONTROLS =================

    private void setupOwnerControls() {
        db.collection("rooms")
                .document(roomId)
                .get()
                .addOnSuccessListener(doc -> {
                    String ownerId = doc.getString("ownerId");
                    if (userId.equals(ownerId)) {
                        btnEndGame.setVisibility(View.VISIBLE);
                    } else {
                        btnEndGame.setVisibility(View.GONE);
                    }
                });
    }

    // ================= ACTION HELPERS =================

    private int getEnteredAmount() {
        String val = etAmount.getText().toString().trim();
        if (val.isEmpty()) return -1;
        return Integer.parseInt(val);
    }

    // ================= ADD TO POOL =================

    private void addToPool() {
        int amount = getEnteredAmount();
        if (amount <= 0) {
            Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference roomRef = db.collection("rooms").document(roomId);
        DocumentReference playerRef =
                roomRef.collection("players").document(userId);

        db.runTransaction(transaction -> {

            DocumentSnapshot roomSnap = transaction.get(roomRef);
            DocumentSnapshot playerSnap = transaction.get(playerRef);

            int pool = roomSnap.getLong("poolAmount").intValue();
            int balance = playerSnap.getLong("balance").intValue();

            Map<String, Object> rules =
                    (Map<String, Object>) roomSnap.get("rules");

            int minSpend = ((Long) rules.get("minSpend")).intValue();

            if (amount < minSpend)
                throw new RuntimeException("Below minimum bet");

            if (balance < amount)
                throw new RuntimeException("Insufficient balance");

            transaction.update(playerRef, "balance", balance - amount);
            transaction.update(roomRef, "poolAmount", pool + amount);

            return null;
        }).addOnFailureListener(e ->
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // ================= TAKE FROM POOL =================

    private void takeFromPool() {
        int amount = getEnteredAmount();
        if (amount <= 0) {
            Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference roomRef = db.collection("rooms").document(roomId);
        DocumentReference playerRef =
                roomRef.collection("players").document(userId);

        db.runTransaction(transaction -> {

            DocumentSnapshot roomSnap = transaction.get(roomRef);
            DocumentSnapshot playerSnap = transaction.get(playerRef);

            int pool = roomSnap.getLong("poolAmount").intValue();
            int balance = playerSnap.getLong("balance").intValue();

            if (pool < amount)
                throw new RuntimeException("Pool has insufficient amount");

            transaction.update(roomRef, "poolAmount", pool - amount);
            transaction.update(playerRef, "balance", balance + amount);

            return null;
        }).addOnFailureListener(e ->
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // ================= ALL IN =================

    private void allIn() {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        DocumentReference playerRef =
                roomRef.collection("players").document(userId);

        db.runTransaction(transaction -> {

            DocumentSnapshot roomSnap = transaction.get(roomRef);
            DocumentSnapshot playerSnap = transaction.get(playerRef);

            int pool = roomSnap.getLong("poolAmount").intValue();
            int balance = playerSnap.getLong("balance").intValue();

            if (balance <= 0)
                throw new RuntimeException("Nothing to go ALL IN");

            transaction.update(playerRef, "balance", 0);
            transaction.update(roomRef, "poolAmount", pool + balance);

            return null;
        }).addOnFailureListener(e ->
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // ================= END / LEAVE / CLEANUP =================

    private void endGame() {
        db.collection("rooms")
                .document(roomId)
                .update("status", "ended");
    }

    private void leaveRoom() {
        db.collection("rooms")
                .document(roomId)
                .collection("players")
                .document(userId)
                .delete()
                .addOnSuccessListener(v -> finish());
    }

    private void deleteRoomIfOwner() {
        db.collection("rooms")
                .document(roomId)
                .get()
                .addOnSuccessListener(doc -> {
                    String ownerId = doc.getString("ownerId");
                    if (userId.equals(ownerId)) {
                        db.collection("rooms").document(roomId).delete();
                    }
                });
    }
}