package com.zendesk.rememberthedate.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zendesk.rememberthedate.Constants;
import com.zendesk.rememberthedate.Global;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.DateModel;
import com.zendesk.rememberthedate.storage.AppStorage;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditDateActivity extends AppCompatActivity {

    private static final int DATE_REQUEST_CODE = 21, TIME_REQUEST_CODE = 22;
    private EditText title;
    private TextView dateView, timeView;
    private AppStorage storage;
    private Calendar calendar;
    private Date dateTime = null;
    private String key = null;

    public static void start(Context context) {
        context.startActivity(new Intent(context, EditDateActivity.class));
    }

    static void start(Context context, String id) {
        final Intent intent = new Intent(context, EditDateActivity.class);
        intent.putExtra("key", id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_date);

        bindViews();
        setupTextViews();

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {
            key = getIntent().getExtras().getString("key");
        }

        storage = Global.getStorage(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Map<String, DateModel> map = new HashMap<>(storage.loadMapData());
        if (key != null) {
            Date itemDate = new Date(map.get(key).getTime().toMillis(false));

            String dateString = Constants.HUMAN_READABLE_DATE.format(itemDate);
            String timeString = Constants.HUMAN_READABLE_TIME.format(itemDate);

            title.setText(map.get(key).getTitle());
            dateView.setText(dateString);
            timeView.setText(timeString);

            calendar = new GregorianCalendar();
            calendar.set(itemDate.getYear(),
                    itemDate.getMonth(),
                    itemDate.getDay(),
                    itemDate.getHours(),
                    itemDate.getMinutes());
        }
    }

    private void bindViews() {
        title = findViewById(R.id.add_title);
        dateView = findViewById(R.id.add_date);
        timeView = findViewById(R.id.add_time);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_date, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Map<String, DateModel> dateMap = storage.loadMapData();
        switch (item.getItemId()) {
            case R.id.action_save:
                if (title.getText().toString().matches("")) {
                    Toast.makeText(this, "Enter a title to save", Toast.LENGTH_LONG).show();
                    return true;
                }
                if (dateView.getText().toString().matches("") ||
                        timeView.getText().toString().matches("")) {
                    Toast.makeText(this,
                            "Enter date and time field to save",
                            Toast.LENGTH_LONG)
                            .show();
                    return true;
                }

                Date date = getDate();
                Time time = new Time();
                time.set(date.getTime());
//                String dateKey = Long.toString(dateView.getTime());
                String calendarKey = Long.toString(time.toMillis(false));

                if (dateMap.containsKey(key)) {
                    dateMap.remove(key);
                }
                key = calendarKey;
                DateModel dateModel = new DateModel(title.getText().toString(), time);
                dateMap.put(key, dateModel);

                storage.storeMapData(dateMap);
                finish();
                return true;

            case R.id.action_delete:
                if (dateMap.containsKey(key)) {
                    dateMap.remove(key);
                }
                storage.storeMapData(dateMap);
                Toast.makeText(this, title.getText().toString() + " deleted.", Toast.LENGTH_LONG).show();
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DATE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                calendar = SetDateActivity.getCalendarFromResultIntent(data);
                Log.i("SetDateAcivityCal", calendar.toString());
                dateView.setText(Constants.HUMAN_READABLE_DATE.format(calendar.getTime()));
            }
        }

        if (requestCode == TIME_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //// FIXME
                dateTime = SetTimeActivity.getTimeFromResultIntent(data).getTime();
                Log.i("SetTimeActivity", dateTime.toString());
                timeView.setText(Constants.HUMAN_READABLE_TIME.format(dateTime));
            }
        }
    }

    private void setupTextViews() {
        dateView.setOnClickListener(v -> SetDateActivity.startForResult(this, DATE_REQUEST_CODE));
        timeView.setOnClickListener(v -> SetTimeActivity.startForResult(this, TIME_REQUEST_CODE));
    }

    private Date getDate() {
        if (calendar == null && dateTime == null) {
            return null;
        }
        calendar = new GregorianCalendar(Locale.getDefault());
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                dateTime.getHours(),
                dateTime.getMinutes());
        return calendar.getTime();
    }

}
