/**MapFragment.java***********************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Fragment for controlling the MapView.  Uses various
 *      		  overlays to handle GPS Position, waypoint drawing, track
 *      		  generation, vehicle display, etc.  It also holds the object
 *      		  for the SlidingDrawer and the subsequent buttons.  Depends
 *      		  heavily on the MainActivity extending MapActivity!!!!!!
 *    Call Path : MainActivity->MapFragment
 *          XML : res->layout->map_fragment
 * Dependencies : SlidingDrawerWrapper, WaypointsOverlay
 ****************************************************************************/

package com.qinetiq.quadcontrol.fragments;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.qinetiq.quadcontrol.MainApplication;
import com.qinetiq.quadcontrol.R;
import com.qinetiq.quadcontrol.RouteOverlay;
import com.qinetiq.quadcontrol.VehicleOverlay;
import com.qinetiq.quadcontrol.VehicleStatus;
import com.qinetiq.quadcontrol.WaypointInfo;
import com.qinetiq.quadcontrol.WaypointList;
import com.qinetiq.quadcontrol.WaypointsOverlay;
import com.qinetiq.quadcontrol.util.SlidingDrawerWrapper;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MapFragment extends Fragment implements OnDrawerOpenListener,
		OnDrawerCloseListener {
	private MapView mapView = null;
	private MapController mc = null;

	private WaypointList wayptList = null;
	private VehicleStatus statusInfo = null;
	
	private MyLocationOverlay myLocOverlay = null;
	private WaypointsOverlay waypointsOverlay = null;
	private VehicleOverlay vehicleOverlay = null;

	boolean satelliteOn = true;
	boolean maximizedOn = false;

	private SlidingDrawerWrapper sd;

	ToggleButton addButton;
	ToggleButton deleteButton;
	ToggleButton moveButton;
	ToggleButton modifyButton;
	Button clearButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.map_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		MainApplication mainApp = (MainApplication) getActivity()
				.getApplicationContext();
		wayptList = mainApp.getWayptList();
		statusInfo = mainApp.getVehicleStatus();

		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.mapFragment);

		wayptList.setupMapFragment(mapFragment);

		mapView = (MapView) getActivity().findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(false);
		mc = mapView.getController();
		mc.setZoom(20);
		mapView.setSatellite(satelliteOn);
		List<Overlay> overlays = mapView.getOverlays();

		// Add vehicle overlay with R.id.quad drawable
		vehicleOverlay = new VehicleOverlay(statusInfo, getActivity(),
				R.drawable.icon_air_vehicle);
		overlays.add(vehicleOverlay);

		// Add a Waypoint Overlay with marker to MapView
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		int markerWidth = marker.getIntrinsicWidth();
		int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(0, markerHeight, markerWidth, 0);

		ImageView dragImage = (ImageView) getActivity().findViewById(R.id.drag);

		// Grab handle for WaypointsListFragment
		// WaypointListFragment wayptListFragment = (WaypointListFragment)
		// getFragmentManager()
		// .findFragmentById(R.id.waypointListFragment);
		waypointsOverlay = new WaypointsOverlay(marker, dragImage,
				getActivity(), wayptList);
		overlays.add(waypointsOverlay);

		// Add Mylocation Overlay to MapView
		myLocOverlay = new MyLocationOverlay(getActivity(), mapView);
		myLocOverlay.enableMyLocation();
		myLocOverlay.enableCompass();
		overlays.add(myLocOverlay);

		// Add Route Overlay to MapView
		RouteOverlay routeOverlay = new RouteOverlay(wayptList);
		overlays.add(routeOverlay);
		mapView.postInvalidate();

		// Capture our button from layout
		addButton = (ToggleButton) getActivity().findViewById(R.id.btnAdd);
		deleteButton = (ToggleButton) getActivity()
				.findViewById(R.id.btnDelete);
		moveButton = (ToggleButton) getActivity().findViewById(R.id.btnMove);
		modifyButton = (ToggleButton) getActivity()
				.findViewById(R.id.btnModify);
		clearButton = (Button) getActivity().findViewById(R.id.btnClear);

		addButton.setOnCheckedChangeListener(mCheckedListener);
		deleteButton.setOnCheckedChangeListener(mCheckedListener);
		moveButton.setOnCheckedChangeListener(mCheckedListener);
		modifyButton.setOnCheckedChangeListener(mCheckedListener);
		clearButton.setOnClickListener(mClickListener);

		sd = (SlidingDrawerWrapper) getActivity().findViewById(R.id.sg_below);
		sd.setOnDrawerOpenListener(this);
		sd.setOnDrawerCloseListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	// ---creating action items on the action bar for a fragment---
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.map_fragment_menu, menu);
	}

	// ---when a menu item is selected---
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// return MenuChoice(item);
		// ---obtain an instance of the activity---
		switch (item.getItemId()) {

		case R.id.SatelliteViewItem:
			Toast.makeText(getActivity(), "Satellite View turned on",
					Toast.LENGTH_SHORT).show();
			mapView.setSatellite(true);
			satelliteOn = true;
			// }

			return true;

		case R.id.MapViewItem:
			Toast.makeText(getActivity(), "Map View turned on",
					Toast.LENGTH_SHORT).show();
			mapView.setSatellite(false);
			satelliteOn = false;

			return true;

		case R.id.mylocation:
			GeoPoint myLoc = myLocOverlay.getMyLocation();
			mc.animateTo(myLoc);
			return true;
		}
		return false;
	}

	private OnCheckedChangeListener mCheckedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (buttonView == addButton) {

				if (isChecked) {
					deleteButton.setChecked(false);
					moveButton.setChecked(false);
					modifyButton.setChecked(false);

					waypointsOverlay.setAddWayptFlag(true);
					waypointsOverlay.setDeleteWayptFlag(false);
					waypointsOverlay.setMoveWayptFlag(false);
					waypointsOverlay.setModifyWayptFlag(false);
				} else {
					waypointsOverlay.setAddWayptFlag(false);
					waypointsOverlay.setDeleteWayptFlag(false);
					waypointsOverlay.setMoveWayptFlag(false);
					waypointsOverlay.setModifyWayptFlag(false);
				}

			}

			if (buttonView == deleteButton) {

				if (isChecked) {
					addButton.setChecked(false);
					moveButton.setChecked(false);
					modifyButton.setChecked(false);

					waypointsOverlay.setAddWayptFlag(false);
					waypointsOverlay.setDeleteWayptFlag(true);
					waypointsOverlay.setMoveWayptFlag(false);
					waypointsOverlay.setModifyWayptFlag(false);
				} else {
					waypointsOverlay.setAddWayptFlag(false);
					waypointsOverlay.setDeleteWayptFlag(false);
					waypointsOverlay.setMoveWayptFlag(false);
					waypointsOverlay.setModifyWayptFlag(false);
				}
			}

			if (buttonView == moveButton) {
				if (isChecked) {
					addButton.setChecked(false);
					deleteButton.setChecked(false);
					modifyButton.setChecked(false);

					waypointsOverlay.setAddWayptFlag(false);
					waypointsOverlay.setDeleteWayptFlag(false);
					waypointsOverlay.setMoveWayptFlag(true);
					waypointsOverlay.setModifyWayptFlag(false);
				} else {
					waypointsOverlay.setAddWayptFlag(false);
					waypointsOverlay.setDeleteWayptFlag(false);
					waypointsOverlay.setMoveWayptFlag(false);
					waypointsOverlay.setModifyWayptFlag(false);
				}

			}

			if (buttonView == modifyButton) {
				if (isChecked) {
					addButton.setChecked(false);
					deleteButton.setChecked(false);
					moveButton.setChecked(false);

					waypointsOverlay.setAddWayptFlag(false);
					waypointsOverlay.setDeleteWayptFlag(false);
					waypointsOverlay.setMoveWayptFlag(false);
					waypointsOverlay.setModifyWayptFlag(true);
				} else {
					waypointsOverlay.setAddWayptFlag(false);
					waypointsOverlay.setDeleteWayptFlag(false);
					waypointsOverlay.setMoveWayptFlag(false);
					waypointsOverlay.setModifyWayptFlag(false);
				}
			}

		}

	};

	// Create an anonymous implementation of OnClickListener
	private OnClickListener mClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.btnClear:
				wayptList.clear();
				break;
			}
		}
	};

	public void gotoLocation(String latlng) {
		// ---the location is represented as "lat,lng"---
		String[] coordinates = latlng.split(",");
		double lat = Double.parseDouble(coordinates[0]);
		double lng = Double.parseDouble(coordinates[1]);
		GeoPoint p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		mc.animateTo(p);
		mc.setZoom(14);

	}

	public void removeWaypoint(int wayptPos) {
		waypointsOverlay.removeItem(wayptPos);
	}

	public void addWaypoint(WaypointInfo waypt) {
		waypointsOverlay.addItem(waypt);
	}

	public void modifyWaypoint(int wayptPos, WaypointInfo waypt) {
		waypointsOverlay.modifyItem(wayptPos, waypt);
	}
	
	public void clearWaypoints() {
		waypointsOverlay.clearItems();
		
		mapView.postInvalidate();
	}

	@Override
	public void onResume() {
		super.onResume();

		myLocOverlay.enableCompass();
		myLocOverlay.enableMyLocation();
	}

	@Override
	public void onPause() {
		super.onPause();

		myLocOverlay.disableCompass();
		myLocOverlay.disableMyLocation();
	}

	public void onDrawerClosed() {
		sd.getHandle().setBackgroundResource(R.drawable.icon_up);

		Toast.makeText(getActivity(), "Finished Editing Markers",
				Toast.LENGTH_SHORT).show();

		addButton.setChecked(false);
		deleteButton.setChecked(false);
		moveButton.setChecked(false);
		modifyButton.setChecked(false);

		waypointsOverlay.setAddWayptFlag(false);
		waypointsOverlay.setDeleteWayptFlag(false);
		waypointsOverlay.setMoveWayptFlag(false);
		waypointsOverlay.setModifyWayptFlag(false);
	}

	public void onDrawerOpened() {
		sd.getHandle().setBackgroundResource(R.drawable.icon_down);

		Toast.makeText(getActivity(), "Editing Markers", Toast.LENGTH_SHORT)
				.show();
	}
}
