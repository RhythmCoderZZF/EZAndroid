package com.rhythmcoder.androidstudysystem.wifi.p2p.wifidirect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import com.rhythmcoder.baselib.utils.LogUtil;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//【1初始化】1.在AndroidManifest中，请求在设备上使用 Wi-Fi 硬件的权限
public class WiFiDirectMgr implements WiFiDirectBroadcastReceiver.WifiP2pBroadcastReceiverListener {
    private static final String TAG = "WiFiDirectMgr";
    private final Context mContext;
    private final OnWifiDirectListener mOnWifiDirectListener;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mWiFiDirectBroadcastReceiver;

    private static boolean mIsStart = false;
    private WifiP2pDeviceList mTmpWifiP2pDeviceList;
    private WifiDirectConnectInfo mWifiDirectConnectInfo;
    private boolean mIsServer = false;

    public WiFiDirectMgr(Context context, OnWifiDirectListener listener) {
        mContext = context;
        mOnWifiDirectListener = listener;
    }

    //【1初始化】3.向 Wi-Fi 点对点框架注册您的应用,得到一个Channel，它用于将应用连接到 WLAN 点对点连接框架
    public void start(boolean asServer) {
        if (mIsStart) {
            LogUtil.e(TAG, " already started");
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
            //【1初始化】4.创建一个 intent 过滤器,注册广播
            mContext.registerReceiver(mWiFiDirectBroadcastReceiver, mWiFiDirectBroadcastReceiver.getIntentFilter());
            //【1初始化】5.如果设备想作为p2p server，则可以通过createGroup创建一个组；否则可以调用startDiscovery去发现组
            if (asServer) {
                mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Server create p2p group success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "Server create p2p group failure");
                    }
                });
            } else {
                startDiscovery();
            }
        }).requestPermission();
    }

    public void stop() {
        if (mIsStart) {
            stopDiscovery();
            if (mIsServer) {
                mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Server remove p2p group success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Server remove p2p group success");
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
                mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                        Log.i(TAG, "wifi p2p connect device:" + deviceName + " success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        //failure logic
                        Log.e(TAG, "wifi p2p connect device:" + deviceName + " failed");
                    }
                });
            } else {
                Log.e(TAG, deviceName + " is not exist");
            }
        }
    }

    //【2发现对等设备】1.调用 discoverPeers() 以检测范围内且可用于连接的可用对等设备。
    private void startDiscovery() {
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //onSuccess() 方法只会通知您发现过程已成功，而不会提供它发现的实际对等设备（如果有）的任何信息
                Log.d(TAG, "Successfully initialed peers discovery.");
            }

            @Override
            public void onFailure(int reason) {
                mIsStart = false;
                Log.e(TAG, "Failed to initial peers discovery.");
            }
        });
    }

    private void stopDiscovery() {
        mWifiP2pManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: Successfully stopped peers discovery.");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Failed to stop peers discovery.");
            }
        });
    }

    public WifiDirectConnectInfo getmWifiDirectConnectInfo() {
        return mWifiDirectConnectInfo;
    }

    private boolean checkCapability() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            LogUtil.e(TAG, "Capability: Wi-Fi Direct is not supported by this device.");
            return false;
        }
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            LogUtil.e(TAG, "Capability: Cannot get Wi-Fi system service.");
            return false;
        }
        if (!wifiManager.isP2pSupported()) {
            LogUtil.e(TAG, "Capability: Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
            return false;
        }
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        if (mWifiP2pManager == null) {
            LogUtil.e(TAG, "Capability: Cannot get Wi-Fi Direct system service.");
            return false;
        }
        mChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), () -> {
            LogUtil.w(TAG, "Capability: The channel to the framework has been disconnected.");
        });
        if (mChannel == null) {
            LogUtil.e(TAG, "Capability: Cannot initialize Wi-Fi Direct.");
            return false;
        }
        return true;
    }


