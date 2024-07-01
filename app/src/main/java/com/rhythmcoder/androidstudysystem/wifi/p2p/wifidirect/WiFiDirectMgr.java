package com.rhythmcoder.androidstudysystem.wifi.p2p.wifidirect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.rhythmcoder.androidstudysystem.permission.PermissionUtil;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Wifi Direct封装类。
 * 【1初始化】
 * 【2发现对等设备】
 * 【3连接对等设备】
 */
//【1初始化】1.在AndroidManifest中，请求在设备上使用 Wi-Fi 硬件的权限。注意区分targetSdk = 33的设备权限申请，具体参见官网
public class WiFiDirectMgr implements WiFiDirectBroadcastReceiver.WifiP2pBroadcastReceiverListener {
    private static final String TAG = "WiFiDirectMgr";
    private final Context mContext;
    private final OnWifiDirectListener mOnWifiDirectListener;

    private ConnectivityManager mConnectivityManager;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mWiFiDirectBroadcastReceiver;
    private WifiP2pDeviceList mTmpWifiP2pDeviceList;
    private WifiDirectConnectInfo mWifiDirectConnectInfo;

    private boolean mIsStart = false;
    private boolean mIsServer = false;
    private boolean mIsNetworkConnected = false;

    public WiFiDirectMgr(Context context, OnWifiDirectListener listener) {
        mContext = context;
        mOnWifiDirectListener = listener;
    }

    public void start(boolean asServer) {
        if (mIsStart) {
            Log.e(TAG, "already started");
            return;
        }
        if (!checkCapability()) {
            return;
        }
        new PermissionUtil(mContext, Manifest.permission.ACCESS_FINE_LOCATION, granted -> {
            if (!granted) {
                return;
            }
            mIsStart = true;
            mIsServer = asServer;
            mWiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(mWifiP2pManager, mChannel, this);
            //【1初始化】3.创建一个 intent 过滤器,注册Wifi p2p广播
            mContext.registerReceiver(mWiFiDirectBroadcastReceiver, mWiFiDirectBroadcastReceiver.getIntentFilter());
            if (asServer) {
                Log.d(TAG, "====createGroup====\n");
                //【2初始化】1.如果设备是作为server端，则需要通过createGroup创建一个组，自己作为GO(group owner)
                mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "Server create p2p group success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "Server create p2p group failure:" + reason);
                    }
                });
            } else {
                Log.d(TAG, "====discoverPeers====\n");
                //【2发现对等设备】1.作为Client端，需要调用startDiscovery去发现GO并连接
                mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //onSuccess() 方法只会通知您发现过程已成功，而不会提供它发现的实际对等设备（如果有）的任何信息
                        //获取通知需要再broadcast中获取
                        Log.i(TAG, "Client Successfully initialed peers discovery.");
                    }

