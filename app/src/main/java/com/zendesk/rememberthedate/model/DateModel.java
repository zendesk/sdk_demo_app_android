package com.zendesk.rememberthedate.model;

import android.text.format.Time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateModel {

    private final long id;
    private final String title;
    private final Date date;

    public DateModel(long id, String title, Date date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }
}
