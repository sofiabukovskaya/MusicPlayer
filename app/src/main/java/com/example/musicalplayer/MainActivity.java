package com.example.musicalplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton buttonPlaySong;
    private ImageButton buttonStopSong;
    private ImageButton buttonResetMusic;
    private TextView textViewStartSeekBar;
    private TextView textViewEndSeekBar;
    private ImageButton buttonPrevSong;
    private ImageButton buttonNextSong;

    Handler seekHandler = new Handler();
    Integer message = 0;
    Integer message2 =0;




    private SeekBar seekBar;
    private SeekBar seekBarVolume;
    BroadcastReceiver broadcastReceiver;

    private AudioManager audioManager;

    private boolean isPlaying;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPlaySong = findViewById(R.id.buttonPlay);
        buttonStopSong = findViewById(R.id.buttonStop);
        buttonResetMusic = findViewById(R.id.buttonRepeat);
        textViewStartSeekBar = findViewById(R.id.textViewStartSeekBar);
        textViewEndSeekBar = findViewById(R.id.textViewEndSeekBar);
        buttonNextSong = findViewById(R.id.nextSongButton);
        buttonPrevSong = findViewById(R.id.prevSongButton);

        seekBar = findViewById(R.id.seekBar);
        seekBarVolume = findViewById(R.id.seekBarVolume);

        buttonPlaySong.setOnClickListener(this);
        buttonStopSong.setOnClickListener(this);
        buttonResetMusic.setOnClickListener(this);
        buttonPrevSong.setOnClickListener(this);
        buttonNextSong.setOnClickListener(this);

        seekBarProgressSong();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                message = intent.getIntExtra("message",0 );
                message2 = intent.getIntExtra("message2",0);

            }
        };
        seekBarVolumeControl();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("custom-event-name"));
        super.onResume();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };


    public void updateSeekBar() {
       seekBar.setProgress(message);
       seekBar.setMax(message2);
       seekHandler.postDelayed(runnable, 500);
    }




    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, MediaService.class);
        switch (v.getId()){
            case R.id.buttonPlay:
                buttonPlaySong.setImageResource(R.drawable.pause_image);
                if(isPlaying) {
                    intent.setAction("START");
                    startService(intent);
                    updateSeekBar();
                } else {
                    intent.setAction("PAUSE");
                    startService(intent);
                    buttonPlaySong.setImageResource(R.drawable.play_image);
                }
                isPlaying = !isPlaying;
                break;
            case R.id.buttonStop:
                stopService(intent);
                buttonPlaySong.setImageResource(R.drawable.play_image);
                break;
            case R.id.buttonRepeat:
                intent.setAction("RESET");
                startService(intent);
                break;
            case R.id.nextSongButton:
                intent.setAction("NEXT");
                startService(intent);
                buttonPlaySong.setImageResource(R.drawable.pause_image);
                break;
            case R.id.prevSongButton:
                intent.setAction("PREV");
                startService(intent);
                buttonPlaySong.setImageResource(R.drawable.pause_image);
                break;
        }

    }

    private void seekBarProgressSong(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    Intent intent = new Intent(MainActivity.this, MediaService.class);
                    intent.setAction("ACTION");
                    intent.putExtra("pos", progress);
                    startService(intent);
                }

                final long progressMinutes = (progress/1000)/60;
                final long progressSec = ((progress/1000)%60);

                textViewStartSeekBar.setText(progressMinutes + ":" + progressSec);

                final long endSeekBarMinutes = (message2/1000)/60;
                final long endSeekBarSec = ((message2/1000)%60);

                textViewEndSeekBar.setText(endSeekBarMinutes + ":" + endSeekBarSec);


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

    private void seekBarVolumeControl() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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


}