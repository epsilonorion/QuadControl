package com.qinetiq.quadcontrol;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import android.app.Application;

public class MainApplication extends Application {
	
	private NodeMainExecutor nodeMainExecutor;
	private NodeConfiguration nodeConfiguration;
	
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

	public boolean isConnectedToVehicle() {
		return ConnectedToVehicle;
	}

	public void setConnectedToVehicle(boolean connectedToVehicle) {
		ConnectedToVehicle = connectedToVehicle;
	}
}
