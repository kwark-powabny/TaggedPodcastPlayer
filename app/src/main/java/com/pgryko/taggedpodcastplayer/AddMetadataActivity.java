package com.pgryko.taggedpodcastplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.time.Duration;

public class AddMetadataActivity extends AppCompatActivity {
    private AudioFileMetadata metadata;
    private String metadataFileString;
    private Button btSave;
    private EditText editTextDescription;
    AudioFileMetaReaderWriter rw;

    private final static String LOG_TAG = "AddMetadataActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_metadata);

        if (getIntent().getExtras() != null) {
            this.metadata = (AudioFileMetadata) getIntent().getSerializableExtra(MainActivity.AUDIO_FILE_METADATA);
            this.metadataFileString = (String) getIntent().getStringExtra(MainActivity.JSON_FILE_PATH);
            Log.d(this.LOG_TAG, "Incoming extras:\n\t* metadataFileString:"
                    + this.metadataFileString
                    + "\n\t* startTime: "
                    + metadata.startTime
            );
        }

        this.editTextDescription = findViewById(R.id.edit_text_description);

        btSave = findViewById(R.id.bt_save);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                metadata.setDescription(editTextDescription.getText().toString());
                rw.metadataList.add(metadata);
                rw.WriteToFile();

                // back to the previous activity
                finish();
            }
        });


        // read existing metadata
        this.rw = new AudioFileMetaReaderWriter(metadataFileString);
        this.rw.ReadFromFile();
        if (this.rw.metadataList == null || this.rw.metadataList.isEmpty()){
            Toast.makeText(this, "Nie ma jeszcze żadnych znaczników", Toast.LENGTH_LONG).show();
        }
    }




}
