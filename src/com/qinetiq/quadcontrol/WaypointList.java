/**WaypointList.java**********************************************************
 *       Author : Joshua Weaver
 * Last Revised : Sept 9, 2012
 *      Purpose : Class object for collection of waypoint items into Array
 *      		  List.
 *    Call Path : MainActivity->WaypointList
 *    		XML : 
 * Dependencies : WaypointInfo
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.qinetiq.quadcontrol.fragments.MapFragment;
import com.qinetiq.quadcontrol.fragments.WaypointListFragment;

public class WaypointList extends ArrayList<WaypointInfo> implements Parcelable {

	private static final long serialVersionUID = 1L;
	MapFragment mapFragment = null;
	WaypointListFragment wayptListFragment = null;

	public WaypointList() {

	}

	public WaypointList(Parcel in) {
		readFromParcel(in);
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
		Log.d("Test", "JOSH2");
		
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

	// Function that updates each class/object that uses WaypointList for
	// clearing all waypts
	public synchronized void updateClassesClear() {
		mapFragment.clearWaypoints();
		wayptListFragment.clearWaypoints();
	}

	@Override
	public synchronized boolean add(WaypointInfo waypt) {
		Log.d("Test", "JOSH1");
		updateClassesAdd(waypt);

		return super.add(waypt);
	}

	public synchronized boolean modify(int wayptPos, WaypointInfo waypt) {
		this.set(wayptPos, waypt);

		updateClassesModify(wayptPos, waypt);

		return true;
	}

	@Override
	public void clear() {
		updateClassesClear();

		super.clear();
	}

	@Override
	public synchronized WaypointInfo remove(int wayptPos) {
		updateClassesRemove(wayptPos);

		return super.remove(wayptPos);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		// Write size of list
		int size = this.size();
		out.writeInt(size);

		// Writing values of
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
			out.writeDouble(waypt.getYawFrom());
			out.writeDouble(waypt.getPosAcc());
		}
	}

	private void readFromParcel(Parcel in) {
		// Write back each field in the order that it was written to the parcel
		// from function writeToParcel
		this.clear();

		// First we have to read the list size
		int size = in.readInt();

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
			waypt.setYawFrom(in.readDouble());
			waypt.setPosAcc(in.readDouble());

			this.add(waypt);
		}
	}
	
	public final Parcelable.Creator<WaypointList> CREATOR = new Parcelable.Creator<WaypointList>() {
        public WaypointList createFromParcel(Parcel in) {
            return new WaypointList(in);
        }

        public WaypointList[] newArray(int size) {
            return new WaypointList[size];
        }
    };

}
