package com.pgryko.taggedpodcastplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Handler timerHandler; // to refresh file progress
    private Runnable repetitiveRunnable; // to refresh file progress
    private static final String TAG = "MainAcitvity";
    public static final String AUDIO_FILE_METADATA = "AudioFileMetadata";
    public static final String JSON_FILE_PATH = "JsonFilePath";

    Messenger mService = null;
    boolean mBound = false;
    Messenger replyMessenger = new Messenger(new HandlerReplyMsg());
    Intent inputIntent;
    String metadataPathString;
    String audioPathString;

    final ArrayList<AudioFileMetadata> arrayList = new ArrayList<AudioFileMetadata>();
    ListView listView;
    AudioFileMetaReaderWriter readerWriter;
    ImageView btPlay;
    ImageView btFwd;
    ImageView btPause;
    SeekBar seekBar1;
    AudioFileInfo currentFileInfo;
    Button btAdd;
    TextView tvTimes;
    ImageView btAudioSpeedPlus;
    ImageView btAudioSpeedMinus;
    TextView tvAudioSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent that started this activity
        this.inputIntent = getIntent();
        if (inputIntent.getData()!= null) {
            this.audioPathString = inputIntent.getData().getPath(); //.getEncodedPath();
            this.metadataPathString = inputIntent.getData().getPath() + ".json"; //getEncodedPath()
        } else{
            Toast.makeText(MainActivity.this, getString(R.string.text01), Toast.LENGTH_LONG).show();
            finish(); // TODO: the server calls anyway??? The toast "Connected" is shown
//            Log.d(TAG, "Test paths used!!!!");
//            this.audioPathString = "/mnt/sdcard/DCIM/taggedPlayer/KLCW_Drogi_Rothschildow_do_bogactwa.mp3";
//            this.metadataPathString = "/mnt/sdcard/DCIM/taggedPlayer/KLCW_Drogi_Rothschildow_do_bogactwa.mp3.json";
        }


        // Bind to PlayerService
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);

        // ----- Duration/length of audio file
        this.tvTimes = findViewById(R.id.tv_times);

        // ----- seek bar
        seekBar1 = findViewById(R.id.seekBar1);
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (tvTimes != null) {
                    tvTimes.setText(convertTimeFormat(currentFileInfo.position) + "/" + convertTimeFormat(currentFileInfo.duration));
                }
                    if (fromUser){
                    if (mBound) {
                        Message msg = Message.obtain(null, PlayerService.SEEK_TO, progress, 0);
                        msg.replyTo = replyMessenger;
                        try {
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace(); // TODO - log
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO: show a label that follows the thumb
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO: hide a label that follows the thumb
            }

            private String convertTimeFormat(int duration){
                if (duration < 360000000){ // over 100 minutes
                    return  String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                }
                else {
                    return "--:--"; //too much
                }
            }

        });

        // button Add
        btAdd = findViewById(R.id.bt_add);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioFileMetadata metadata = new AudioFileMetadata("", Duration.ofMillis(seekBar1.getProgress()), Duration.ofMillis(seekBar1.getMax()));
                Intent intent = new Intent(MainActivity.this, AddMetadataActivity.class);
                intent.putExtra(AUDIO_FILE_METADATA, metadata);
                intent.putExtra(JSON_FILE_PATH, metadataPathString ); // TODO: can't be empty
                startActivity(intent);
            }
        });

        // speed value
        tvAudioSpeed = findViewById(R.id.tv_audio_speed);

        // button Speed Minus
        btAudioSpeedMinus = findViewById(R.id.bt_audio_speed_minus);
        btAudioSpeedMinus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int speedPercent = Integer.parseInt(tvAudioSpeed.getText().toString());
                if (mBound) {
                    if (speedPercent > 25){
                        speedPercent -= 25;
                        tvAudioSpeed.setText(String.valueOf(speedPercent));

                        Message msg = Message.obtain(null, PlayerService.SET_AUDIO_SPEED, speedPercent, 0);
                        msg.replyTo = replyMessenger;
                        try {
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        // button Speed Plus
        btAudioSpeedPlus = findViewById(R.id.bt_audio_speed_plus);
        btAudioSpeedPlus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int speedPercent = Integer.parseInt(tvAudioSpeed.getText().toString());
                if (mBound) {
                    if (speedPercent < 300) {
                        speedPercent += 25;
                        tvAudioSpeed.setText(String.valueOf(speedPercent));

                        Message msg = Message.obtain(null, PlayerService.SET_AUDIO_SPEED, speedPercent, 0);
                        msg.replyTo = replyMessenger;
                        try {
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });


        // ----- button play
        btPlay = findViewById(R.id.bt_play);
        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    btPlay.setVisibility(View.GONE);
                    btPause.setVisibility(View.VISIBLE);
                    AudioFileInfo fileInfo = new AudioFileInfo(seekBar1.getMax(), seekBar1.getProgress());
                    Message msg = Message.obtain(null, PlayerService.START_PLAYING, 0, 0, fileInfo);
                    msg.replyTo = replyMessenger;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        //e.printStackTrace();
                    }
                }

            }

        });

        // -------- button PAUSE
        btPause = findViewById(R.id.bt_pause);
        btPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    btPause.setVisibility(View.GONE);
                    btPlay.setVisibility(View.VISIBLE);
                    Message msg = Message.obtain(null, PlayerService.PAUSE_PLAYING, 0, 0);
                    msg.replyTo = replyMessenger;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // -------- button FWD 30
        btFwd = findViewById(R.id.bt_fwd);
        btFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    Message msg = Message.obtain(null, PlayerService.FORWARD_30, 0, 0);
                    msg.replyTo = replyMessenger;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        // -------- button REW 10
        btFwd = findViewById(R.id.bt_rew);
        btFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    Message msg = Message.obtain(null, PlayerService.BACKWARD_10, 0, 0);
                    msg.replyTo = replyMessenger;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // ------- initialize runnable to refresh seekbar status
        timerHandler = new Handler();
        repetitiveRunnable = new Runnable() {
            @Override
            public void run() {
                if (mBound) {
                    Message msg = Message.obtain(null, PlayerService.CHECK_PROGRESS, 0, 0);
                    msg.replyTo = replyMessenger;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace(); // TODO - log
                    }
                }
                timerHandler.postDelayed(repetitiveRunnable, 500);
            }
        };
        repetitiveRunnable.run();

        // >> TODO: move somewhere else

        // ---- read metadata from file
        readMetadataList();

        AudioFileMetadataAdapter adapter = new AudioFileMetadataAdapter(this, arrayList);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //AudioFileAddData afad = (AudioFileAddData) view.getTag();
                //AudioFileAddData afad = (AudioFileAddData) arrayList.get(position);
                AudioFileMetadata afad = (AudioFileMetadata) listView.getItemAtPosition(position);

                if (mBound) {
                    Message msg = Message.obtain(null, PlayerService.SEEK_TO, (int) Duration.parse(afad.startTime).toMillis(), 0);
                    msg.replyTo = replyMessenger;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace(); // TODO - log
                    }
                }
            }


        });
        // << TODO: move somewhere else
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        readMetadataList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void readMetadataList(){
        if (this.metadataPathString != null && this.metadataPathString != ""){
            Log.d(TAG, "Path to metadata file: " + this.metadataPathString);
            AudioFileMetaReaderWriter metaReaderWriter = new AudioFileMetaReaderWriter(this.metadataPathString);
            metaReaderWriter.ReadFromFile();
            arrayList.clear();
            if (metaReaderWriter.metadataList != null && !metaReaderWriter.metadataList.isEmpty()){
                arrayList.addAll(metaReaderWriter.metadataList);
            } else {
                Log.d(TAG, getString(R.string.text02));
            }
        }else {
            Log.e(TAG, getString(R.string.text03));
        }
    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();
            mService = new Messenger(service);
            mBound = true;

            // ----- set audio source
            // TODO - maybe pass source file directly in binding??
            try {
                 if (audioPathString != null && audioPathString != "") {
                    Message msg = Message.obtain(null, PlayerService.SET_AUDIO_SOURCE, 0, 0, audioPathString);
                    msg.replyTo = replyMessenger;
                    mService.send(msg);
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace(); // TODO - log
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mBound = false;
        }
    };


    // handler for message from service

    class HandlerReplyMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mBound){
                currentFileInfo = (AudioFileInfo) msg.obj;
                seekBar1.setMax(currentFileInfo.duration);
                seekBar1.setProgress(currentFileInfo.position);
//                if (tvTimes != null){
//                    tvTimes.setText(convertTimeFormat(currentFileInfo.position) + "/" + convertTimeFormat(currentFileInfo.duration));
//                }
            }
        }
    }




}
