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

import com.qinetiq.quadcontrol.fragments.MapFragment;
import com.qinetiq.quadcontrol.fragments.WaypointListFragment;

public class WaypointList extends ArrayList<WaypointInfo> {

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

	// Function that updates each class/object that uses WaypointList for
	// clearing all waypts
	public synchronized void updateClassesClear() {
		mapFragment.clearWaypoints();
		wayptListFragment.clearWaypoints();
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
	public void clear() {
		updateClassesClear();

		super.clear();
	}

	@Override
	public synchronized WaypointInfo remove(int wayptPos) {
		updateClassesRemove(wayptPos);

		return super.remove(wayptPos);
	}

	// public synchronized void getWaypoint(int wayptPos) {
	// this.get(wayptPos);
	// }
}
