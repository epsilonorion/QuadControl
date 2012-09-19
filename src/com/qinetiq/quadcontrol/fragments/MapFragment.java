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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MapFragment extends Fragment implements OnDrawerOpenListener,
		OnDrawerCloseListener {
	private View mView;

	// MapView Variables
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

	private ToggleButton addButton;
	private ToggleButton deleteButton;
	private ToggleButton moveButton;
	private ToggleButton modifyButton;
	private Button newButton;
	private Button clearButton;

	private SharedPreferences prefs;

	private static final int DEFAULT_ZOOM = 20;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.map_fragment, container, false);
		
//		Bundle bundle = getArguments();
//		if (bundle != null) {
//			wayptList = bundle.getParcelable("wayptList");
//		}

		MainApplication mainApp = (MainApplication) getActivity()
				.getApplicationContext();
		// wayptList = mainApp.getWayptList();
		statusInfo = mainApp.getVehicleStatus();
		wayptList = mainApp.getWayptList();
		
		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.mapFragment);

		wayptList.setupMapFragment(mapFragment);

		// Grab preferences of the application
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		mapView = (MapView) mView.findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(false);
		mc = mapView.getController();
		mc.setZoom(DEFAULT_ZOOM);
		mapView.setSatellite(satelliteOn);
		List<Overlay> overlays = mapView.getOverlays();

		// Add vehicle overlay with R.id.quad drawable
		vehicleOverlay = new VehicleOverlay(statusInfo, getActivity(),
				R.drawable.icon_vehicle);
		overlays.add(vehicleOverlay);

		// Add a Waypoint Overlay with marker to MapView
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		int markerWidth = marker.getIntrinsicWidth();
		int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(0, markerHeight, markerWidth, 0);

		ImageView dragImage = (ImageView) mView.findViewById(R.id.drag);

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

		// Capture our button from mView layout
		addButton = (ToggleButton) mView.findViewById(R.id.btnAdd);
		deleteButton = (ToggleButton) mView
				.findViewById(R.id.btnDelete);
		moveButton = (ToggleButton) mView.findViewById(R.id.btnMove);
		modifyButton = (ToggleButton) mView
				.findViewById(R.id.btnModify);
		newButton = (Button) mView.findViewById(R.id.btnNew);
		clearButton = (Button) mView.findViewById(R.id.btnClear);

		addButton.setOnCheckedChangeListener(mCheckedListener);
		deleteButton.setOnCheckedChangeListener(mCheckedListener);
		moveButton.setOnCheckedChangeListener(mCheckedListener);
		modifyButton.setOnCheckedChangeListener(mCheckedListener);
		newButton.setOnClickListener(mClickListener);
		clearButton.setOnClickListener(mClickListener);

		sd = (SlidingDrawerWrapper) mView.findViewById(R.id.sg_below);
		sd.setOnDrawerOpenListener(this);
		sd.setOnDrawerCloseListener(this);
		
		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();
		
		Log.d("Test", "List size " + wayptList.size());
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
			if (myLoc != null) {
				mc.animateTo(myLoc);
			} else {
				Toast.makeText(getActivity(),
						"Current Position is not detected", Toast.LENGTH_LONG)
						.show();
			}
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

			case R.id.btnNew:
				OpenDialog();
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
		Log.d("Test", "JOSH3");
		waypointsOverlay.addItem(waypt);
	}

	public void modifyWaypoint(int wayptPos, WaypointInfo waypt) {
		waypointsOverlay.modifyItem(wayptPos, waypt);
	}

	public void clearWaypoints() {
		waypointsOverlay.clearItems();

		mapView.invalidate();
	}

	@Override
	public void onResume() {
		super.onResume();

		mapView.invalidate();
		
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

	private void OpenDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle("New Waypoint Info");

		// Set an EditText view to get user input
		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater
				.inflate(R.layout.waypoint_entry_dialog, null);

		alert.setView(view);

		// Populate all text boxes with default values
		// Use Current Position as default Lat/Long coordinates
		double myLocLat = 0.0;
		double myLocLong = 0.0;

		GeoPoint myLoc = myLocOverlay.getMyLocation();
		if (myLoc != null) {
			myLocLat = myLoc.getLatitudeE6() / 1E6;
			myLocLong = myLoc.getLongitudeE6() / 1E6;
		}

		final double defaultLatitude = myLocLat;
		final double defaultLongitude = myLocLong;

		// Use preference values as default values for other components
		final double defaultSpeed = Double.parseDouble(prefs.getString(
				"default_speed", ""));
		final double defaultAltitude = Double.parseDouble(prefs.getString(
				"default_altitude", ""));
		final double defaultHoldTime = Double.parseDouble(prefs.getString(
				"default_hold_time", ""));
		final double defaultPanAngle = Double.parseDouble(prefs.getString(
				"default_pan_position", ""));
		final double defaultTiltAngle = Double.parseDouble(prefs.getString(
				"default_tilt_position", ""));
		final double defaultHeading = Double.parseDouble(prefs.getString(
				"default_yaw_from", ""));
		final double defaultPosAcc = Double.parseDouble(prefs.getString(
				"default_position_accuracy", ""));

		final EditText wayptNameTxt = (EditText) view
				.findViewById(R.id.txtWaypointName);
		final EditText latitudeTxt = (EditText) view
				.findViewById(R.id.txtLatitude);
		final EditText longitudeTxt = (EditText) view
				.findViewById(R.id.txtLongitude);
		final EditText speedToTxt = (EditText) view
				.findViewById(R.id.txtSpeedTo);
		final EditText holdTimeTxt = (EditText) view
				.findViewById(R.id.txtHoldTime);
		final EditText altitudeTxt = (EditText) view
				.findViewById(R.id.txtAltitude);
		final EditText headingTxt = (EditText) view
				.findViewById(R.id.txtDesiredHeading);
		final EditText panAngleTxt = (EditText) view
				.findViewById(R.id.txtPanAngle);
		final EditText tiltAngleTxt = (EditText) view
				.findViewById(R.id.txtTiltAngle);
		final EditText posAccTxt = (EditText) view
				.findViewById(R.id.txtPosAccuracy);

		wayptNameTxt.setText("Waypoint");
		latitudeTxt.setText("" + defaultLatitude);
		longitudeTxt.setText("" + defaultLongitude);
		speedToTxt.setText("" + defaultSpeed);
		holdTimeTxt.setText("" + defaultHoldTime);
		altitudeTxt.setText("" + defaultAltitude);
		headingTxt.setText("" + defaultHeading);
		panAngleTxt.setText("" + defaultPanAngle);
		tiltAngleTxt.setText("" + defaultTiltAngle);
		posAccTxt.setText("" + defaultPosAcc);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String wayptNameStr = wayptNameTxt.getText().toString();
				String latitudeStr = latitudeTxt.getText().toString();
				String longitudeStr = longitudeTxt.getText().toString();
				String speedToStr = speedToTxt.getText().toString();
				String holdTimeStr = holdTimeTxt.getText().toString();
				String altitudeStr = altitudeTxt.getText().toString();
				String headingStr = headingTxt.getText().toString();
				String panAngleStr = panAngleTxt.getText().toString();
				String tiltAngleStr = tiltAngleTxt.getText().toString();
				String posAccStr = posAccTxt.getText().toString();

				double latitude, longitude, speedTo, holdTime, altitude, heading;
				double panAngle, tiltAngle, posAcc;

				try {
					latitude = Double.parseDouble(latitudeStr);
				} catch (final NumberFormatException e) {
					latitude = 0.0;
				}

				try {
					longitude = Double.parseDouble(longitudeStr);
				} catch (final NumberFormatException e) {
					longitude = 0.0;
				}

				try {
					speedTo = Double.parseDouble(speedToStr);
				} catch (final NumberFormatException e) {
					speedTo = defaultSpeed;
				}

				try {
					holdTime = Double.parseDouble(holdTimeStr);
				} catch (final NumberFormatException e) {
					holdTime = defaultHoldTime;
				}

				try {
					altitude = Double.parseDouble(altitudeStr);
				} catch (final NumberFormatException e) {
					altitude = defaultAltitude;
				}

				try {
					heading = Double.parseDouble(headingStr);
				} catch (final NumberFormatException e) {
					heading = defaultHeading;
				}

				try {
					panAngle = Double.parseDouble(panAngleStr);
				} catch (final NumberFormatException e) {
					panAngle = defaultPanAngle;
				}

				try {
					tiltAngle = Double.parseDouble(tiltAngleStr);
				} catch (final NumberFormatException e) {
					tiltAngle = defaultTiltAngle;
				}

				try {
					posAcc = Double.parseDouble(posAccStr);
				} catch (final NumberFormatException e) {
					posAcc = defaultPosAcc;
				}

				WaypointInfo waypt = new WaypointInfo(wayptNameStr, latitude,
						longitude, speedTo, altitude, holdTime, panAngle,
						tiltAngle, heading, posAcc);

				// Add waypoint to waypoint list object
				wayptList.add(waypt);
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}
}
