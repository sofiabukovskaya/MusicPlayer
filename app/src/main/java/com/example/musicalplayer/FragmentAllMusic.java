package com.example.musicalplayer;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentAllMusic extends Fragment implements SongListAdapter.CallBackPosition {

    private RecyclerView recyclerView;
    private SongListAdapter songListAdapter;
    private ArrayList<SongList> playlist = new ArrayList<>();
    private FloatingActionButton floatingActionButton;
    private Integer lastPosition;

    private BroadcastReceiver broadcastReceiver;
    private Integer songPosition = 0;


    public FragmentAllMusic() {

    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, new IntentFilter("actionGoToLast"));
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_music, container, false);
        getList();
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        floatingActionButton = rootView.findViewById(R.id.floatingActionButtonAdd);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        songListAdapter = new SongListAdapter(getActivity(), playlist, this);
        recyclerView.setAdapter(songListAdapter);
        setHasOptionsMenu(true);

      addBroadcast();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = new Intent(getActivity(), MediaService.class);
        intent.putParcelableArrayListExtra("song",playlist);
        intent.setAction("LIST");
        getActivity().startService(intent);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertDialogForAddSong(intent);
            }
        });
    }

    public void createAlertDialogForAddSong(Intent intent){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Do yo wanna add a new song?")
                .setMessage("Please, select")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lastPosition = (playlist.size()-1) + 1;
                        playlist.clear();
                        getList();
                        addItem(lastPosition);
                        intent.putParcelableArrayListExtra("updateSong",playlist);
                        songListAdapter.notifyDataSetChanged();
                        intent.setAction("UPDATE_LIST");
                        getActivity().startService(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Okay", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }

    public  void addItem(int currentPosition){
        playlist.add(currentPosition, new SongList(4,"Sub Urban - Cradles", R.raw.song4));
        songListAdapter.notifyItemInserted(currentPosition);
        songListAdapter.notifyDataSetChanged();
    }

    public void updatePosition(int position){
        Collections.swap(playlist,position,0);
        songListAdapter.notifyItemMoved(position,0);
    }

    public void addBroadcast(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                songPosition = intent.getIntExtra("mediaCurrentPosition",songPosition);
                songListAdapter.notifyItemMoved(songPosition,playlist.size()-1);
                Collections.swap(playlist, songPosition, playlist.size()-1);
            }
        };
    }


    private void getList() {
        playlist.add(new SongList(1, "Joji - Run",R.raw.music));
        playlist.add(new SongList(2, "Powfu - Death Bed",R.raw.song2));
        playlist.add(new SongList(3, "Alt-j - Breezeblocks",R.raw.song3));
    }

    @Override
    public void songClicked(int position) {
        Intent intent = new Intent(getActivity(), MediaService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("songPosition", position);
        intent.putExtras(bundle);
        updatePosition(position);
        songListAdapter.notifyItemMoved(position,0);
        songListAdapter.notifyDataSetChanged();
        intent.setAction("PLAY_SELECTED_SONG");
        getActivity().startService(intent);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list,menu);

        MenuItem menuItem = menu.findItem(R.id.item_search);
        SearchView searchView = (SearchView)menuItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                songListAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}