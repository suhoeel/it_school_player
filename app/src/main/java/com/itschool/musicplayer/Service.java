package com.itschool.musicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import java.io.IOException;

/**
 * service for playing music
 */
public class Service extends android.app.Service implements MediaPlayerStateListener {

    final HWListener hwListener;
    final Notifications notifications;
    private AudioPlayer audioPlayer;

    //    private final Playlist playlist;
//    private Iterator<Playlist.Entry> iterator;
    public Service() {
        hwListener = new HWListener(this);
        notifications = new Notifications(this);
//        playlist = new Playlist(this);
    }

    /**
     * unused
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * setup
     */
    @Override
    public void onCreate() {
        hwListener.create();
        notifications.create();
        super.onCreate();
    }


    private void playEntryFromPlaylist() {
        try {

            /* get audio playback logic and start async */
            audioPlayer = new AudioPlayer(this, Uri.parse("https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"));
            audioPlayer.start();

            /* create notification for playback control */
            notifications.getNotification("IT_School Player", false, false);

            /* start service as foreground */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                startForeground(Notifications.NOTIFICATION_ID, notifications.notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            else startForeground(Notifications.NOTIFICATION_ID, notifications.notification);

        } catch (IllegalArgumentException e) {
            Exceptions.throwError(this, Exceptions.IllegalArgument);
            playOrDestroy();
        } catch (SecurityException e) {
            Exceptions.throwError(this, Exceptions.Security);
            playOrDestroy();
        } catch (IllegalStateException e) {
            Exceptions.throwError(this, Exceptions.IllegalState);
            playOrDestroy();
        } catch (IOException e) {
            Exceptions.throwError(this, Exceptions.IO);
            playOrDestroy();
        }
    }

    public void playOrDestroy() {
//        if (!playNextEntry())
        onMediaPlayerDestroy();
    }

    @Override
    public void setState(boolean playing, boolean looping) {
        audioPlayer.setState(playing, looping);
        hwListener.setState(playing, looping);
        notifications.setState(playing, looping);
    }

    /**
     * forward to startup logic for newer androids
     */
    @SuppressLint("InlinedApi")
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent.getAction() == null) {
//            var isPLaying = audioPlayer.isPlaying();
//            var isLooping = audioPlayer.isLooping();
            switch (intent.getByteExtra(Launcher.TYPE, Launcher.NULL)) {
                /* start or pause audio playback */
//                case Launcher.PLAY_PAUSE -> setState(!isPLaying, isLooping);
//                case Launcher.PLAY -> setState(true, false);
//                case Launcher.PAUSE -> setState(false, false);
//                case Launcher.LOOP -> setState(isPLaying, !isLooping);
//                case Launcher.SKIP -> playNextEntry();
                /* cancel audio playback and kill service */
                case Launcher.KILL -> {
                    if (audioPlayer != null) {
                        if (audioPlayer.isPlaying()) {
                            audioPlayer.interrupt();
                        }
                    }
                    stopSelf();
                    stopForeground(android.app.Service.STOP_FOREGROUND_REMOVE);
                }
            }
        }
        playEntryFromPlaylist();
        return START_STICKY;
    }

    @Override
    public void onMediaPlayerReset() {
        notifications.onMediaPlayerReset();
        hwListener.onMediaPlayerReset();
        if (audioPlayer != null)
            audioPlayer.onMediaPlayerReset();
    }

    @Override
    public void onMediaPlayerDestroy() {
        // calls onDestroy()
        stopSelf();
        if (Build.VERSION.SDK_INT >= 24) {
            stopForeground(android.app.Service.STOP_FOREGROUND_REMOVE);
        }
    }

    /**
     * destroy on playback complete
     */
    void onMediaPlayerComplete() {
//        if (!playNextEntry())
        onMediaPlayerDestroy();
    }

    /**
     * service killing logic
     */
    @Override
    public void onDestroy() {
        onMediaPlayerReset();
        notifications.onMediaPlayerDestroy();
        hwListener.onMediaPlayerDestroy();
        if (audioPlayer != null) {
            if (audioPlayer.isPlaying()) {
                audioPlayer.interrupt();
            }
            audioPlayer.onMediaPlayerDestroy();
        }
//        playlist.clear();
        super.onDestroy();
    }
}
