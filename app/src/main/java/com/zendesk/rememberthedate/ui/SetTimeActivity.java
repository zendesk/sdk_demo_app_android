package com.zendesk.rememberthedate.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TimePicker;

import com.zendesk.rememberthedate.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class SetTimeActivity extends AppCompatActivity {

    private static final String TIME_REQUEST_CODE = "TIME_REQUEST_CODE";
    public static final int REQUEST_CODE = 22;

    private Button ok;
    private Button cancel;
    private TimePicker timePicker;

    private Calendar calendar;

    static Calendar getTimeFromResultIntent(Intent intent) {
        return (Calendar) intent.getSerializableExtra(TIME_REQUEST_CODE);
    }

    static void startForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, SetTimeActivity.class), requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);
        bindViews();

        ok.setOnClickListener(v -> {
            calendar = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                calendar.set(0, 0, 0, timePicker.getHour(), timePicker.getMinute());
            } else {
                calendar.set(0, 0, 0, timePicker.getCurrentHour(), timePicker.getCurrentMinute());
            }

            Intent intent = new Intent();
            intent.putExtra(TIME_REQUEST_CODE, calendar);
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
        timePicker = findViewById(R.id.time_picker);
    }
}
