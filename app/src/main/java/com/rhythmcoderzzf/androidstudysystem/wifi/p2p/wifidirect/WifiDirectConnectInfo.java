package com.rhythmcoderzzf.androidstudysystem.wifi.p2p.wifidirect;

import java.net.InetAddress;

public class WifiDirectConnectInfo {
    //connection info
    public boolean isGroupOwner;
    public boolean isGroupFormed;
    public InetAddress groupOwnerAddress;

    @Override
    public String toString() {
        return "WifiDirectConnectInfo{" +
                ", isGroupOwner=" + isGroupOwner +
                ", isGroupFormed=" + isGroupFormed +
                ", groupOwnerAddress=" + groupOwnerAddress +
                '}';
    }
}
