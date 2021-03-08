package com.pgryko.taggedpodcastplayer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;


public class AudioFileMetaReaderWriter {

    private static final String TAG = "AudioFileMetaReaderWri";

    public ArrayList<AudioFileMetadata> metadataList;
    private Path path;

    public AudioFileMetaReaderWriter(String fileName){
        this.path = Paths.get(fileName);
        this.metadataList = new ArrayList<AudioFileMetadata>();

    }


    public void ReadFromFile(){
        try(Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            this.metadataList = gson.fromJson(reader, new TypeToken<List<AudioFileMetadata>>(){}.getType());
            if (this.metadataList == null){
                this.metadataList = new ArrayList<AudioFileMetadata>();
            }
            Log.d(TAG, "Odczytano: " + this.metadataList.size() + " rekordów z pliku " + this.path.toString());
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }

    public void WriteToFile(){

        try {

            try (Writer writer = Files.newBufferedWriter(this.path, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                gson.toJson(this.metadataList, writer);
                Log.d(TAG, "Zapisano: " + this.metadataList.size() + " rekordów do pliku " + this.path.toString());
            }
        }
        catch (IOException e){
            Log.e(TAG, e.toString());
        }
    }




}