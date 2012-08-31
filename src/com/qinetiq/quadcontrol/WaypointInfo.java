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

public class WaypointInfo {
	private String name;
	private double latitude;
	private double longitude;
	private double speedTo;
	private double altitude;
	private double holdTime;
	private double panAngle;
	private double tiltAngle;
	private double yawFrom;

	public WaypointInfo() {
		this.name = "EmptyMarker";
		this.latitude = 0;
		this.longitude = 0;
		this.speedTo = 0;
		this.altitude = 0;
		this.holdTime = 0;
		this.panAngle = 0;
		this.tiltAngle = 0;
		this.yawFrom = 0;
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
		this.panAngle = 0;
		this.tiltAngle = 0;
		this.yawFrom = 0;
	}

	// Typical Constructor for Air Vehicle
	public WaypointInfo(String name, double latitude, double longitude,
			double speedTo, double altitude, double holdTime, double panAngle,
			double tiltAngle, double yawFrom) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speedTo = speedTo;
		this.altitude = altitude;
		this.holdTime = holdTime;
		this.panAngle = panAngle;
		this.tiltAngle = tiltAngle;
		this.yawFrom = yawFrom;
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
}
