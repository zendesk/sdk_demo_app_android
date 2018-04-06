package com.zendesk.rememberthedate.model;

import android.text.format.Time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateModel {

    private String id, title;
    private Time time;
    private Calendar calendar;

    public DateModel(String title, Time time){
        this.id = Long.toString(time.toMillis(true));
        this.title = title;
        this.time = time;
        calendar = new GregorianCalendar(time.year, time.month, time.monthDay, time.hour, time.monthDay);
    }

    public String getTitle(){
        return title;
    }

    public Time getTime(){
        return time;
    }

    public String getId() {
        return id;
   }

    public Date getDate() {
        return calendar.getTime();
    }
}
