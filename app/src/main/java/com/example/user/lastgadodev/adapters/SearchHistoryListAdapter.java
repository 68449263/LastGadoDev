package com.example.user.lastgadodev.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.user.lastgadodev.R;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryListAdapter extends RecyclerView.Adapter<com.example.user.lastgadodev.adapters.SearchHistoryListAdapter.ViewHolder> {

    private List<String> SearchHistoryTrainsList = new ArrayList<>();

    private int mLastAnimatedItemPosition = -1;

    public interface OnItemClickListener {
        void onClickedHistory(View view, int pos);
    }

    private OnItemClickListener HistoryItemsOnClickListener;

    public void setItemsOnClickListener(OnItemClickListener onClickListener) {
        this.HistoryItemsOnClickListener = onClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView HistoryRoute;

        public ViewHolder(View view) {
            super(view);

            HistoryRoute = view.findViewById(R.id.history_value_tv);
        }
    }

    public void populateSearchHistory(List<String> mNewDataSet) {
        SearchHistoryTrainsList = mNewDataSet;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,final int position) {

        final String historyTrainRoute = SearchHistoryTrainsList.get(position);

        holder.HistoryRoute.setText(historyTrainRoute);

        if (mLastAnimatedItemPosition < position) {

            mLastAnimatedItemPosition = position;
        }

        if (HistoryItemsOnClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HistoryItemsOnClickListener.onClickedHistory(v, position);

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return SearchHistoryTrainsList.size();
    }

}


