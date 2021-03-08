package com.pgryko.taggedpodcastplayer;

import android.app.Service;
import android.content.Intent;
//import android.os.Binder;
//import android.os.Bundle;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;
//import android.util.Log;
//import android.widget.Toast;


public class PlayerService extends Service {

    MediaPlayer mediaPlayer1;
    Messenger replyMessanger;

    static final int START_PLAYING = 0;
    static final int PAUSE_PLAYING = 1;
    static final int FORWARD_30 = 2;
    static final int BACKWARD_10 = 3;
    static final int CHECK_PROGRESS = 4;
    static final int SEEK_TO = 5;
    static final int SET_AUDIO_SOURCE = 6;
    static final int SET_AUDIO_SPEED = 7;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        //Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_LONG).show();
        // initialize media player
        mediaPlayer1 = new MediaPlayer();
        mediaPlayer1.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        return mMessenger.getBinder();
    }


    /**
     * Handler of incoming messages from main activity.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_AUDIO_SOURCE:
                    try {
                        //String fileName2 = "/mnt/sdcard/DCIM/taggedPlayer/KLCW_Drogi Rothschildow_do_bogactwa.mp3"; // TODO - ścieżka jest na sztywno
                       //Uri inputData  = (Uri) msg.obj;
                        //mediaPlayer1.setDataSource(inputData.getEncodedPath());
                        mediaPlayer1.setDataSource((String) msg.obj);
                        //mediaPlayer1.setDataSource("/mnt/sdcard/DCIM/taggedPlayer/KLCW_Drogi_Rothschildow_do_bogactwa.mp3");
                        mediaPlayer1.prepare();
                    }catch (Exception e){
                        // TODO log
                        Toast.makeText(PlayerService.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                    retrunFileInfo(msg);
                    break;
                case START_PLAYING:
                        mediaPlayer1.start();
                        retrunFileInfo(msg);
                    break;

                case PAUSE_PLAYING:
                    mediaPlayer1.pause();
                    retrunFileInfo(msg);
                    break;

                case FORWARD_30:
                    if (mediaPlayer1.getCurrentPosition() + 30000 < mediaPlayer1.getDuration())
                        mediaPlayer1.seekTo(mediaPlayer1.getCurrentPosition() + 30000);
                    else
                        mediaPlayer1.seekTo(mediaPlayer1.getDuration());
                    retrunFileInfo(msg);
                    break;

                case BACKWARD_10:
                    if (mediaPlayer1.getCurrentPosition() - 10000 > 0)
                        mediaPlayer1.seekTo(mediaPlayer1.getCurrentPosition() - 10000);
                    else
                        mediaPlayer1.seekTo(0);
                    retrunFileInfo(msg);
                    break;

                case SEEK_TO:
                    int position = msg.arg1;
                    if (position >= 0 && position < mediaPlayer1.getDuration())
                        mediaPlayer1.seekTo(position);
                    retrunFileInfo(msg);
                    break;

                case SET_AUDIO_SPEED:
                    setAudioSpeed(msg.arg1);
                    retrunFileInfo(msg);
                    break;

                case CHECK_PROGRESS:
                     if (mediaPlayer1 != null && replyMessanger != null) {
                        retrunFileInfo(msg);
                     }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }

        private void retrunFileInfo(Message msg){
            replyMessanger = msg.replyTo;
            try {
                Message message = new Message();
                AudioFileInfo fileInfo = new AudioFileInfo(
                        mediaPlayer1.getDuration(),
                        mediaPlayer1.getCurrentPosition());
                message.obj = fileInfo;
                replyMessanger.send(message);// sending msg back
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private void setAudioSpeed(int speedPercent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // TODO: zdeaktywować przyciski do zmiany prędkości gdy niższa wersja?
                try {
                    boolean isPlaying = mediaPlayer1.isPlaying();
                    PlaybackParams params = mediaPlayer1.getPlaybackParams();
                    params.setSpeed((float) speedPercent / 100); // percentage
                    mediaPlayer1.setPlaybackParams(params);
                    if(!isPlaying){
                        mediaPlayer1.pause(); // because setPlaybackParams gets player played but we don't want it to do so
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(PlayerService.this, "Zmiana prędkości nie jest obsługiwana na tym urządzeniu", Toast.LENGTH_LONG).show(); // TODO: teksty do XML-a
            }
        }


    }




}