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

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.rhythmcoderzzf.ezandroid.core.AbstractBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * EZ Bluetooth。
 * <p>使用方式</p>
 * <pre>
 *  EZBluetooth ezBluetooth = new EZBluetooth.Builder(this)
 *                 .setCallback(BluetoothCallback())
 *                 .build();
 *  //搜索
 *  ezBluetooth.startDiscover();
 *  //向远程设备发起连接。需要bond成功
 *  ezBluetooth.startConnectToServerThread()
 * </pre>
 */
@SuppressLint("MissingPermission")
public class EZBluetooth implements DefaultLifecycleObserver {
    private static final String TAG = EZBluetooth.class.getSimpleName();
    private final Context mContext;
    private BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    private ActivityResultLauncher<Intent> mEnableBluetoothLauncher;
    private ActivityResultLauncher<Intent> mEnableDiscoverable;
    private OnBluetoothCallback mCallback;
    private boolean mListenActivityLifecycle = true;
    private String mServerName;
    private UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //发现一台蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mCallback != null) mCallback.onFoundedDevice(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //开始发现蓝牙
                if (mCallback != null) mCallback.onDiscoveryStarted();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //结束发现蓝牙
                if (mCallback != null) mCallback.onDiscoveryFinished();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                //蓝牙开关状态改变
                if (mCallback != null) mCallback.onStateChanged(intent);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                //蓝牙配对状态改变
                if (mCallback != null) mCallback.onBondStateChanged(intent);
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //ACL连接
                if (mCallback != null) mCallback.onACLConnected(intent, true);
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //ACL断开
                if (mCallback != null) mCallback.onACLConnected(intent, false);
            }
        }
    };

    private EZBluetooth(Context tmpContext) {
        this.mContext = tmpContext;
        mBluetoothManager = mContext.getSystemService(BluetoothManager.class);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            throw new IllegalStateException();
        }
    }

    private EZBluetooth onInit() {
        //注意必须在Activity onStart之前注册
        if (mContext instanceof ComponentActivity) {
            ComponentActivity activity = (ComponentActivity) mContext;
            mEnableBluetoothLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (mCallback != null)
                    mCallback.onUserGrantedOpen(result.getResultCode() == RESULT_OK);
            });

            mEnableDiscoverable = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (mCallback != null)
                    mCallback.onUserGrantedDiscover(result.getResultCode() == RESULT_OK);
            });
        }

        if (mContext instanceof LifecycleOwner && mListenActivityLifecycle) {
            LifecycleOwner lifecycleOwner = (LifecycleOwner) mContext;
            lifecycleOwner.getLifecycle().addObserver(this);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(mReceiver, filter);
        return this;
    }

    @Override
    final public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        release();
    }

    //结束发现附近蓝牙
    private void cancelDiscovery() {
        if (mBluetoothAdapter.isEnabled() && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
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
     * 释放所有资源。该方法一般再LifecycleOwner中的onDestroy中自动释放，如果context不支持lifecycle，则需要手动释放
     */
    public void release() {
        if (mContext instanceof LifecycleOwner) {
            LifecycleOwner lifecycleOwner = (LifecycleOwner) mContext;
            lifecycleOwner.getLifecycle().removeObserver(this);
        }
        cancelDiscovery();
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (acceptThread != null) {
            acceptThread.closeServer();
            acceptThread = null;
        }
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    /**
     * 蓝牙开关是否开启
     *
     * @return 开启状态
     */
    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 查询已配对设备
     *
     * @return 已配对的设备
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return mBluetoothAdapter.getBondedDevices();
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
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
        mEnableDiscoverable.launch(intent);
    }

    /**
     * 开始发现附近蓝牙
     */
    public void startDiscover() {
        if (mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
        }
    }

    /**
     * 设备作为Server端，启动AcceptThread监听并接收Client socket接入
     *
     * @param callback         接收到客户端回调
     * @param closeImmediately 服务端处理完Client接收任务后，是否立即关闭
     * @return AcceptThread
     */
    public AcceptThread startListenClientThread(ServerSocketCallback callback, boolean closeImmediately) {
        if (acceptThread != null) {
            acceptThread.closeServer();
            acceptThread = null;
        }
        acceptThread = new AcceptThread(callback, closeImmediately);
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
    public ConnectThread startConnectToServerThread(BluetoothDevice device, ClientSocketCallback callback) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        connectThread = new ConnectThread(device, callback);
        connectThread.start();
        return connectThread;
    }

    //设备作为Server端，接收Client端请求和数据
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;
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
            mServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    //监听客户端接入，当调用close()方法会出发IO异常，从而跳出while循环
                    socket = mServerSocket.accept();
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
                if (mServerSocket != null) mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    //设备作为Client端，向Server端请求和发送数据
    private class ConnectThread extends Thread {
        private BluetoothSocket mSocket;
        private final ClientSocketCallback clientSocketCallback;

        public ConnectThread(BluetoothDevice device, ClientSocketCallback callback) {
            clientSocketCallback = callback;
            try {
                //Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device using SDP lookup of uuid.
                mSocket = device.createRfcommSocketToServiceRecord(mUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        }

        public void run() {
            try {
                mBluetoothAdapter.cancelDiscovery();
                mSocket.connect();
            } catch (Exception e) {
                Log.e(TAG, "Socket's connect() method failed", e);
                cancel();
                return;
            }
            clientSocketCallback.acceptServerSocket(mSocket);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }


    public static class Builder implements AbstractBuilder<EZBluetooth> {
        private final Context context;
        private OnBluetoothCallback mCallback;
        private String name;
        private UUID uuid;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置蓝牙回调接口
         *
         * @param callback 回调接口
         */
        public Builder setCallback(OnBluetoothCallback callback) {
            mCallback = callback;
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
            ezBluetooth.mCallback = mCallback;
            ezBluetooth.mServerName = name;
            if (uuid != null) {
                ezBluetooth.mUUID = uuid;
            }
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

        /**
         * 每发现一个蓝牙设备，都会回调该方法。注意该蓝牙设备可能被重复发现，需要做过滤操作
         *
         * @param bluetoothDevice 蓝牙设备
         */
        public void onFoundedDevice(BluetoothDevice bluetoothDevice) {
        }

        /**
         * 开始发现附近蓝牙。等于{@link BluetoothAdapter#ACTION_DISCOVERY_STARTED}
         */
        public void onDiscoveryStarted() {
        }

        /**
         * 停止发现附近蓝牙。等于{@link BluetoothAdapter#ACTION_DISCOVERY_FINISHED}
         */
        public void onDiscoveryFinished() {
        }

        /**
         * 蓝牙开关状态改变。等于{@link BluetoothAdapter#ACTION_STATE_CHANGED}
         *
         * @param intent 广播携带的intent
         */
        public void onStateChanged(Intent intent) {
        }

        /**
         * 蓝牙绑定状态改变。{@link BluetoothDevice#createBond}调用后会出发广播{@link BluetoothDevice#ACTION_BOND_STATE_CHANGED}
         *
         * @param intent 广播携带的intent
         */
        public void onBondStateChanged(Intent intent) {
        }


        /**
         * 成功建立ACL（Asynchronous Connection-Less）连接。参见{@link BluetoothDevice#ACTION_ACL_CONNECTED};{@link BluetoothDevice#ACTION_ACL_DISCONNECTED}
         *
         * @param intent    intent
         * @param connected 是否连接
         */
        public void onACLConnected(Intent intent, boolean connected) {
        }
    }

    /**
     * 设备作为服务端，蓝牙通信Socket回调
     */
    public interface ServerSocketCallback {
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
    public interface ClientSocketCallback {
        /**
         * 客户端成功连接上服务端的回调
         *
         * @param server 服务端socket
         */
        void acceptServerSocket(BluetoothSocket server);
    }
}
