package com.pgryko.taggedpodcastplayer;

import java.io.Serializable;
import java.time.Duration;

class AudioFileMetadata implements Serializable {
    //private String[] tags;
    public String description;
    public String startTime;
    public String endTime;

//    public AudioFileAddData(String[] tags, String description, Time startTime, Time endTime){
//        this.tags = tags;
//        this.description = description;
//        this.startTime = startTime;
//        this.endTime = endTime;
//    }


    public AudioFileMetadata(String description, Duration startTime, Duration endTime){
        this.description = description;
        this.startTime = startTime.toString(); // it hast to be a string in order to save in json file.
        this.endTime = endTime.toString(); // it hast to be a string in order to save in json file.
    }

    public AudioFileMetadata(String description, String startTime, String endTime){
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }


    public void setStartTime(Duration startTime){
        this.startTime = startTime.toString();
    }

    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public String getStartTime(){
        return this.startTime;
    }

//    public void setEndTime(Time endTime){
//        this.endTime = endTime;
//    }
//
//    public Time getEndTimeTime(){
//        return this.endTime;
//    }
//
//    public void setTags(String [] tags){
//        this.tags= tags;
//    }
//
//    public String[] getTags(){
//        return this.tags;
//    }

//    public void addTag(String tag){
//        this.description = description;
//    }

}
