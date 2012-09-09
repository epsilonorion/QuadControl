package com.qinetiq.quadcontrol;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import android.app.Application;

public class MainApplication extends Application {

	private NodeMainExecutor nodeMainExecutor;
	private NodeConfiguration nodeConfiguration;

	private StatusInfo statusInfo;
	
	private int test;

	private boolean ConnectedToVehicle = false;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	// GETTERS AND SETTERS FOR GLOBAL VARIABLES
	public NodeMainExecutor getNodeMainExecutor() {
		return nodeMainExecutor;
	}

	public void setNodeMainExecutor(NodeMainExecutor nodeMainExecutor) {
		this.nodeMainExecutor = nodeMainExecutor;
	}

	public NodeConfiguration getNodeConfiguration() {
		return nodeConfiguration;
	}

	public void setNodeConfiguration(NodeConfiguration nodeConfiguration) {
		this.nodeConfiguration = nodeConfiguration;
	}

	public StatusInfo getStatusInfo() {
		return statusInfo;
	}

	public void setStatusInfo(StatusInfo statusInfo) {
		this.statusInfo = statusInfo;
	}

	public boolean isConnectedToVehicle() {
		return ConnectedToVehicle;
	}

	public void setConnectedToVehicle(boolean connectedToVehicle) {
		ConnectedToVehicle = connectedToVehicle;
	}

	public int getTest() {
		return test;
	}

	public void setTest(int test) {
		this.test = test;
	}

	
}