                    @Override
                    public void onFailure(int reason) {
                        mIsStart = false;
                        Log.e(TAG, "Client Failed to initial peers discovery:" + reason);
                    }
                });
            }
        }).requestPermission();
    }

    public void stop() {
        if (mIsStart) {
            Log.d(TAG, "====stopPeerDiscovery====\n");
            mWifiP2pManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "Client Successfully stopped peers discovery.");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Client Failed to stop peers discovery:" + reason);
                }
            });
            Log.d(TAG, "====cancelConnect====\n");
            mWifiP2pManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "cancel Connect onSuccess");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "cancel Connect onFailure:" + reason);
                }
            });
            if (mIsServer) {
                Log.d(TAG, "====removeGroup====\n");
                mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "Server remove p2p group success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "Server remove p2p group onFailure:" + reason);
                    }
                });
            }

            mContext.unregisterReceiver(mWiFiDirectBroadcastReceiver);
            mOnWifiDirectListener.onSessionConnected(false);
            mIsStart = false;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void connectDevice(String deviceName) {
        if (mTmpWifiP2pDeviceList != null) {
            List<WifiP2pDevice> wifiP2pDevices = mTmpWifiP2pDeviceList.getDeviceList().stream().filter(new Predicate<WifiP2pDevice>() {
                @Override
                public boolean test(WifiP2pDevice wifiP2pDevice) {
                    return wifiP2pDevice.deviceName.equals(deviceName);
                }
            }).collect(Collectors.toList());
            if (!wifiP2pDevices.isEmpty()) {
                WifiP2pDevice device = wifiP2pDevices.get(0);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                Log.d(TAG, "====connect====\n");
                //【3连接对等设备】1.获取可能的对等设备列表并选择要连接的设备后，请调用 connect() 方法以连接到该设备。
                mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                        Log.i(TAG, "Client connect device:" + deviceName + " success");
                        //【3连接对等设备】3.当Client和Server已经连接过(设置>wifi直连显示"已连接"),不会回调WIFI_P2P_CONNECTION_CHANGED_ACTION，所以这里需要主动获取ConnectionInfo，执行接下来流程
                        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                        if (activeNetworkInfo.isConnected()) {
                            mWifiP2pManager.requestConnectionInfo(mChannel, info -> {
                                if (info != null && info.groupFormed) {
                                    Log.i(TAG, "wifi p2p Group is already Formed");
                                    WiFiDirectMgr.this.onConnectionInfoAvailable(info);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(int reason) {
                        //failure logic
                        Log.e(TAG, "Client connect device:" + deviceName + " failed:" + reason);
                    }
                });
            } else {
                Log.e(TAG, deviceName + " is not exist");
            }
        }
    }

    public WifiDirectConnectInfo getmWifiDirectConnectInfo() {
        return mWifiDirectConnectInfo;
    }

    private boolean checkCapability() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(TAG, "Capability: Wi-Fi Direct is not supported by this device.");
            return false;
        }
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.e(TAG, "Capability: Cannot get Wi-Fi system service.");
            return false;
        }
        if (!wifiManager.isP2pSupported()) {
            Log.e(TAG, "Capability: Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
            return false;
        }
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        if (mWifiP2pManager == null) {
            Log.e(TAG, "Capability: Cannot get Wi-Fi Direct system service.");
            return false;
        }
        Log.d(TAG, "====initialize====\n");
        //【1初始化】2.向 Wi-Fi 点对点框架注册您的应用,得到一个Channel，它用于将应用连接到 WLAN 点对点连接框架
        mChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), () -> {
            Log.d(TAG, "Capability: The channel to the framework has been disconnected.");
        });
        if (mChannel == null) {
            Log.e(TAG, "Capability: Cannot initialize Wi-Fi Direct.");
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.d(TAG, "++++ onPeersAvailable wifiP2pDeviceList: ++++ \n" + wifiP2pDeviceList.toString() + "\n--------");
        mTmpWifiP2pDeviceList = wifiP2pDeviceList;
        List<String> deviceNames = wifiP2pDeviceList.getDeviceList().stream().map(wifiP2pDevice -> wifiP2pDevice.deviceName).collect(Collectors.toList());
        mOnWifiDirectListener.onDevicePeersDiscovered(deviceNames);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info != null) {
            mWifiDirectConnectInfo = new WifiDirectConnectInfo();
            Log.d(TAG, "++++  onConnectionInfoAvailable WifiP2pInfo: ++++ \n" + info + "\n--------");
            mWifiDirectConnectInfo.isGroupOwner = info.isGroupOwner;
            mWifiDirectConnectInfo.isGroupFormed = info.groupFormed;
            mWifiDirectConnectInfo.groupOwnerAddress = info.groupOwnerAddress;
            mWifiP2pManager.requestGroupInfo(mChannel, this::onGroupInfoAvailable);
        }
    }

    public void onGroupInfoAvailable(WifiP2pGroup group) {
        if (group != null) {
            Log.d(TAG, "++++  onGroupInfoAvailable WifiP2pGroup: ++++ \n" + group + "\n--------");
            onSessionConnected(true);
        }
    }

    @Override
    public void setWifiP2pEnabled(boolean enabled) {
        if (!enabled) {
            Log.e(TAG, "wifi p2p is disabled");
            stop();
        }
    }

    @Override
    public void updateThisP2pDevice(WifiP2pDevice wifiP2pDevice) {
    }

    @Override
    public void onSessionConnected(boolean isConnected) {
        if (mIsNetworkConnected != isConnected) {
            mIsNetworkConnected = isConnected;
            if (!isConnected) {
                mWifiDirectConnectInfo = null;
            }
            mOnWifiDirectListener.onSessionConnected(isConnected);
            Log.i(TAG, "onSessionConnect status: " + (isConnected ? "connected" : "disConnected"));
        }
    }

    /**
     * Wifi p2p 连接信息回调
     */
    public interface OnWifiDirectListener {
        /**
         * 向Client返回对等p2p设备名称。Client需要选择某一个设备名称来通过{@link #connectDevice(String)}方法去建立连接
         *
         * @param deviceNames
         */
        void onDevicePeersDiscovered(List<String> deviceNames);

        /**
         * p2p连接状态
         *
         * @param isConnected true:Client和Server连接成功
         */
        void onSessionConnected(boolean isConnected);
    }

}
