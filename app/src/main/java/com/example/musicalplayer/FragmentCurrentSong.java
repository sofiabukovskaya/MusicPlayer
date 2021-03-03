package com.example.musicalplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


public class FragmentCurrentSong extends Fragment implements View.OnClickListener {

    private ImageButton buttonPlaySong;
    private ImageButton buttonStopSong;
    private ImageButton buttonResetMusic;
    private ImageButton buttonPrevSong;
    private ImageButton buttonNextSong;

    private TextView songTitle;
    private TextView textViewStartSeekBar;
    private TextView textViewEndSeekBar;

    private Handler seekHandler = new Handler();
    private Integer mediaCurrentPosition = 0;
    private Integer mediaDuration = 0;
    private boolean isPlaying;

    private SeekBar seekBar;
    private SeekBar seekBarVolume;

    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiverFromService;
    private BroadcastReceiver broadcastReceiverGetTitle;

    private AudioManager audioManager;

    private String titleSong;

    public FragmentCurrentSong() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_current_song, container, false);

        buttonPlaySong = (ImageButton) rootView.findViewById(R.id.buttonPlay);
        buttonStopSong = (ImageButton) rootView.findViewById(R.id.buttonStop);
        buttonResetMusic = (ImageButton) rootView.findViewById(R.id.buttonRepeat);
        textViewStartSeekBar = (TextView) rootView.findViewById(R.id.textViewStartSeekBar);
        textViewEndSeekBar = (TextView) rootView.findViewById(R.id.textViewEndSeekBar);
        buttonNextSong = (ImageButton) rootView.findViewById(R.id.nextSongButton);
        buttonPrevSong = (ImageButton) rootView.findViewById(R.id.prevSongButton);
        songTitle = (TextView)rootView.findViewById(R.id.songTitle);

        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBarVolume = (SeekBar) rootView.findViewById(R.id.seekBarVolume);

        buttonPlaySong.setOnClickListener(this);
        buttonStopSong.setOnClickListener(this);
        buttonResetMusic.setOnClickListener(this);
        buttonPrevSong.setOnClickListener(this);
        buttonNextSong.setOnClickListener(this);


        seekBarProgressSong();
        seekBarVolumeControl();

        setBroadcastes();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("sendMessageToFragmentCurrentMusic"));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiverFromService, new IntentFilter("startPlayer"));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiverGetTitle, new IntentFilter("titleSong"));

        super.onResume();
    }

    private void seekBarProgressSong() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent intent = new Intent(getActivity(), MediaService.class);
                    intent.setAction("CHANGE_PROGRESS_SONG");
                    intent.putExtra("position", progress);
                    getActivity().startService(intent);
                }
                setTimeDuration(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textViewStartSeekBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), MediaService.class);
        switch (v.getId()) {
            case R.id.buttonPlay:
                buttonPlaySong.setImageResource(R.drawable.pause_image);
                if (isPlaying) {
                    updateSeekBar();
                    intent.setAction("START");
                    getActivity().startService(intent);
                } else {
                    intent.setAction("PAUSE");
                    getActivity().startService(intent);
                    buttonPlaySong.setImageResource(R.drawable.play_image);
                }
                isPlaying = !isPlaying;
                break;
            case R.id.buttonStop:
                getActivity().stopService(intent);
                buttonPlaySong.setImageResource(R.drawable.play_image);
                break;
            case R.id.buttonRepeat:
                intent.setAction("RESET");
                getActivity().startService(intent);
                break;
            case R.id.nextSongButton:
                intent.setAction("NEXT");
                getActivity().startService(intent);
                buttonPlaySong.setImageResource(R.drawable.pause_image);
                break;
            case R.id.prevSongButton:
                intent.setAction("PREVIOUS");
                getActivity().startService(intent);
                buttonPlaySong.setImageResource(R.drawable.pause_image);
                break;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };

    public void updateSeekBar() {
        seekBar.setProgress(mediaCurrentPosition);
        seekBar.setMax(mediaDuration);
        seekHandler.postDelayed(runnable, 500);
    }

    private void seekBarVolumeControl() {
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        seekBarVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setBroadcastes(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mediaCurrentPosition = intent.getIntExtra("mediaCurrentPosition", 0);
                mediaDuration = intent.getIntExtra("mediaDuration", 0);
            }
        };

        broadcastReceiverFromService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                buttonPlaySong.setImageResource(R.drawable.pause_image);
                updateSeekBar();
            }
        };

        broadcastReceiverGetTitle = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                titleSong = intent.getStringExtra("titleSong");
                songTitle.setText(titleSong);
            }
        };

    }

    public void setTimeDuration(int progress){
        final long progressMinutes = (progress / 1000) / 60;
        final long progressSec = ((progress / 1000) % 60);

        textViewStartSeekBar.setText(progressMinutes + ":" + progressSec);

        final long endSeekBarMinutes = (mediaDuration / 1000) / 60;
        final long endSeekBarSec = ((mediaDuration / 1000) % 60);

        textViewEndSeekBar.setText(endSeekBarMinutes + ":" + endSeekBarSec);

    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiverFromService);
        super.onPause();
    }
}