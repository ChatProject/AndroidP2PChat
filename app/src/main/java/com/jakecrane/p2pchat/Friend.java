package com.jakecrane.p2pchat;

import java.io.Serializable;

public class Friend implements Serializable {

    private static final long serialVersionUID = -9095423069662282316L;

    private String username;
    private String ipv4_address;
    private int listeningPort;
    private long lastActive;

    public Friend() {

    }

    public Friend(String username, String ipv4_address, int listeningPort, long lastActive) {
        this.username = username;
        this.ipv4_address = ipv4_address;
        this.listeningPort = listeningPort;
        this.lastActive = lastActive;
    }

    public String getUsername() {
        return username;
    }

    public String getIpv4_address() {
        return ipv4_address;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public long getLastActive() {
        return lastActive;
    }

    @Override
    public String toString() {
        return username;
    }
}
