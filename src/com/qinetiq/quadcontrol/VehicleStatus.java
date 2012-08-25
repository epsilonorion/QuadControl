package com.qinetiq.quadcontrol;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class VehicleStatus implements Parcelable {
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
	
	// PARCEL Commands
	public VehicleStatus(Parcel in) {
		vehicleStatus.setVehicleName(in.readString());
		vehicleStatus.setLatitude(in.readDouble());
		vehicleStatus.setLongitude(in.readDouble());
		vehicleStatus.setHeading(in.readDouble());
		vehicleStatus.setSpeed(in.readDouble());
		vehicleStatus.setAltitude(in.readDouble());
		vehicleStatus.setPanAngle(in.readDouble());
		vehicleStatus.setTiltAngle(in.readDouble());
		vehicleStatus.setBatteryStatus(in.readInt());
		vehicleStatus.setGpsStatus(in.readInt());
		vehicleStatus.setCurrWaypoint(in.readInt());
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(vehicleStatus.getVehicleName());
		out.writeDouble(vehicleStatus.getLatitude());
		out.writeDouble(vehicleStatus.getLongitude());
		out.writeDouble(vehicleStatus.getHeading());
		out.writeDouble(vehicleStatus.getSpeed());
		out.writeDouble(vehicleStatus.getAltitude());
		out.writeDouble(vehicleStatus.getPanAngle());
		out.writeDouble(vehicleStatus.getTiltAngle());
		out.writeInt(vehicleStatus.getBatteryStatus());
		out.writeInt(vehicleStatus.getGpsStatus());
		out.writeInt(vehicleStatus.getCurrWaypoint());
	}

	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public VehicleStatus createFromParcel(Parcel in) {
			return new VehicleStatus(in);
		}

		public Object[] newArray(int arg0) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

}
