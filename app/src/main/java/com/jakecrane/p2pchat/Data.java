package com.jakecrane.p2pchat;

import java.io.Serializable;

public class Data implements Serializable {
	
	private static final long serialVersionUID = -2723363051271966964L;

	private long createdTime;
	private long receivedTime;
	private String message;
    private String senderDisplayName;
	
	public Data(String message, String senderDisplayName) {
		this.message = message;
        this.senderDisplayName = senderDisplayName;
		createdTime = System.currentTimeMillis();
	}
	
	public long getCreatedTime() {
		return createdTime;
	}
	
	public long getReceivedTime() {
		return receivedTime;
	}
	
	public void setReceivedTime(long millis) {
		receivedTime = millis;
	}
	
	public String getMessage() {
		return message;
	}

    public String getSenderDisplayName() { return senderDisplayName; }
}
