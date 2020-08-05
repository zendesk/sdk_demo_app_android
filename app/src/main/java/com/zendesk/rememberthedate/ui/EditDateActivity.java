package com.zendesk.rememberthedate.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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

public class EditDateActivity extends AppCompatActivity {

    private EditText title;
    private TextView dateView, timeView;
    private AppStorage storage;
    private Calendar currentlySelectedDate;
    private Date currentlySelectedTime = null;
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

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {
            key = getIntent().getExtras().getString("key");
        }

        storage = Global.getStorage(getApplicationContext());

        DateModel selectedDate = storage.loadMapData().get(key);
        if (selectedDate != null) {
            Date date = selectedDate.getDate();
            currentlySelectedDate = new GregorianCalendar();
            currentlySelectedDate.setTime(date);
            currentlySelectedTime = date;


            String dateString = Constants.HUMAN_READABLE_DATE.format(date);
            String timeString = Constants.HUMAN_READABLE_TIME.format(date);

            title.setText(selectedDate.getTitle());
            dateView.setText(dateString);
            timeView.setText(timeString);
        }
    }

    private void bindViews() {
        title = findViewById(R.id.add_title);

        dateView = findViewById(R.id.add_date);
        dateView.setOnClickListener(v -> SetDateActivity.startForResult(this, SetDateActivity.REQUEST_CODE));

        timeView = findViewById(R.id.add_time);
        timeView.setOnClickListener(v -> SetTimeActivity.startForResult(this, SetTimeActivity.REQUEST_CODE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_date, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        HashMap<String, DateModel> dateMap = new HashMap<>(storage.loadMapData());

        switch (item.getItemId()) {
            case R.id.action_save:

                if (title.getText().toString().matches("")) {
                    Toast.makeText(this, "Enter a title to save", Toast.LENGTH_LONG).show();
                    return true;
                }
                if (currentlySelectedTime == null || currentlySelectedDate == null) {
                    Toast.makeText(this,
                            "Enter date and time field to save",
                            Toast.LENGTH_LONG)
                            .show();
                    return true;
                }

                Calendar newCalendar = new GregorianCalendar(currentlySelectedDate.get(Calendar.YEAR),
                        currentlySelectedDate.get(Calendar.MONTH),
                        currentlySelectedDate.get(Calendar.DAY_OF_MONTH),
                        currentlySelectedTime.getHours(),
                        currentlySelectedTime.getMinutes());

                long dateLong = newCalendar.getTimeInMillis();
                String calendarKey = Long.toString(dateLong);

                // Delete previous instance of item, if changed
                if (dateMap.containsKey(key)) {
                    dateMap.remove(key);
                }

                DateModel dateModel = new DateModel(title.getText().toString(), dateLong);
                dateMap.put(calendarKey, dateModel);

                storage.storeMapData(dateMap);
                finish();
                return true;

            case R.id.action_delete:
                if (dateMap.containsKey(key)) {
                    dateMap.remove(key);
                    storage.storeMapData(dateMap);
                    Toast.makeText(this, title.getText().toString() + " deleted.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this,
                            "Date not stored yet.",
                            Toast.LENGTH_LONG)
                            .show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SetDateActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                currentlySelectedDate = SetDateActivity.getCalendarFromResultIntent(data);
                dateView.setText(Constants.HUMAN_READABLE_DATE.format(currentlySelectedDate.getTime()));
            }
        }

        if (requestCode == SetTimeActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                currentlySelectedTime = SetTimeActivity.getTimeFromResultIntent(data).getTime();
                timeView.setText(Constants.HUMAN_READABLE_TIME.format(currentlySelectedTime));
            }
        }
    }
}
