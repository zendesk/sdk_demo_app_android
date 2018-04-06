package com.zendesk.rememberthedate.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;

import com.zendesk.rememberthedate.R;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SetDateActivity extends AppCompatActivity {

    private static final String CALENDAR_RESULT_KEY = "CALENDAR_RESULT_KEY";
    Button ok, cancel;
    DatePicker datePicker;
    Calendar calendar;

    static Calendar getCalendarFromResultIntent(Intent intent){
        return (Calendar) intent.getSerializableExtra(CALENDAR_RESULT_KEY);
    }

    static void startForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, SetDateActivity.class), requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_date);
        bindViews();

        ok.setOnClickListener(v -> {
            calendar = new GregorianCalendar(datePicker.getYear(),
                    datePicker.getMonth(),
                    datePicker.getDayOfMonth());

            Intent intent = new Intent();
            intent.putExtra(CALENDAR_RESULT_KEY, calendar);
            setResult(RESULT_OK, intent);
            finish();
        });
        cancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void bindViews() {
        ok = findViewById(R.id.ok_button);
        cancel = findViewById(R.id.cancel_button);
        datePicker = findViewById(R.id.date_picker);
    }


}
