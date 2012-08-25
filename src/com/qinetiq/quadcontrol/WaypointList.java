/**WaypointList.java**********************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class object for collection of waypoint items into Array
 *      		  List.  Must be a Parcelable component for passing between
 *      		  fragments.
 *    Call Path : MainActivity->WaypointList
 *    		XML : 
 * Dependencies : WaypointInfo
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class WaypointList extends ArrayList<WaypointInfo> implements Parcelable {

	private static final long serialVersionUID = 1L;
	MapFragment mapFragment = null;
	WaypointListFragment wayptListFragment = null;

	public WaypointList() {

	}	
	
	public synchronized void setupMapFragment(MapFragment mapFragment) {
		this.mapFragment = mapFragment;
	}

	public synchronized void setupWayptListFragment(
			WaypointListFragment wayptListFragment) {
		this.wayptListFragment = wayptListFragment;
	}

	// Function that updates each class/object that uses WaypointList for adding
	// Waypt
	public synchronized void updateClassesAdd(WaypointInfo waypt) {
		mapFragment.addWaypoint(waypt);
		wayptListFragment.addWaypoint(waypt);
	}

	// Function that updates each class/object that uses WaypointList for
	// modifying Waypt
	public synchronized void updateClassesModify(int wayptPos,
			WaypointInfo waypt) {
		mapFragment.modifyWaypoint(wayptPos, waypt);
		wayptListFragment.modifyWaypoint(wayptPos, waypt);
	}

	// Function that updates each class/object that uses WaypointList for
	// deleting Waypt
	public synchronized void updateClassesRemove(int wayptPos) {
		mapFragment.removeWaypoint(wayptPos);
		wayptListFragment.removeWaypoint(wayptPos);
	}

	@Override
	public synchronized boolean add(WaypointInfo waypt) {
		updateClassesAdd(waypt);
		
		return super.add(waypt);
	}

	public synchronized boolean modify(int wayptPos, WaypointInfo waypt) {
		this.set(wayptPos, waypt);

		updateClassesModify(wayptPos, waypt);
		
		return true;
	}
	
	@Override
	public synchronized WaypointInfo remove(int wayptPos) {
		updateClassesRemove(wayptPos);
		
		return super.remove(wayptPos);
	}

	public synchronized void getWaypoint(int wayptPos) {
		this.get(wayptPos);
	}

	// PARCEL Commands
	public WaypointList(Parcel in) {
		this.clear();

		// Read list size
		int size = in.readInt();

		// Now read each item, order is dependent on way written in under
		// "writeToParcel"
		for (int i = 0; i < size; i++) {

			WaypointInfo waypt = new WaypointInfo();

			waypt.setName(in.readString());
			waypt.setLatitude(in.readDouble());
			waypt.setLongitude(in.readDouble());
			waypt.setSpeedTo(in.readDouble());
			waypt.setAltitude(in.readDouble());
			waypt.setHoldTime(in.readDouble());
			waypt.setPanAngle(in.readDouble());
			waypt.setTiltAngle(in.readDouble());

			this.add(waypt);
		}
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		// Write size of list
		int size = this.size();
		out.writeInt(size);

		// Writing name, then latitude, then longitude.
		for (int i = 0; i < size; i++) {

			WaypointInfo waypt = this.get(i);

			out.writeString(waypt.getName());
			out.writeDouble(waypt.getLatitude());
			out.writeDouble(waypt.getLongitude());
			out.writeDouble(waypt.getSpeedTo());
			out.writeDouble(waypt.getAltitude());
			out.writeDouble(waypt.getHoldTime());
			out.writeDouble(waypt.getPanAngle());
			out.writeDouble(waypt.getTiltAngle());
		}

	}
	
	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public WaypointList createFromParcel(Parcel in) {
			return new WaypointList(in);
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
