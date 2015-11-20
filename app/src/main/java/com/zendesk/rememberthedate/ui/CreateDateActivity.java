package com.zendesk.rememberthedate.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.zendesk.rememberthedate.LocalNotification;
import com.zendesk.rememberthedate.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CreateDateActivity extends AppCompatActivity {

    String   key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_date);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        if (getIntent().getExtras() != null) {
            key = getIntent().getExtras().getString("key");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Map<String, String> map = loadMap("dates");

        DatePicker  datePicker  = (DatePicker)this.findViewById(R.id.datePicker);
        TimePicker  timePicker  = (TimePicker)this.findViewById(R.id.timePicker);
        EditText    title   = (EditText)this.findViewById(R.id.nameText);

        if (key != null) {
            long l = Long.parseLong(key);
            Time time = new Time();
            time.set(l);

            datePicker.updateDate(time.year, time.month, time.monthDay);

            timePicker.setCurrentHour(time.hour);
            timePicker.setCurrentMinute(time.minute);

            title.setText(map.get(key));
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {

            DatePicker  datePicker  = (DatePicker)this.findViewById(R.id.datePicker);
            TimePicker  timePicker  = (TimePicker)this.findViewById(R.id.timePicker);
            EditText    title   = (EditText)this.findViewById(R.id.nameText);

            if (title.getText().toString().matches(""))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set title
                alertDialogBuilder.setTitle("Error");

                // set dialog message
                alertDialogBuilder
                        .setMessage("You need to fill the title!")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            }

            Time time   = new Time();

            time.year       = datePicker.getYear();
            time.month      = datePicker.getMonth();
            time.monthDay   = datePicker.getDayOfMonth();
            time.hour       = timePicker.getCurrentHour();
            time.minute     = timePicker.getCurrentMinute();

            long    millis  = time.toMillis(false);
            String  millisStr   = String.valueOf(millis);

            Map<String, String> map = loadMap("dates");

            if (key == null)
            {
                key = millisStr;
            }
            else
            {
                map.remove(key);
                key = millisStr;
            }

            map.put(key, title.getText().toString());

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, LocalNotification.class);
            intent.putExtra("message", title.getText().toString());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) millis, intent, PendingIntent.FLAG_ONE_SHOT);

            alarmManager.set(AlarmManager.RTC_WAKEUP, millis,pendingIntent);

            saveMap(map, "dates");

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveMap(Map<String,String> inputMap, String key){
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("MyDates", Context.MODE_PRIVATE);
        if (pSharedPref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove(key).apply();
            editor.putString(key, jsonString);
            editor.apply();
        }
    }

    private Map<String,String> loadMap(String Key){
        Map<String,String> outputMap = new HashMap<>();
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("MyDates", Context.MODE_PRIVATE);
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString(Key, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = (String) jsonObject.get(key);
                    outputMap.put(key, value);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_create_date, container, false);

            return rootView;
        }
    }
}
