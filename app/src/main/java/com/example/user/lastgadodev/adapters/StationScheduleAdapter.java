package com.example.user.lastgadodev.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.user.lastgadodev.R;

import java.util.ArrayList;
import java.util.List;

public class StationScheduleAdapter extends RecyclerView.Adapter<com.example.user.lastgadodev.adapters.StationScheduleAdapter.ViewHolder> {

    private List<String> ScheduleStationsList = new ArrayList<>();

    private int mLastAnimatedItemPosition = -1;

    private OnTouchListener touchListener;

    //when the switch is turned on, within schedule list item
    public interface OnTouchListener {
        void onAddStationNotification(View view, int pos);
    }

    public void setItemsOnClickListener(OnTouchListener touchListener) {
        this.touchListener = touchListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView stationName;
        public final Switch aSwitch;

        public ViewHolder(View view) {
            super(view);

            stationName = view.findViewById(R.id.station_name);
            aSwitch = view.findViewById(R.id.scheduleNotificationSW);
        }
    }

    public void populateScheduleList(List<String> mNewDataSet) {
        ScheduleStationsList = mNewDataSet;
        notifyDataSetChanged();//notifies the recycler view about any data changes
    }

    @Override
    public StationScheduleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.schedule_list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")//https://stackoverflow.com/questions/46135249/custom-view-imagebutton-has-setontouchlistener-called-on-it-but-does-not-overr
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final String stationName = ScheduleStationsList.get(position);

        holder.stationName.setText(stationName);

        if (mLastAnimatedItemPosition < position) {

            mLastAnimatedItemPosition = position;
        }

        if (touchListener != null) {
            holder.aSwitch.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    touchListener.onAddStationNotification(view, position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ScheduleStationsList.size();
    }
}
