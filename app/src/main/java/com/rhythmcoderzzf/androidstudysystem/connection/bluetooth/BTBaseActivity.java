package com.rhythmcoderzzf.androidstudysystem.connection.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityBluetoothBaseBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.connection.EZBluetooth;
import com.rhythmcoderzzf.ezandroid.permission.EZPermission;
import com.rhythmcoderzzf.ezandroid.utils.EZLogUtil;
import com.rhythmcoderzzf.ezandroid.view.EZRecyclerView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingPermission")
public class BTBaseActivity extends BaseActivity<ActivityBluetoothBaseBinding> {
    private EZBluetooth ezBluetooth;
    private EZRecyclerView mUnBondRecyclerView;
    private EZRecyclerView mBondRecyclerView;
    private List<BluetoothDevice> mUnBondedDevices = new ArrayList<>();
    private List<BluetoothDevice> mBondedDevices = new ArrayList<>();

    //当前配对上蓝牙设备的mac地址
    private BluetoothDevice selectBondDevice;

    @Override
    protected ActivityBluetoothBaseBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityBluetoothBaseBinding.inflate(layoutInflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new EZPermission.Builder(this).applyRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN).build().requestPermission(this::init);
    }

    private void init(List<String> deniedPermissions) {
        if (!deniedPermissions.isEmpty()) {
            toast("请授予权限");
            return;
        }
        ezBluetooth = new EZBluetooth.Builder(this).setCallback(new BTBaseActivity.MyOnBluetoothCallback()).build();
        if (!ezBluetooth.isEnabled()) {
            ezBluetooth.open();
        } else {
            startDiscover();
        }
        mBinding.tvBtOpened.setText("蓝牙开启:" + ezBluetooth.isEnabled());
        try {
            mUnBondRecyclerView = new EZRecyclerView.Builder<String>(this).setCallBack(new UnBoundedViewCallBack()).setRecyclerView(mBinding.rvUnBoundedDevices).build();
            mBondRecyclerView = new EZRecyclerView.Builder<String>(this).setCallBack(new BoundedViewCallBack()).setRecyclerView(mBinding.rvBoundedDevices).build();
        } catch (Exception e) {
            Log.e(TAG, "init: ", e);
        }
    }

    private void startDiscover() {
        ezBluetooth.startDiscover();
    }

    class MyOnBluetoothCallback extends EZBluetooth.OnBluetoothCallback {
        @Override
        public void onStateChanged(Intent intent) {
            super.onStateChanged(intent);
            Bundle bundle = intent.getExtras();
            mBinding.tvBtOpened.setText("蓝牙开启:" + (bundle.getInt(BluetoothAdapter.EXTRA_STATE) == BluetoothAdapter.STATE_ON));
        }

        @Override
        public void onUserGrantedOpen(boolean granted) {
            super.onUserGrantedOpen(granted);
            toast("用户授权了打开蓝牙");
        }

        @Override
        public void onUserGrantedDiscover(boolean granted) {
            super.onUserGrantedDiscover(granted);
            toast("用户授权了发现蓝牙设备");
        }

        @Override
        public void onFoundedDevice(BluetoothDevice bluetoothDevice) {
            super.onFoundedDevice(bluetoothDevice);
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                if (!mBondedDevices.contains(bluetoothDevice)) mBondedDevices.add(bluetoothDevice);
            } else {
                if (!mUnBondedDevices.contains(bluetoothDevice))
                    mUnBondedDevices.add(bluetoothDevice);
            }
        }

        @Override
        public void onDiscoveryStarted() {
            super.onDiscoveryStarted();
            toast("开始发现附近蓝牙中...");
            mUnBondedDevices.clear();
            mBondedDevices.clear();
        }

        @Override
        public void onDiscoveryFinished() {
            super.onDiscoveryFinished();
            toast("结束附近蓝牙设备搜索");
            mUnBondRecyclerView.setDataList(mUnBondedDevices);
            mBondRecyclerView.setDataList(mBondedDevices);
        }

        @Override
        public void onBondStateChanged(Intent intent) {
            super.onBondStateChanged(intent);
            Bundle bundle = intent.getExtras();
            int state = bundle.getInt(BluetoothDevice.EXTRA_BOND_STATE);
            BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
            toast(state == BluetoothDevice.BOND_BONDED ? "绑定成功" : "未绑定");
            if (state == BluetoothDevice.BOND_BONDED) {
                if (!mBondedDevices.contains(device)) {
                    mBondedDevices.add(device);
                    mBondRecyclerView.setDataList(mBondedDevices);
                }
                if (mUnBondedDevices.contains(device)) {
                    mUnBondedDevices.remove(device);
                    mUnBondRecyclerView.setDataList(mUnBondedDevices);
                }
            }
        }
    }

    class UnBoundedViewCallBack extends EZRecyclerView.EZRecyclerViewCallBack {

        @Override
        protected int setItemViewLayoutResId() {
            return R.layout.rv_item_text;
        }

        @Override
        protected void bindViewHolder(View itemView, int position) {
            TextView textView = itemView.findViewById(R.id.tv);
            textView.setText(mUnBondedDevices.get(position).getName());
        }

        @Override
        protected void onItemClick(View itemView, int position) {
            super.onItemClick(itemView, position);
            BluetoothDevice device = mUnBondedDevices.get(position);
            device.createBond();
        }
    }

    class BoundedViewCallBack extends EZRecyclerView.EZRecyclerViewCallBack {

        @Override
        protected int setItemViewLayoutResId() {
            return R.layout.rv_item_text;
        }

        @Override
        protected void bindViewHolder(View itemView, int position) {
            TextView textView = itemView.findViewById(R.id.tv);
            textView.setText(mBondedDevices.get(position).getName());
        }

        @Override
        protected void onItemClick(View itemView, int position) {
            super.onItemClick(itemView, position);
            selectBondDevice = mBondedDevices.get(position);
            send("hello");
        }
    }


    public void send(String sendData) {
        if (selectBondDevice == null) {
            return;
        }
        ezBluetooth.startClientSocketConnectThread(selectBondDevice, server -> {
            try (OutputStream outputStream = server.getOutputStream()) {
                byte[] data = sendData.getBytes("gbk");
                server.getOutputStream().write(data, 0, data.length);
                outputStream.flush();
            } catch (Exception e) {
                EZLogUtil.e(TAG, "", e);
            }
        });
    }

}