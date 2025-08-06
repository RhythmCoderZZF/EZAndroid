package com.rhythmcoderzzf.ezandroid.connection;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentActivity;

import com.rhythmcoderzzf.ezandroid.core.AbstractBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class EZBluetooth {
    private static final String TAG = EZBluetooth.class.getSimpleName();
    private final FragmentActivity mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private OnBluetoothCallback onBluetoothCallback;
    private ActivityResultLauncher<Intent> mEnableBluetoothLauncher;
    private ActivityResultLauncher<Intent> mEnableDiscoverable;
    private final List<BluetoothDevice> mFoundedDevices = new ArrayList<>();
    private String mServerName;
    private UUID mUUID;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //当每发现一台蓝牙设备，都会广播该设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mFoundedDevices.add(device);
            }
        }
    };

    public EZBluetooth(FragmentActivity tmpContext) {
        this.mContext = tmpContext;
        mBluetoothManager = mContext.getSystemService(BluetoothManager.class);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }
        //必须在activity started之前注册
        mEnableBluetoothLauncher = mContext.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            onBluetoothCallback.onUserGrantedOpen(result.getResultCode() == RESULT_OK);
        });

        mEnableDiscoverable = mContext.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            onBluetoothCallback.onUserGrantedDiscover(result.getResultCode() == RESULT_OK);
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);
    }

    private EZBluetooth onInit() {
        if (onBluetoothCallback == null) {
            throw new IllegalStateException();
        }
        return this;
    }


    /**
     * 开启蓝牙开关。系统会显示一个对话框，请求用户授予
     */
    public void open() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mEnableBluetoothLauncher.launch(enableBtIntent);
        }
    }

    /**
     * 查询已配对设备
     *
     * @return 已配对的设备
     */
    public Set<BluetoothDevice> getPairedDevices() {
        if (!mBluetoothAdapter.isEnabled()) {
            return mBluetoothAdapter.getBondedDevices();
        }
        return null;
    }

    /**
     * 将设备设置为在五分钟内可被发现。系统会显示一个对话框，请求用户授予
     */
    public void setDiscoverable() {
        setDiscoverable(300);
    }

    /**
     * 设置设备在一定时间被可以被发现。系统会显示一个对话框，请求用户授予
     *
     * @param seconds 被发现的秒数
     */
    public void setDiscoverable(int seconds) {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
            mEnableDiscoverable.launch(intent);
        }
    }

    /**
     * 设备作为Server端，启动AcceptThread监听并接收Client socket接入
     *
     * @param callback         接收到客户端回调
     * @param closeImmediately 服务端处理完Client接收任务后，是否立即关闭
     * @return AcceptThread
     */
    public AcceptThread startServerSocketListenThread(ServerSocketCallback callback, boolean closeImmediately) {
        AcceptThread acceptThread = new AcceptThread(callback, closeImmediately);
        acceptThread.start();
        return acceptThread;
    }

    /**
     * 设备作为Client端，启动ConnectThread向目标BluetoothDevice发起连接
     *
     * @param device   目标BluetoothDevice
     * @param callback 连接成功后回调
     * @return
     */
    public ConnectThread startClientSocketConnectThread(BluetoothDevice device, ClientSocketCallback callback) {
        ConnectThread connectThread = new ConnectThread(device, callback);
        connectThread.start();
        return connectThread;
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private final ServerSocketCallback serverSocketCallback;
        private final boolean closeImmediately;

        public AcceptThread(ServerSocketCallback callback, boolean closeImmediately) {
            this.serverSocketCallback = callback;
            this.closeImmediately = closeImmediately;
            BluetoothServerSocket tmp = null;
            try {
                //Create a listening, secure RFCOMM Bluetooth socket with Service Record.
                //A remote device connecting to this socket will be authenticated and communication on this socket will be encrypted.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mServerName, mUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }
                if (socket != null) {
                    serverSocketCallback.acceptClientSocket(socket);
                    if (closeImmediately) closeServer();
                    break;
                }
            }
        }

        public void closeServer() {
            try {
                if (mmServerSocket != null) mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final ClientSocketCallback clientSocketCallback;

        public ConnectThread(BluetoothDevice device, ClientSocketCallback callback) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            clientSocketCallback = callback;

            try {
                //Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device using SDP lookup of uuid.
                tmp = mmDevice.createRfcommSocketToServiceRecord(mUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                cancel();
                return;
            }
            clientSocketCallback.acceptServerSocket(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }


    public static class Builder implements AbstractBuilder<EZBluetooth> {
        private final FragmentActivity context;
        private OnBluetoothCallback onBluetoothCallback;
        private String name;
        private UUID uuid;

        public Builder(FragmentActivity context) {
            this.context = context;
        }

        /**
         * 设置蓝牙回调接口
         *
         * @param callback 回调接口
         */
        public Builder setOnBluetoothCallback(OnBluetoothCallback callback) {
            onBluetoothCallback = callback;
            return this;
        }

        /**
         * 设置当前蓝牙设备作为服务端连接的一些配置
         *
         * @param name 该字符串是服务的可识别名称， 自动写入新的服务发现协议 (SDP) 数据库条目 。该名称没有限制，可以直接使用您的应用名称。
         * @param uuid 当客户端尝试连接此设备时，它会携带一个 UUID 用于标识自己想要连接的服务的唯一标识符。这些 UUID 必须匹配，连接才会被接受。
         *             一般都是固定的：UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
         */
        public Builder applyRfcommWithServerRecordConfig(String name, UUID uuid) {
            this.name = name;
            this.uuid = uuid;
            return this;
        }

        /**
         * 设置当前蓝牙设备作为客户端连接的一些配置
         *
         * @param uuid 当客户端尝试连接此设备时，它会携带一个 UUID 用于标识自己想要连接的服务的唯一标识符。这些 UUID 必须匹配，连接才会被接受。
         *             一般都是固定的：UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
         */
        public Builder applyRfcommWithClientRecordConfig(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public EZBluetooth build() {
            EZBluetooth ezBluetooth = new EZBluetooth(context);
            ezBluetooth.onBluetoothCallback = onBluetoothCallback;
            ezBluetooth.mServerName = name;
            ezBluetooth.mUUID = uuid;
            return ezBluetooth.onInit();
        }
    }

    /**
     * 蓝牙通用回调接口
     */
    public static class OnBluetoothCallback {
        /**
         * 请求打开蓝牙
         *
         * @param granted 用户是否授权
         */
        public void onUserGrantedOpen(boolean granted) {
        }

        /**
         * 请求当前蓝牙设备允许被其他设备发现
         *
         * @param granted 用户是否授权
         */
        public void onUserGrantedDiscover(boolean granted) {
        }
    }

    /**
     * 设备作为服务端，蓝牙通信Socket回调
     */
    interface ServerSocketCallback {
        /**
         * 服务端接收到客户端成功接入的回调
         *
         * @param client 客户端socket
         */
        void acceptClientSocket(BluetoothSocket client);
    }

    /**
     * 设备作为客户端，蓝牙通信Socket回调
     */
    interface ClientSocketCallback {
        /**
         * 客户端成功连接上服务端的回调
         *
         * @param server 服务端socket
         */
        void acceptServerSocket(BluetoothSocket server);
    }
}
