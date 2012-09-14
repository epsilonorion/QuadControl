package com.qinetiq.quadcontrol;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import com.qinetiq.quadcontrol.fragments.StatusFragment;

import android.app.Application;

public class MainApplication extends Application {

	private NodeMainExecutor nodeMainExecutor;
	private NodeConfiguration nodeConfiguration;

	private VehicleStatus vehicleStatus;
	private WaypointList wayptList;
	
	private StatusFragment statusFrag = null;

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

	public VehicleStatus getVehicleStatus() {
		return vehicleStatus;
	}

	public void setVehicleStatus(VehicleStatus vehicleStatus) {
		this.vehicleStatus = vehicleStatus;
	}

	public WaypointList getWayptList() {
		return wayptList;
	}

	public void setWayptList(WaypointList wayptList) {
		this.wayptList = wayptList;
	}

	public boolean isConnectedToVehicle() {
		return ConnectedToVehicle;
	}

	public void setConnectedToVehicle(boolean connectedToVehicle) {
		ConnectedToVehicle = connectedToVehicle;
	}

	public StatusFragment getStatusFrag() {
		return statusFrag;
	}

	public void setStatusFrag(StatusFragment statusFrag) {
		this.statusFrag = statusFrag;
	}
}
