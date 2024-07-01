package com.rhythmcoder.androidstudysystem.wifi.p2p.wifidirect;

import java.net.InetAddress;

public class WifiDirectConnectInfo {
    //Group Info
    public int mSourcePort;
    public String mSourceMacAddr;

    //connection info
    public boolean isGroupOwner;
    public boolean isGroupFormed;
    public InetAddress groupOwnerAddress;

    @Override
    public String toString() {
        return "WifiDirectConnectInfo{" +
                "mSourcePort=" + mSourcePort +
                ", mSourceMacAddr='" + mSourceMacAddr + '\'' +
                ", isGroupOwner=" + isGroupOwner +
                ", isGroupFormed=" + isGroupFormed +
                ", groupOwnerAddress=" + groupOwnerAddress +
                '}';
    }
}
