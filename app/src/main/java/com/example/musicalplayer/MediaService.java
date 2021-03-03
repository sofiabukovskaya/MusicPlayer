package com.example.musicalplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.session.MediaButtonReceiver;

import java.util.ArrayList;
import java.util.Collections;

public class MediaService extends Service implements MediaPlayer.OnCompletionListener {

    public static final String CHANNEL_ID = "channel";


    private MediaPlayer mediaPlayer;
    private int pauseMusicPosition;
    private Handler handler = new Handler();
    private ArrayList<Integer> playList = new ArrayList<>();
    public ArrayList<SongList> songLists;

    private Notification notification;
    private NotificationManager notificationManager;
    private NotificationManagerCompat notificationManagerCompat;
    private MediaSessionCompat mediaSessionCompat;
    private NotificationChannel channel;
    private PendingIntent pendingIntentPrevious;
    private PendingIntent pendingIntentStart;
    private PendingIntent pendingIntentNext;


    boolean isPlaying;

    private Integer songsPosition = 0;
    private BroadcastReceiver broadcastReceiverFromAdapter;

    private Intent intent;
    private Intent intentSetTitle;



    @Override
    public void onCreate() {

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverFromAdapter, new IntentFilter("actionFromAdapter"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        addBroadcastFromAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        switch (intent.getAction()) {
            case "PLAY_SELECTED_SONG":
                Bundle bundle = intent.getExtras();
                songsPosition = bundle.getInt("songPosition");
                if(mediaPlayer!=null) {
                    mediaPlayer.release();
                }
                mediaPlayer = MediaPlayer.create(getApplicationContext(), playList.get(songsPosition));
                mediaPlayer.setOnCompletionListener(this);
                Collections.swap(playList,songsPosition,0);
                mediaPlayer.start();
                sendMessage();
                sendTitleSong();

                if (mediaPlayer.isPlaying()) {
                    Intent intentFragment = new Intent("startPlayer");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentFragment);
                } else {
                    pauseMusic();
                    sendTitleSong();
                }
                isPlaying = !isPlaying;
                break;
            case "LIST":
                songLists = intent.getParcelableArrayListExtra("song");
                for (SongList song : songLists) {
                         playList.add(song.getSong());
                    }
                sendTitleSong();
                break;
            case "UPDATE_LIST":
                songLists.clear();
                playList.clear();
                songLists = intent.getParcelableArrayListExtra("updateSong");
                for (SongList song : songLists) {
                    playList.add(song.getSong());
                }
                sendTitleSong();
                break;
            case "START":
                sendMessage();
                mediaPlayer.start();
                sendTitleSong();
                if (mediaPlayer.isPlaying()) {
                    notificationMethod(R.drawable.pause_image);
                    sendTitleSong();
                } else {
                    pauseMusic();
                    sendTitleSong();
                    intent.setAction("PAUSE");
                    startService(intent);
                }
                isPlaying = !isPlaying;
                break;
            case "PAUSE":
                if (isPlaying) {
                    notificationMethod(R.drawable.play_image);
                    sendTitleSong();
                    pauseMusic();
                } else if(mediaPlayer == null) {
                    Toast.makeText(this, "Please select a song!", Toast.LENGTH_SHORT).show();
                } else {
                    pauseMusic();
                    notificationMethod(R.drawable.pause_image);
                }
                isPlaying = !isPlaying;
                break;
            case "RESET":
                resetMusic();
                sendTitleSong();
                break;
            case "CHANGE_PROGRESS_SONG":
                intent.getIntExtra("position", 0);
                mediaPlayer.seekTo(intent.getIntExtra("position", 0));
                sendTitleSong();
                break;
            case "NEXT":
                playNextSong();
                sendTitleSong();
                break;
            case "PREVIOUS":
                playPreviousSong();
                sendTitleSong();
                break;
            case "DELETE":
                songLists.clear();
                playList.clear();
                songLists = intent.getParcelableArrayListExtra("updateSongAfterDelete");
                for (SongList song : songLists) {
                    playList.add(song.getSong());
                }
                songsPosition = 0;
                playNextSong();
                sendTitleSong();
                break;
        }
        return START_STICKY;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sendMessage();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void pauseMusic() {
        if (mediaPlayer != null && isPlaying()) {
            mediaPlayer.pause();
            pauseMusicPosition = mediaPlayer.getCurrentPosition();
            notificationMethod(R.drawable.pause_image);
            sendTitleSong();
        }

    }

    public void sendTitleSong(){
        intentSetTitle = new Intent("titleSong");

        intentSetTitle.putExtra("titleSong", songLists.get(songsPosition).getName());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSetTitle);
    }

    public void sendMessage() {
        intent = new Intent("sendMessageToFragmentCurrentMusic");

        intent.putExtra("mediaCurrentPosition", mediaPlayer.getCurrentPosition());
        intent.putExtra("mediaDuration", mediaPlayer.getDuration());

        handler.postDelayed(runnable, 1000);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void resetMusic() {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
    }

    public void playNextSong() {
        if (songsPosition < playList.size() - 1) {
            songsPosition++;
        } else {
            songsPosition = 0;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), playList.get(songsPosition));
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.start();
        notificationMethod(R.drawable.pause_image);

    }

    public void addBroadcastFromAdapter(){
        broadcastReceiverFromAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(mediaPlayer!=null){
                    mediaPlayer.release();
                }
                playNextSong();
                mediaPlayer.start();
                sendTitleSong();
            }
        };
    }

    public void playPreviousSong() {
        if (songsPosition > 0) {
            songsPosition--;
        } else {
            songsPosition = playList.size() - 1;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer = MediaPlayer.create(getApplicationContext(), playList.get(songsPosition));
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.start();

        notificationMethod(R.drawable.pause_image);
    }

    public void notificationMethod(int playButton) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
            mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Notification_TAG");


            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

            setIntentsForNotification();

            notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("Title song")
                    .setContentText("Artist name")
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(R.drawable.previous_song, "Previous", pendingIntentPrevious)
                    .addAction(R.drawable.play_image, "Play", pendingIntentStart)
                    .addAction(R.drawable.next_song, "Next", pendingIntentNext)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();
            notificationManagerCompat.notify(1, notification);
            sendTitleSong();

        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, "Channel_music", NotificationManager.IMPORTANCE_LOW);

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void setIntentsForNotification(){
        Intent intentPrevious = new Intent(getApplicationContext(), MediaService.class)
                .setAction("PREV");
        pendingIntentPrevious = PendingIntent.getService(this, 1, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlay = new Intent(getApplicationContext(), MediaService.class)
                .setAction("PAUSE");
        pendingIntentStart = PendingIntent.getService(this, 2, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(getApplicationContext(), MediaService.class)
                .setAction("NEXT");
        pendingIntentNext = PendingIntent.getService(this, 3, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextSong();
        goToLast();
        sendTitleSong();
    }

    public void goToLast(){
        sendTitleSong();
        Collections.swap(playList, songsPosition, playList.size()-1);
        Intent intent3 = new Intent("actionGoToLast");
        intent3.putExtra("mediaCurrentPosition", songsPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent3);

    }
}