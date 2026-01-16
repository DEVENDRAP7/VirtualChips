package com.deven.virtualchips;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private final List<Player> playerList;

    public PlayerAdapter(List<Player> playerList) {
        this.playerList = playerList;
    }

        @NonNull
        @Override
        public PlayerViewHolder onCreateViewHolder (@NonNull ViewGroup parent,int viewType){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_player, parent, false);
            return new PlayerViewHolder(view);
        }

        @Override
        public void onBindViewHolder (@NonNull PlayerViewHolder holder,int position){
            Player player = playerList.get(position);
            holder.tvName.setText(player.name);
            holder.tvBalance.setText(String.valueOf(player.balance));
        }

        @Override
        public int getItemCount () {
            return playerList.size();
        }

        // ⚠️ CLASS NAME AND CONSTRUCTOR NAME MUST MATCH
        static class PlayerViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvBalance;

            public PlayerViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPlayerName);
                tvBalance = itemView.findViewById(R.id.tvBalance);
            }
        }
}
