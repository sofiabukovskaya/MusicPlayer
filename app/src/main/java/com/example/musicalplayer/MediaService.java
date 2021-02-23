package com.example.musicalplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.session.MediaButtonReceiver;

import java.util.ArrayList;

public class MediaService extends Service {

    public static final String CHANNEL_ID = "channel";



    MediaPlayer mediaPlayer;
    int pauseMusicPosition;
    Handler handler = new Handler();
    ArrayList<Integer> playList;
    int currentIndex = 0;
    Notification notification;
    NotificationManager notificationManager;
    NotificationManagerCompat notificationManagerCompat;
    MediaSessionCompat mediaSessionCompat;
    NotificationChannel channel;
    PendingIntent pendingIntentPrevious;
    PendingIntent pendingIntentStart;
    PendingIntent pendingIntentNext;
    boolean isPlaying;


    Intent intent;


    @Override
    public void onCreate() {
        Log.d("AAA", "onCreate: ");
        playList = new ArrayList<>();
        playList.add(0, R.raw.music);
        playList.add(1, R.raw.song2);
        playList.add(2, R.raw.song3);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), playList.get(currentIndex));
        mediaPlayer.setLooping(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AAA", "onStartCommand: ");

        switch (intent.getAction()) {
            case "START":
                sendMessage();
                mediaPlayer.start();
                if(mediaPlayer.isPlaying()){
                    notificationMethod(R.drawable.pause_image);
                    Log.d("bbb", "onStartCommand START");
                } else {
                    pauseMusic();
                }
                isPlaying = !isPlaying;
                break;
            case "PAUSE":
                if(isPlaying){
                    notificationMethod( R.drawable.play_image);
                    pauseMusic();
                    Log.d("bbb", "onStartCommand PAUSE");
                } else {
                    notificationMethod( R.drawable.pause_image);
                    mediaPlayer.start();
//                    Log.d("bbb", "onStartCommand START");
                }
                isPlaying = !isPlaying;
                break;
            case "RESET":
                resetMusic();
                break;
            case "ACTION":
                intent.getIntExtra("pos",0);
                mediaPlayer.seekTo(intent.getIntExtra("pos",0));
                break;
            case "NEXT":
                playNext();
                Log.d("bbb", "onStartCommand NEXT");
                break;
            case "PREV":
                playRrev();
                Log.d("bbb", "onStartCommand PREV");
                break;

        }
        return START_STICKY;

    }

    private  Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sendMessage();
        }
    };

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
    }

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
        }


    }

    public void sendMessage() {
        Log.d("TAG", "sendMessage: " + mediaPlayer.getCurrentPosition());
        intent = new Intent("custom-event-name");

        intent.putExtra("message", mediaPlayer.getCurrentPosition());
        intent.putExtra("message2", mediaPlayer.getDuration());

        handler.postDelayed(runnable,1000);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void resetMusic() {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
    }

    public void playNext() {
        if(currentIndex < playList.size() -1 ) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer = MediaPlayer.create(getApplicationContext(), playList.get(currentIndex));
        mediaPlayer.start();
        notificationMethod( R.drawable.pause_image);

    }

    public void playRrev(){
        if(currentIndex >0) {
            currentIndex--;
        } else {
            currentIndex = playList.size()-1;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer = MediaPlayer.create(getApplicationContext(), playList.get(currentIndex));
        mediaPlayer.start();

        notificationMethod(R.drawable.pause_image);
    }

    public void notificationMethod(int playButton){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "tag");


                Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.icon);

                Intent intentPrevious = new Intent(getApplicationContext(), MediaService.class)
                        .setAction("PREV");
                pendingIntentPrevious = PendingIntent.getService(this, 1, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent intentPlay = new Intent(getApplicationContext(), MediaService.class)
                    .setAction("PAUSE");
                 pendingIntentStart = PendingIntent.getService(this, 2, intentPlay,PendingIntent.FLAG_UPDATE_CURRENT);

                 Intent intentNext = new Intent(getApplicationContext(), MediaService.class)
                    .setAction("NEXT");
                 pendingIntentNext = PendingIntent.getService(this, 3, intentNext,PendingIntent.FLAG_UPDATE_CURRENT);

                notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("Title song")
                        .setContentText("Artist name")
                        .setLargeIcon(icon)
                        .setOnlyAlertOnce(true)
                        .setShowWhen(false)
                        .addAction(R.drawable.previous_song,"Previous",pendingIntentPrevious)
                        .addAction(R.drawable.play_image,"Play",pendingIntentStart)
                        .addAction(R.drawable.next_song,"Next",pendingIntentNext)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .build();
                notificationManagerCompat.notify(1,notification);

        }

    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID,"music", NotificationManager.IMPORTANCE_LOW);

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager!= null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }



}