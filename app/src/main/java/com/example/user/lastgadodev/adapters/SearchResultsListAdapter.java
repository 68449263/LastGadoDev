package com.example.user.lastgadodev.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.user.lastgadodev.Models.GeoTrain;
import com.example.user.lastgadodev.R;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsListAdapter extends RecyclerView.Adapter<SearchResultsListAdapter.ViewHolder> {

    private List<GeoTrain> geoTrainsList = new ArrayList<>();

    private int mLastAnimatedItemPosition = -1;

    public interface OnItemClickListener{
        void onClick(View view, int pos);
    }

    private OnItemClickListener mTrainItemsOnClickListener;

    //TODO: this executes when the user clicks the list item instead of the tracktrain button
    public void setItemsOnClickListener(OnItemClickListener onClickListener){
       this.mTrainItemsOnClickListener = onClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTrain_id_tv;
        public final TextView mSpeed_tv;
        public final TextView Travel_State_tv;
        public final TextView Route_Info_tv;
        public final Button Btracking;


        public ViewHolder(View view) {
            super(view);
            mTrain_id_tv = view.findViewById(R.id.train_id);
            mSpeed_tv = view.findViewById(R.id.speed);
            Btracking = view.findViewById(R.id.starttracking);
            Travel_State_tv = view.findViewById(R.id.travel_state);
            Route_Info_tv = view.findViewById(R.id.route_info);

        }
    }

    public void swapData(List<GeoTrain> mNewDataSet) {
        geoTrainsList = mNewDataSet;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_results_list_item2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)  {

        final GeoTrain availableTrain = geoTrainsList.get(position);
        holder.mTrain_id_tv.setText(availableTrain.getTrain_id());
        holder.mSpeed_tv.setText(Double.toString(availableTrain.getCurrent_Speed())+" km/h");
        holder.Travel_State_tv.setText(availableTrain.getTravel_State());
        holder.Route_Info_tv.setText("To "+availableTrain.getDestination());

        holder.Btracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mTrainItemsOnClickListener.onClick(view,position);

            }
        });

        if(mLastAnimatedItemPosition < position){

            mLastAnimatedItemPosition = position;
        }

        if(mTrainItemsOnClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTrainItemsOnClickListener.onClick(v,position);

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return geoTrainsList.size();
    }

}
