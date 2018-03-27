package com.zendesk.rememberthedate.model;

import android.text.format.Time;

public class DateModel {

    private String title;
    private Time time;

    public DateModel(String title, Time time){
        this.title = title;
        this.time = time;
    }

    public String getTitle(){
        return title;
    }

    public Time getTime(){
        return time;
    }
}
