package com.zendesk.rememberthedate.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zendesk.rememberthedate.Constants;
import com.zendesk.rememberthedate.Global;
import com.zendesk.rememberthedate.LocalNotification;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.DateModel;
import com.zendesk.rememberthedate.storage.AppStorage;

import java.util.ArrayList;
import java.util.Date;
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
    private ImageView imageView;
    private TextView textView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_date, container, false);
        recyclerView = view.findViewById(R.id.date_list);
        imageView = view.findViewById(R.id.image_view);
        textView = view.findViewById(R.id.text_view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        storage = Global.getStorage(getActivity());

        dateAdapter = new DateAdapter(new OnDateClickListener() {
            @Override
            public void onClick(DateModel item) {
                EditDateActivity.start(getActivity(), Long.toString(item.getDateInMillis()));
            }

            @Override
            public void onLongClick(DateModel item) {
                showRemoveDialog(item);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), 0));
        recyclerView.setAdapter(dateAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadAdapter();
    }

    private void reloadAdapter() {
        final Map<String, DateModel> mapData = storage.loadMapData();
        final List<DateModel> data = new ArrayList<>();

        for (Map.Entry<String, DateModel> entry : mapData.entrySet()) {
            data.add(entry.getValue());
        }

        if (data.size() > 0) {
            imageView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            dateAdapter.update(data);

        } else {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    private void showRemoveDialog(final DateModel item) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder
                .setTitle("Confirm")
                .setMessage("Remove this date?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) -> {
                    //data.remove(arg2);

                    long millis = item.getDateInMillis();

                    AlarmManager alarmManager = (AlarmManager) DateFragment.this.getActivity().getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(DateFragment.this.getActivity(), LocalNotification.class);
                    intent.putExtra("message", item.getTitle());

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(DateFragment.this.getActivity(), (int) millis, intent, PendingIntent.FLAG_ONE_SHOT);

                    Map<String, DateModel> mapData = storage.loadMapData();
                    mapData.remove(Long.toString(item.getDateInMillis()));
                    storage.storeMapData(mapData);

                    alarmManager.cancel(pendingIntent);

                    reloadAdapter();

                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        // create alert dialog
        alertDialogBuilder.create().show();
    }

    private static class DateAdapter extends RecyclerView.Adapter<DateAdapter.MyViewHolder> {

        private final OnDateClickListener clickListener;
        private List<DateModel> items;

        private DateAdapter(OnDateClickListener clickListener) {
            this.clickListener = clickListener;
            this.items = new ArrayList<>();
        }

        void update(List<DateModel> items) {
            this.items = new ArrayList<>(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
            final LayoutInflater li = LayoutInflater.from(parent.getContext());
            final View view = li.inflate(R.layout.date_cell, parent, false);
            return new MyViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            Log.i("DateAdapter", items.get(position).getTitle());
            DateModel item = items.get(position);

            holder.bindData(item);

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

        public static class MyViewHolder extends ViewHolder {
            final TextView titleView;
            final TextView dateView;

            MyViewHolder(View view) {
                super(view);
                this.titleView = view.findViewById(R.id.title_view);
                this.dateView = view.findViewById(R.id.date_view);
            }

            private void bindData(DateModel dateModel) {
                titleView.setText(dateModel.getTitle());
                Date date = dateModel.getDate();
                dateView.setText(Constants.HUMAN_READABLE_DATETIME.format(date));
            }
        }

    }

    interface OnDateClickListener {
        void onClick(DateModel item);

        void onLongClick(DateModel item);
    }

}
