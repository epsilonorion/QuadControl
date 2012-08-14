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

import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import com.google.android.maps.MapActivity;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.support.v4.view.ViewPager;

public class MainActivity extends MapActivity {
	//private VehicleSubscriber vehSub;
	//private WaypointPublisher wayptPub;
	//private ROSService testService;
	private ROSClient testClient;
	private NodeMainExecutor nodeMainExecutor;
	private NodeConfiguration nodeConfiguration;

	boolean maximizeMapFragFlag = false;
	boolean maximizeDataFragFlag = false;
	boolean maximizeVideoFragFlag = false;

	ViewFragmentAdapter mAdapter;
	ViewPager mPager;
	PageIndicator mIndicator;

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

		// Create WaypointList Object to be used in fragments and ROS Nodes
		WaypointList wayptObject = new WaypointList();

		// Setup Fragment Control for Application
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		// Add Map Fragment
		MapFragment mapFrag = new MapFragment();
		Bundle mapFragBundle = new Bundle();
		mapFragBundle.putParcelable("wayptObject", wayptObject);
		mapFrag.setArguments(mapFragBundle);
		transaction.replace(R.id.mapFragment, mapFrag);

		// Add Video Fragment
		MediaFragment videoFrag = new MediaFragment();
		transaction.replace(R.id.mediaFragment, videoFrag);
		transaction.commit();

		// Setup button listener for minmax button of each fragment
		addShowHideListener(R.id.btnMinMaxMapFragment, mapFrag, videoFrag);
		addShowHideListener(R.id.btnMinMaxDataFragment, mapFrag, videoFrag);
		addShowHideListener(R.id.btnMinMaxMediaFragment, mapFrag, videoFrag);

		// Add ViewPager and related Fragments
		WaypointEntryFragment wayptEntryFrag = new WaypointEntryFragment();
		
		Bundle wayptListBundle = new Bundle();
		wayptListBundle.putParcelable("wayptObject", wayptObject);
		WaypointListFragment wayptListFrag = new WaypointListFragment();
		wayptListFrag.setArguments(wayptListBundle);

		StatusFragment statusFrag = new StatusFragment();

		CommandFragment commandFrag = new CommandFragment();

		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(wayptEntryFrag);
		fragments.add(wayptListFrag);
		fragments.add(statusFrag);
		fragments.add(commandFrag);

		mAdapter = new ViewFragmentAdapter(getFragmentManager(), fragments);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);

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

//		vehSub = new VehicleSubscriber();
//		wayptPub = new WaypointPublisher();
//		testService = new ROSService();
		testClient = new ROSClient(wayptObject);

		// nodeMainExecutor.execute(vehSub, nodeConfiguration);
		// nodeMainExecutor.execute(wayptPub, nodeConfiguration);
		// nodeMainExecutor.execute(testService, nodeConfiguration);

	}

	// Handle inflation of Mene when menu button pressed
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Handle menuitem presses
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, PreferencesMenu.class));

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

						 nodeMainExecutor.shutdown();
					} else {
						// Maximize MapFragment
						button.setImageResource(R.drawable.minimizesmall);

						// ft.hide(dataFrag);
						ft.hide(videoFrag);
						LinearLayout splitView = (LinearLayout) findViewById(R.id.splitFragmentContainer);
						splitView.setVisibility(View.GONE);

						maximizeMapFragFlag = true;

						// vehSub = new VehicleSubscriber();
//						 wayptPub = new WaypointPublisher();
						// testService = new ROSNode();
						// nodeMainExecutor.execute(vehSub, nodeConfiguration);
//						 nodeMainExecutor.execute(wayptPub,
//						 nodeConfiguration);
						// nodeMainExecutor.execute(testService,
						// nodeConfiguration);
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
						 nodeMainExecutor.shutdownNodeMain(testClient);
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

						 nodeMainExecutor.execute(testClient,
						 nodeConfiguration);
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
						// ft.show(dataFrag);

						maximizeVideoFragFlag = false;
					} else {
						// Maximize dataFrag
						button.setImageResource(R.drawable.minimizesmall);

						RelativeLayout mapView = (RelativeLayout) findViewById(R.id.mapFragmentContainer);
						mapView.setVisibility(View.GONE);
						LinearLayout dataView = (LinearLayout) findViewById(R.id.dataFragmentContainer);
						dataView.setVisibility(View.GONE);
						ft.hide(mapFrag);
						// ft.hide(dataFrag);

						maximizeVideoFragFlag = true;
					}
					break;
				}
				ft.commit();
			}
		});
	}
}
