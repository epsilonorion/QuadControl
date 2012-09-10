package com.qinetiq.quadcontrol;


//TODO Remove in future version
public class VehicleStatus {
	StatusInfo vehicleStatus;

	public VehicleStatus() {
		vehicleStatus = new StatusInfo();
	}	
	
	public synchronized void setVehicleStatus(StatusInfo vehicleStatus) {
		this.vehicleStatus = vehicleStatus;
	}
	
	public synchronized StatusInfo getVehicleStatus() {
		return this.vehicleStatus;
	}
}
