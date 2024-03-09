package com.rhythmcoder.baselib;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rhythmcoder.baselib.utils.Constants;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<TitleBean> mDataList;

    public ListAdapter(List<TitleBean> dataList) {
        this.mDataList = dataList;
    }

    public void setDataList(List<TitleBean> dataList) {
        mDataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TitleBean data = mDataList.get(position);
        holder.tvTitle.setText(data.getTitle());
        holder.tvSubTitle.setText(data.getSubTitle());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvSubTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvSubTitle = itemView.findViewById(R.id.subTitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            TitleBean data = mDataList.get(position);
            Context context = v.getContext();
            Intent intent = new Intent(context, data.getActivityClass());
            intent.putExtra(Constants.INTENT_TITLE, data.getTitle());
            intent.putExtra(Constants.INTENT_INFO, data.getInfo());
            context.startActivity(intent);
        }
    }
}
