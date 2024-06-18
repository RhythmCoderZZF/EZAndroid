package com.rhythmcoder.androidstudysystem.wifi.p2p;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rhythmcoder.androidstudysystem.R;

import java.util.List;

public class P2pDeviceAdapter extends RecyclerView.Adapter<P2pDeviceAdapter.MyViewHolder> {

    private List<WifiP2pDevice> mDataList;
    private OnItemClickListener onItemClickListener;

    public P2pDeviceAdapter(List<WifiP2pDevice> dataList, OnItemClickListener listener) {
        this.mDataList = dataList;
        this.onItemClickListener = listener;
    }

    public void setDataList(List<WifiP2pDevice> dataList) {
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
        WifiP2pDevice data = mDataList.get(position);
        holder.tvTitle.setText(data.deviceName);
        holder.tvSubTitle.setText(data.deviceAddress);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvSubTitle;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubTitle = itemView.findViewById(R.id.tv_sub_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            WifiP2pDevice data = mDataList.get(position);
            v.setOnClickListener(v1 -> {
                onItemClickListener.onClick(data);
            });
        }
    }

    interface OnItemClickListener {
        void onClick(WifiP2pDevice device);
    }
}
