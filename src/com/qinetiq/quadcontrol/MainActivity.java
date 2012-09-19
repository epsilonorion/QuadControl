/**MainActivity.java**********************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : MainActivity for application.  Starts PreferenceManager,
 *      		  creates WaypointList, creates Fragments, creates ViewPager,
 *      		  starts ROS.
 *    Call Path : BASE
 *          XML : res->layout->main
 * Dependencies : ViewPagerIndicator<L>, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import com.google.android.maps.MapActivity;
import com.qinetiq.quadcontrol.filechooser.FileChooserDialog;
import com.qinetiq.quadcontrol.fragments.CommandFragment;
import com.qinetiq.quadcontrol.fragments.MapFragment;
import com.qinetiq.quadcontrol.fragments.MediaFragment;
import com.qinetiq.quadcontrol.fragments.StatusFragment;
import com.qinetiq.quadcontrol.fragments.ViewFragmentAdapter;
import com.qinetiq.quadcontrol.fragments.WaypointListFragment;
import com.qinetiq.quadcontrol.preferences.PreferencesMenu;
import com.qinetiq.quadcontrol.ros.VehicleSubscriber;
import com.qinetiq.quadcontrol.ros.WaypointClient;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.support.v4.view.ViewPager;

public class MainActivity extends MapActivity implements DialogInterface.OnClickListener{
	private VehicleSubscriber vehSub;
	private WaypointClient wayptClient;
	VehicleStatus vehicleStatus;

	private NodeMainExecutor nodeMainExecutor;
	private NodeConfiguration nodeConfiguration;

	boolean maximizeMapFragFlag = false;
	boolean maximizeDataFragFlag = false;
	boolean maximizeVideoFragFlag = false;

	ViewFragmentAdapter mAdapter;
	ViewPager mPager;
	PageIndicator mIndicator;

	MainApplication mainApp;

	Menu menu;

	private static final int PICKFILE_RESULT_CODE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set screen to always stay on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Load default preference values if not performed before
		PreferenceManager.setDefaultValues(this, R.xml.general_preferences,
				true);
		PreferenceManager.setDefaultValues(this, R.xml.ros_preferences, true);
		PreferenceManager.setDefaultValues(this, R.xml.vehicle_preferences,
				true);

		// Grab preferences of the application
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Handle Theme Setup based on preference value.
		if (prefs.getString("theme_list", null).compareTo("Black Theme") == 0) {
			setTheme(R.style.AppThemeBlack);
		} else if (prefs.getString("theme_list", null).compareTo("White Theme") == 0) {
			setTheme(R.style.AppThemeLight);
		}
		setContentView(R.layout.main);

		// Create Objects to be used in various fragments and ROS Nodes
		WaypointList wayptList = new WaypointList();
		vehicleStatus = new VehicleStatus();

		// Setup Fragment Control for Application
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		mainApp = (MainApplication) getApplicationContext();
		mainApp.setWayptList(wayptList);
		mainApp.setVehicleStatus(vehicleStatus);

		// Replace Map and Video Fragments dynamically
		MapFragment mapFrag = new MapFragment();
		Bundle mapFragBundle = new Bundle();
		mapFragBundle.putParcelable("wayptList", wayptList);
		mapFrag.setArguments(mapFragBundle);
		transaction.replace(R.id.mapFragment, mapFrag);

		MediaFragment videoFrag = new MediaFragment();
		transaction.replace(R.id.mediaFragment, videoFrag);
		transaction.commit();

		// Add ViewPager and Related Fragments
		WaypointListFragment wayptListFrag = new WaypointListFragment();
		StatusFragment statusFrag = new StatusFragment();
		CommandFragment commandFrag = new CommandFragment();

		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(commandFrag);
		fragments.add(wayptListFrag);
		fragments.add(statusFrag);

		mainApp.setStatusFrag(statusFrag);

		mAdapter = new ViewFragmentAdapter(getFragmentManager(), fragments);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOffscreenPageLimit(4);
		mPager.setAdapter(mAdapter);

		mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);

		// Setup button listener for minmax button of each fragment
		addShowHideListener(R.id.btnMinMaxMapFragment, mapFrag, videoFrag);
		addShowHideListener(R.id.btnMinMaxDataFragment, mapFrag, videoFrag);
		addShowHideListener(R.id.btnMinMaxMediaFragment, mapFrag, videoFrag);

		// TODO Move to single class controlled by button connect
		// Handle ROS Connect
		nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory
				.newNonLoopback().getHostAddress());

		String hostMaster = prefs.getString("ros_IP", "");
		Integer port = Integer.parseInt(prefs.getString("ros_port", ""));
		URI uri = URI.create("http://" + hostMaster + ":" + port);
		Log.d("Josh", uri.toString());
		nodeConfiguration.setMasterUri(uri);

		nodeMainExecutor = DefaultNodeMainExecutor.newDefault();

		// wayptClient = new WaypointClient(wayptObject);

		// nodeMainExecutor.execute(vehSub, nodeConfiguration);
		// nodeMainExecutor.execute(wayptPub, nodeConfiguration);
		// nodeMainExecutor.execute(testService, nodeConfiguration);
	}

	// Handle inflation of Menu when menu button pressed
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		this.menu = menu;
		return true;
	}

	// Handle menuitem presses
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, PreferencesMenu.class));

			return (true);
		case R.id.load_mission:
			FileChooserDialog openFileDialog = FileChooserDialog.openInstance(this);
			openFileDialog.show(getFragmentManager(), "openFileDialogFragment");

			return (true);
		case R.id.save_mission:
			FileChooserDialog saveFileDialog = FileChooserDialog.saveInstance(this);
			saveFileDialog.show(getFragmentManager(), "saveFileDialogFragment");

			return (true);
		case R.id.playback_mission:
			FileChooserDialog playbackFileDialog = FileChooserDialog.saveInstance(this);
			playbackFileDialog.show(getFragmentManager(), "playbackFileDialogFragment");

			return (true);
		case R.id.vehicle_connect:

			return (true);
		}

		return (super.onOptionsItemSelected(item));
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	// Button Listener for maximizing and minimizing fragments
	void addShowHideListener(int buttonId, final Fragment mapFrag,
			final Fragment videoFrag) {
		final ImageButton button = (ImageButton) findViewById(buttonId);

		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();

				ImageButton button = (ImageButton) findViewById(v.getId());

				switch (v.getId()) {

				case R.id.btnMinMaxMapFragment:
					if (maximizeMapFragFlag) {
						// Minimize MapFragment
						button.setImageResource(R.drawable.maximizesmall);

						LinearLayout splitView = (LinearLayout) findViewById(R.id.splitFragmentContainer);
						splitView.setVisibility(View.VISIBLE);
						// ft.show(dataFrag);
						ft.show(videoFrag);

						maximizeMapFragFlag = false;
					} else {
						// Maximize MapFragment
						button.setImageResource(R.drawable.minimizesmall);

						// ft.hide(dataFrag);
						ft.hide(videoFrag);
						LinearLayout splitView = (LinearLayout) findViewById(R.id.splitFragmentContainer);
						splitView.setVisibility(View.GONE);

						maximizeMapFragFlag = true;
					}
					break;
				case R.id.btnMinMaxDataFragment:
					if (maximizeDataFragFlag) {
						// Minimize dataFrag
						button.setImageResource(R.drawable.maximizesmall);

						RelativeLayout mapView = (RelativeLayout) findViewById(R.id.mapFragmentContainer);
						mapView.setVisibility(View.VISIBLE);
						RelativeLayout videoView = (RelativeLayout) findViewById(R.id.mediaFragmentContainer);
						videoView.setVisibility(View.VISIBLE);
						ft.show(mapFrag);
						ft.show(videoFrag);
						maximizeDataFragFlag = false;
					} else {
						// Maximize dataFrag
						button.setImageResource(R.drawable.minimizesmall);

						RelativeLayout mapView = (RelativeLayout) findViewById(R.id.mapFragmentContainer);
						mapView.setVisibility(View.GONE);
						RelativeLayout videoView = (RelativeLayout) findViewById(R.id.mediaFragmentContainer);
						videoView.setVisibility(View.GONE);
						ft.hide(mapFrag);
						ft.hide(videoFrag);

						maximizeDataFragFlag = true;
					}
					break;
				case R.id.btnMinMaxMediaFragment:
					if (maximizeVideoFragFlag) {
						// Minimize dataFrag
						button.setImageResource(R.drawable.maximizesmall);

						RelativeLayout mapView = (RelativeLayout) findViewById(R.id.mapFragmentContainer);
						mapView.setVisibility(View.VISIBLE);
						LinearLayout dataView = (LinearLayout) findViewById(R.id.dataFragmentContainer);
						dataView.setVisibility(View.VISIBLE);
						ft.show(mapFrag);

						maximizeVideoFragFlag = false;
					} else {
						// Maximize dataFrag
						button.setImageResource(R.drawable.minimizesmall);

						RelativeLayout mapView = (RelativeLayout) findViewById(R.id.mapFragmentContainer);
						mapView.setVisibility(View.GONE);
						LinearLayout dataView = (LinearLayout) findViewById(R.id.dataFragmentContainer);
						dataView.setVisibility(View.GONE);
						ft.hide(mapFrag);

						maximizeVideoFragFlag = true;
					}
					break;
				}
				ft.commit();
			}
		});
	}

	public void updateVehicleConnect() {
		// MenuItem vehicleConnect = menu.getItem(R.id.vehicle_connect);
		// if (mainApp.isConnectedToVehicle()) {
		//
		// vehicleConnect.setTitle("AYYIYI");
		// vehicleConnect.setIcon(R.drawable.icon_connection_on);
		// } else {
		// vehicleConnect.setTitle("AYYIYI");
		// vehicleConnect.setIcon(R.drawable.icon_connection_on);
		// // MenuItem vehicleConnectItem = (MenuItem)
		// this.findViewById(R.id.vehicle_connect);
		// // vehicleConnectItem.setTitle("AYIYIYI");
		// // vehicleConnectItem.setIcon(R.drawable.icon_connection_off);
		// }
	}

	public Handler UIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.getData().getInt("VEHICLE_CONNECT")) {
			case 0: {
				updateVehicleConnect();
			}
				break;
			case 1: {
				updateVehicleConnect();
			}
				break;
			}
		};
	};

	@Override
	public void onClick(DialogInterface dialog, int which) {
		
	}

	public void getFilePath(String path) {
		Toast.makeText(this, "Path is " + path,
				Toast.LENGTH_SHORT).show();
	}
}

