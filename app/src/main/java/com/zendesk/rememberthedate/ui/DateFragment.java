package com.zendesk.rememberthedate.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zendesk.rememberthedate.LocalNotification;
import com.zendesk.rememberthedate.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 *
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class DateFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;
    ArrayList<Item> data;

    public static DateFragment newInstance() {
        DateFragment fragment = new DateFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reloadAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();

        reloadAdapter();
    }

    public void reloadAdapter()
    {
        Map<String, String> mapData    = loadMap("dates");

        data   = new ArrayList<Item>();

        Iterator it = mapData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Item item   = new Item();
            item.title  = pairs.getValue().toString();
            item.id     = pairs.getKey().toString();
            data.add(item);
        }

        SimpleAdapter   adapter = new SimpleAdapter();

        setListAdapter(adapter);
    }

    private void saveMap(Map<String,String> inputMap, String key){
        SharedPreferences pSharedPref = getActivity().getApplicationContext().getSharedPreferences("MyDates", Context.MODE_PRIVATE);
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
        Map<String,String> outputMap = new HashMap<String,String>();
        SharedPreferences pSharedPref = getActivity().getApplicationContext().getSharedPreferences("MyDates", Context.MODE_PRIVATE);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DateFragment.this.getActivity());

                // set title
                alertDialogBuilder.setTitle("Confirm");

                // set dialog message
                final AlertDialog.Builder ok = alertDialogBuilder;
                ok.setMessage("Remove this date?");
                ok.setCancelable(true);
                ok.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Item item = data.get(arg2);
                        //data.remove(arg2);

                        long millis = Long.parseLong(item.id);

                        AlarmManager alarmManager = (AlarmManager)DateFragment.this.getActivity().getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(DateFragment.this.getActivity(), LocalNotification.class);
                        intent.putExtra("message", item.title);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(DateFragment.this.getActivity(), (int) millis, intent, PendingIntent.FLAG_ONE_SHOT);

                        alarmManager.cancel(pendingIntent);

                        Map<String, String> mapData = loadMap("dates");
                        mapData.remove(item.id);

                        saveMap(mapData, "dates");

                        reloadAdapter();

                    }
                });
                ok.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // nothing
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date, container, false);
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Item objectItem = data.get(position);

            mListener.onFragmentInteraction(objectItem.id);
        }
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }

    private class SimpleAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                // inflate the layout
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            // object item based on the position
            Item objectItem = data.get(position);
            TextView    text    = (TextView)convertView.findViewById(android.R.id.text1);
            text.setText(objectItem.title);

            return convertView;
        }
    }

    private class Item
    {
        String title;
        String id;
    }

}
