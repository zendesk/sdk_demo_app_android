package com.zendesk.rememberthedate.model;

import java.util.Date;

public class DateModel {

    private final String title;
    private final long dateInMillis;

    public DateModel(String title, long dateInMillis ) {
        this.title = title;
        this.dateInMillis = dateInMillis;
    }

    public String getTitle() {
        return title;
    }

    public long getDateInMillis(){
        return dateInMillis;
    }

    public Date getDate() {
        return new Date(dateInMillis);
    }
}
