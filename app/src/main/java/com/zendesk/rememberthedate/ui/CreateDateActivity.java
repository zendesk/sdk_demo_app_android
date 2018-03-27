package com.zendesk.rememberthedate.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.zendesk.rememberthedate.Global;
import com.zendesk.rememberthedate.LocalNotification;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.DateModel;
import com.zendesk.rememberthedate.storage.AppStorage;

import java.util.HashMap;
import java.util.Map;


public class CreateDateActivity extends AppCompatActivity {

    static void start(Context context) {
        context.startActivity(new Intent(context, CreateDateActivity.class));
    }

    static void start(Context context, String id) {
        final Intent intent = new Intent(context, CreateDateActivity.class);
        intent.putExtra("key", id);
        context.startActivity(intent);
    }

    private String key;
    private AppStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_date);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        DatePicker datePicker = this.findViewById(R.id.datePicker);
        TimePicker timePicker = this.findViewById(R.id.timePicker);
        EditText title = this.findViewById(R.id.nameText);

        if (key != null) {
            long l = Long.parseLong(key);
            Time time = new Time();
            time.set(l);

            datePicker.updateDate(time.year, time.month, time.monthDay);

            timePicker.setCurrentHour(time.hour);
            timePicker.setCurrentMinute(time.minute);

            title.setText(map.get(key).getTitle());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_date, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Log.i(Global.LOG_TAG, "Pressed home");
            onBackPressed();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {

            DatePicker datePicker = this.findViewById(R.id.datePicker);
            TimePicker timePicker = this.findViewById(R.id.timePicker);
            EditText title = this.findViewById(R.id.nameText);

            if (title.getText().toString().matches("")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set dialog message
                alertDialogBuilder
                        .setTitle("Error")
                        .setMessage("You need to fill the title!")
                        .setCancelable(false)
                        .setPositiveButton("OK", null);

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            }


            Time time = new Time();
            time.year = datePicker.getYear();
            time.month = datePicker.getMonth();
            time.monthDay = datePicker.getDayOfMonth();
            time.hour = timePicker.getCurrentHour();
            time.minute = timePicker.getCurrentMinute();

            long millis = time.toMillis(false);
            String millisStr = String.valueOf(millis);

            Map<String, DateModel> dateMap = new HashMap<>(storage.loadMapData());

            if (key == null) {
                key = millisStr;    // Sets key to be a the time represented as a long in milliseconds
            } else {
                dateMap.remove(key);
                key = millisStr;
            }
            Log.i(Global.LOG_TAG, "onOptionsItemSelected: "+key+ title.getText().toString());

            DateModel date = new DateModel(title.getText().toString(), time);
            dateMap.put(key, date);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, LocalNotification.class);
            intent.putExtra("message", title.getText().toString());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) millis, intent, PendingIntent.FLAG_ONE_SHOT);

            alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);

            storage.storeMapData(dateMap);

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
