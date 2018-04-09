package com.zendesk.rememberthedate.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TimeModel implements Parcelable {

    public static final Creator<TimeModel> CREATOR = new Creator<TimeModel>() {
        @Override
        public TimeModel createFromParcel(Parcel in) {
            return new TimeModel(in);
        }

        @Override
        public TimeModel[] newArray(int size) {
            return new TimeModel[size];
        }
    };

    private final int hour;
    private final int minute;

    public TimeModel(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    private TimeModel(Parcel in) {
        hour = in.readInt();
        minute = in.readInt();
    }

    public int getMinute() {
        return minute;
    }

    public int getHour() {
        return hour;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hour);
        dest.writeInt(minute);
    }
}
