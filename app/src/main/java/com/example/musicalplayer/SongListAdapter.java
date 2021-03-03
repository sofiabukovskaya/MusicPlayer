package com.example.musicalplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> implements Filterable {

    private Context context;
    private ArrayList<SongList> songLists;
    private ArrayList<SongList> songListItem;
    private CallBackPosition callBackPosition;
    private Intent intent;

    @Override
    public Filter getFilter() {
        return filterList;
    }

    public interface CallBackPosition{
            void songClicked(int position);
    }

    public SongListAdapter(Context context, ArrayList<SongList> songLists, CallBackPosition callBackPosition) {
        this.context = context;
        this.songLists = songLists;
        this.callBackPosition = callBackPosition;
        songListItem = new ArrayList<>(songLists);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SongList dataSongList = songLists.get(position);
            holder.textViewId.setText(String.valueOf(dataSongList.getId()));
            holder.textViewName.setText(dataSongList.getName());
            holder.textViewName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callBackPosition!=null){
                        callBackPosition.songClicked(position);
                    }
                }
            });
            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createAlertDelete(position);
                }
            });
    }

    @Override
    public int getItemCount() {
        return songLists.size();
    }

    public void createAlertDelete(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to remove this song from the playlist?")
                .setMessage("Please, select")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(position);
                        Toast.makeText(context, "Your song is deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Okay, your song in safe", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }

    public void deleteItem(int position){
        songLists.remove(position);
        notifyDataSetChanged();
        intent = new Intent(context, MediaService.class);
        intent.putParcelableArrayListExtra("updateSongAfterDelete",songLists);
        intent.setAction("DELETE");
        context.startService(intent);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewId, textViewName;
        ImageButton buttonDelete;
        FloatingActionButton floatingActionButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewId = itemView.findViewById(R.id.textViewID);
            textViewName = itemView.findViewById(R.id.textViewName);
            buttonDelete = itemView.findViewById(R.id.deleteButton);
            floatingActionButton = itemView.findViewById(R.id.floatingActionButtonAdd);
        }
    }

    private Filter filterList = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SongList> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length()==0){
                filteredList.addAll(songListItem);
            } else {
                String filter = constraint.toString().toLowerCase().trim();
                for (SongList name: songListItem) {
                    if(name.getName().toLowerCase().contains(filter)){
                        filteredList.add(name);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            songLists.clear();
            songLists.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