//    private void setGroupInfo(String groupInfo) {
//        if (!TextUtils.isEmpty(groupInfo)) {
//            mWifiDirectConnectInfo.mSourceMacAddr = groupInfo.substring(groupInfo.lastIndexOf("deviceAddress") + 15, groupInfo.lastIndexOf("primary type")).trim();
//            String sourcePortStr = groupInfo.substring(groupInfo.lastIndexOf("WFD CtrlPort") + 14, groupInfo.lastIndexOf("WFD MaxThroughput")).trim();
//            if (!TextUtils.isEmpty(sourcePortStr)) {
//                int tmp = Integer.parseInt(sourcePortStr);
//                mWifiDirectConnectInfo.mSourcePort = (tmp == 0) ? 7236 : tmp;
//            }
//        }
//    }


//    private void setConnInfo(String connectInfo) {
//        if (!TextUtils.isEmpty(connectInfo)) {
//            connectInfo = connectInfo.replace(":", "");
//            String[] connectInfoArr = connectInfo.split(" ");
//            for (int i = 0; i < connectInfoArr.length - 1; i += 2) {
//                if ("groupFormed".equals(connectInfoArr[i])) {
//                    mWifiDirectConnectInfo.isGroupFormed = "true".equals(connectInfoArr[i + 1]);
//                } else if ("isGroupOwner".equals(connectInfoArr[i])) {
//                    mWifiDirectConnectInfo.isGroupOwner = "true".equals(connectInfoArr[i + 1]);
//                } else if ("groupOwnerAddress".equals(connectInfoArr[i])) {
//                    if (!mWifiDirectConnectInfo.isGroupOwner) {
//                        mWifiDirectConnectInfo.mSourceIp = connectInfoArr[i + 1].substring(1);
//                    }
//                }
//            }
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.d(TAG, "\n====== onPeersAvailable wifiP2pDeviceList: ======\n" + wifiP2pDeviceList.toString() + "\n==================");
        mTmpWifiP2pDeviceList = wifiP2pDeviceList;
        List<String> deviceNames = wifiP2pDeviceList.getDeviceList().stream().map(wifiP2pDevice -> wifiP2pDevice.deviceName).collect(Collectors.toList());
        mOnWifiDirectListener.onDevicePeersDiscovered(deviceNames);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info != null) {
            mWifiDirectConnectInfo = new WifiDirectConnectInfo();
            String connectInfoStr = info.toString();
            Log.d(TAG, "\n====== onConnectionInfoAvailable WifiP2pInfo: ======\n" + connectInfoStr + "\n==================");
            mWifiDirectConnectInfo.isGroupOwner = info.isGroupOwner;
            mWifiDirectConnectInfo.isGroupFormed = info.groupFormed;
            mWifiDirectConnectInfo.groupOwnerAddress = info.groupOwnerAddress;
            mWifiP2pManager.requestGroupInfo(mChannel, this::onGroupInfoAvailable);
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        if (group != null) {
            String groupInfoStr = group.toString();
            Log.d(TAG, "\n====== onGroupInfoAvailable WifiP2pGroup: ======\n" + groupInfoStr + "\n==================");
//            setGroupInfo(groupInfoStr);
//            mWifiDirectConnectInfo.mSourceMacAddr = group.
            onSessionConnected(true);
        }
    }

    @Override
    public void setWifiP2pEnabled(boolean enabled) {
    }

    @Override
    public void updateThisP2pDevice(WifiP2pDevice wifiP2pDevice) {

    }

    @Override
    public void onSessionConnected(boolean isConnected) {
        mIsStart = isConnected;
        if (!isConnected) {
            mWifiDirectConnectInfo = null;
        }
        mOnWifiDirectListener.onSessionConnected(isConnected);
        Log.i(TAG, "onSessionConnect status: " + (isConnected ? "connected" : "disConnected"));
    }


    public interface OnWifiDirectListener {
        void onDevicePeersDiscovered(List<String> deviceNames);

        void onSessionConnected(boolean isConnected);
    }

}
