package com.rhythmcoder.androidstudysystem.wifi.p2p.wifidirect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rhythmcoder.androidstudysystem.R;

import java.util.List;

public class P2pDeviceAdapter extends RecyclerView.Adapter<P2pDeviceAdapter.MyViewHolder> {

    private List<String> mDataList;
    private OnItemClickListener onItemClickListener;

    public P2pDeviceAdapter(List<String> dataList, OnItemClickListener listener) {
        this.mDataList = dataList;
        this.onItemClickListener = listener;
    }

    public void setDataList(List<String> dataList) {
        this.mDataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_wifi_p2p_device, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String deviceName = mDataList.get(position);
        holder.tvTitle.setText(deviceName);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            String data = mDataList.get(position);
            onItemClickListener.onClick(data);
        }
    }

    public interface OnItemClickListener {
        void onClick(String device);
    }
}
