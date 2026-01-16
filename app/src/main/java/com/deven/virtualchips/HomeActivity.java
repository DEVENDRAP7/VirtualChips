package com.deven.virtualchips;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        EditText etPlayerName = findViewById(R.id.etPlayerName);

        SharedPreferences prefs =
                getSharedPreferences("player_prefs", MODE_PRIVATE);

        String savedName = prefs.getString("player_name", "");
        etPlayerName.setText(savedName);

        Button btnCreate = findViewById(R.id.btnCreateRoom);
        Button btnJoin = findViewById(R.id.btnJoinRoom);

        btnCreate.setOnClickListener(v -> {
            String name = etPlayerName.getText().toString().trim();
            if (name.isEmpty()) {
                etPlayerName.setError("Enter your name");
                return;
            }
            saveName(name);
            startActivity(new Intent(this, CreateRoomActivity.class));
        });

        btnJoin.setOnClickListener(v -> {
            String name = etPlayerName.getText().toString().trim();
            if (name.isEmpty()) {
                etPlayerName.setError("Enter your name");
                return;
            }
            saveName(name);
            startActivity(new Intent(this, JoinRoomActivity.class));
        });
    }
    private void saveName(String name) {
        SharedPreferences prefs =
                getSharedPreferences("player_prefs", MODE_PRIVATE);
        prefs.edit().putString("player_name", name).apply();
    }

}
