package com.zendesk.rememberthedate.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zendesk.rememberthedate.Global;
import com.zendesk.rememberthedate.LocalNotification;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.DateModel;
import com.zendesk.rememberthedate.storage.AppStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 */
public class DateFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Date";

    public static DateFragment newInstance() {
        return new DateFragment();
    }

    private AppStorage storage;
    private RecyclerView recyclerView;
    private DateAdapter dateAdapter;
    private View emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_date, container, false);
        recyclerView = view.findViewById(R.id.date_list);
        emptyView = view.findViewById(R.id.empty_view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        storage = Global.getStorage(getActivity());

        dateAdapter = new DateAdapter(new OnDateClickListener() {
            @Override
            public void onClick(Item item) {
                CreateDateActivity.start(getActivity(), item.id);
            }

            @Override
            public void onLongClick(Item item) {
                showRemoveDialog(item);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(dateAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadAdapter();
    }

    private void reloadAdapter() {
        final Map<String, DateModel> mapData = storage.loadMapData();
        final List<Item> data = new ArrayList<>();

        for (Map.Entry<String, DateModel> entry : mapData.entrySet()) {
            Item item = new Item();
            item.title = entry.getValue().getTitle();
            item.id = entry.getKey();
            data.add(item);
        }

        if (data.size() > 0) {
            emptyView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            dateAdapter.update(data);

        } else {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    private void showRemoveDialog(final Item item) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder
                .setTitle("Confirm")
                .setMessage("Remove this date?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) -> {
                    //data.remove(arg2);

                    long millis = Long.parseLong(item.id);

                    AlarmManager alarmManager = (AlarmManager) DateFragment.this.getActivity().getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(DateFragment.this.getActivity(), LocalNotification.class);
                    intent.putExtra("message", item.title);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(DateFragment.this.getActivity(), (int) millis, intent, PendingIntent.FLAG_ONE_SHOT);

                    Map<String, DateModel> mapData = storage.loadMapData();
                    mapData.remove(item.id);
                    storage.storeMapData(mapData);

                    alarmManager.cancel(pendingIntent);

                    reloadAdapter();

                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        // create alert dialog
        alertDialogBuilder.create().show();
    }

    private static class DateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final OnDateClickListener clickListener;
        private List<Item> items;

        private DateAdapter(OnDateClickListener clickListener) {
            this.clickListener = clickListener;
            this.items = new ArrayList<>();
        }

        void update(List<Item> items) {
            this.items = new ArrayList<>(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater li = LayoutInflater.from(parent.getContext());
            final View view = li.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final TextView text = holder.itemView.findViewById(android.R.id.text1);
            final Item item = items.get(position);
            text.setText(item.title);

            holder.itemView.setOnClickListener(v -> clickListener.onClick(item));
            holder.itemView.setOnLongClickListener(v -> {
                clickListener.onLongClick(item);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

    private static class Item {
        String title;
        String id;
    }

    interface OnDateClickListener {
        void onClick(Item item);
        void onLongClick(Item item);
    }

}
