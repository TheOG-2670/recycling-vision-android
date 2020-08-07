package com.prj666.recycling_vision;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MatchHistoryRecyclerAdapter extends RecyclerView.Adapter<MatchHistoryRecyclerAdapter.MatchHistoryViewHolder>
{
    Context context;
    ArrayList<MatchHistoryItem> historyItems;

    public MatchHistoryRecyclerAdapter(Context c, ArrayList<MatchHistoryItem> items)
    {
        if (c != null && items != null)
        {
            this.context = c;
            this.historyItems = items;
        }
    }

    //inflates a new view for the view item
    @NonNull
    @Override
    public MatchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_match_history_recyclerview_item, parent, false);
        return new MatchHistoryViewHolder(v);
    }

    //gets the view item's position in the adapter and binds data to it
    @Override
    public void onBindViewHolder(@NonNull MatchHistoryViewHolder holder, int position)
    {
        MatchHistoryItem item = this.historyItems.get(position);

        holder
    }

    //returns number of items in the adapter's dataset
    @Override
    public int getItemCount()
    {
        return 0;
    }

    //the view item itself
    public class MatchHistoryViewHolder extends RecyclerView.ViewHolder
    {
        ImageView objectImage;
        TextView objectName, recyclingInstructions, probabilityMatch, userID;
        public MatchHistoryViewHolder(@NonNull View itemView)
        {
            super(itemView);
            
        }
    }
}
