/**WaypointInfo.java**********************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class object for waypoints.  Defines components of what make
 *      		  up waypoints.
 *    Call Path : WaypointList->WaypointInfo
 *    		XML :
 * Dependencies : 
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import android.os.Parcel;
import android.os.Parcelable;

public class WaypointInfo implements Parcelable {
	private String name;
	private double latitude;
	private double longitude;
	private double speedTo;
	private double altitude;
	private double holdTime;
	private double yawFrom;
	private double posAcc;
	private double panAngle;
	private double tiltAngle;

	public WaypointInfo(Parcel in) {
		readFromParcel(in);
	}

	public WaypointInfo() {
		this.name = "EmptyMarker";
		this.latitude = 0;
		this.longitude = 0;
		this.speedTo = 0;
		this.altitude = 0;
		this.holdTime = 0;
		this.yawFrom = 0;
		this.posAcc = 0;
		this.panAngle = 0;
		this.tiltAngle = 0;
	}

	// Typical Constructor for Ground Vehicle
	public WaypointInfo(String name, double latitude, double longitude,
			double speedTo) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speedTo = speedTo;
		this.altitude = 0;
		this.holdTime = 0;
		this.yawFrom = 0;
		this.posAcc = 0;
		this.panAngle = 0;
		this.tiltAngle = 0;
	}

	// Typical Constructor for Air Vehicle
	public WaypointInfo(String name, double latitude, double longitude,
			double speedTo, double altitude, double holdTime, double yawFrom, double posAcc, double panAngle,
			double tiltAngle) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speedTo = speedTo;
		this.altitude = altitude;
		this.holdTime = holdTime;
		this.yawFrom = yawFrom;
		this.posAcc = posAcc;
		this.panAngle = panAngle;
		this.tiltAngle = tiltAngle;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getSpeedTo() {
		return speedTo;
	}

	public void setSpeedTo(double speedTo) {
		this.speedTo = speedTo;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getHoldTime() {
		return holdTime;
	}

	public void setHoldTime(double holdTime) {
		this.holdTime = holdTime;
	}

	public double getPanAngle() {
		return panAngle;
	}

	public void setPanAngle(double panAngle) {
		this.panAngle = panAngle;
	}

	public double getTiltAngle() {
		return tiltAngle;
	}

	public void setTiltAngle(double tiltAngle) {
		this.tiltAngle = tiltAngle;
	}

	public double getYawFrom() {
		return yawFrom;
	}

	public void setYawFrom(double yawFrom) {
		this.yawFrom = yawFrom;
	}

	public double getPosAcc() {
		return posAcc;
	}

	public void setPosAcc(double posAcc) {
		this.posAcc = posAcc;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// Write each field into the parcel. They will be read back in same
		// order from the function readFromParcel
		dest.writeString(name);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeDouble(speedTo);
		dest.writeDouble(altitude);
		dest.writeDouble(holdTime);
		dest.writeDouble(yawFrom);
		dest.writeDouble(posAcc);
		dest.writeDouble(panAngle);
		dest.writeDouble(tiltAngle);
	}

	private void readFromParcel(Parcel in) {
		// Write back each field in the order that it was written to the parcel
		// from function writeToParcel
		name = in.readString();
		latitude = in.readDouble();
		longitude = in.readDouble();
		speedTo = in.readDouble();
		altitude = in.readDouble();
		holdTime = in.readDouble();
		yawFrom = in.readDouble();
		posAcc = in.readDouble();
		panAngle = in.readDouble();
		tiltAngle = in.readDouble();
	}
	
	public final Parcelable.Creator<WaypointInfo> CREATOR = new Parcelable.Creator<WaypointInfo>() {
        public WaypointInfo createFromParcel(Parcel in) {
            return new WaypointInfo(in);
        }

        public WaypointInfo[] newArray(int size) {
            return new WaypointInfo[size];
        }
    };
}
