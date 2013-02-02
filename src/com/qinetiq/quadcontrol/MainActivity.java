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
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import com.qinetiq.quadcontrol.util.CSVWriter;
import com.qinetiq.quadcontrol.util.CSVReader;
import com.google.android.maps.MapActivity;
import com.qinetiq.quadcontrol.filechooser.FileChooserDialog;
import com.qinetiq.quadcontrol.filechooser.FileChooserDialog.FileChooserDialogListener;
import com.qinetiq.quadcontrol.fragments.CommandFragment;
import com.qinetiq.quadcontrol.fragments.MapFragment;
import com.qinetiq.quadcontrol.fragments.MediaFragment;
import com.qinetiq.quadcontrol.fragments.StatusFragment;
import com.qinetiq.quadcontrol.fragments.ViewFragmentAdapter;
import com.qinetiq.quadcontrol.fragments.WaypointListFragment;
import com.qinetiq.quadcontrol.preferences.PreferencesMenu;
import com.qinetiq.quadcontrol.ros.ROSVehicleBridge;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.view.ViewPager;

public class MainActivity extends MapActivity implements
		DialogInterface.OnClickListener, FileChooserDialogListener,
		OnSeekBarChangeListener {
	WaypointList wayptList;
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
	MenuItem testItem;

	// File file;
	// String filePath;
	// String fileName;

	private AnimationDrawable connectAnimation;
	private ImageView connectImage;

	private MapFragment mapFrag;
	private StatusFragment statusFrag;
	private MediaFragment videoFrag;
	private CommandFragment commandFrag;

	private playbackMission playbackMissionTask;

	private static final String BASE_DIRECTORY = "sdcard/QuadControl";
	private static final String MISSION_DIRECTORY = "sdcard/QuadControl/mission";
	private static final String PLAYBACK_DIRECTORY = "sdcard/QuadControl/playback";
	private static final String MISSION_FILE_EXTENSION = "mission";
	private static final String LOG_FILE_EXTENSION = "log";

	SeekBar playbackSeekBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Make sure base directory for application has been created
		File file = new File(BASE_DIRECTORY);
		file.mkdirs();
		// Make sure mission and playback folders have been created
		file = new File(MISSION_DIRECTORY);
		file.mkdirs();
		file = new File(PLAYBACK_DIRECTORY);
		file.mkdirs();

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
		wayptList = new WaypointList();
		vehicleStatus = new VehicleStatus();

		// Setup Fragment Control for Application
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		mainApp = (MainApplication) getApplicationContext();
		mainApp.setWayptList(wayptList);
		mainApp.setVehicleStatus(vehicleStatus);

		// Replace Map and Video Fragments dynamically
		mapFrag = MapFragment.newInstance(wayptList);
		// mapFrag = new MapFragment();
		// Bundle mapFragBundle = new Bundle();
		// mapFragBundle.putParcelable("wayptList", wayptList);
		// mapFrag.setArguments(mapFragBundle);
		transaction.replace(R.id.mapFragment, mapFrag);

		// videoFrag = new MediaFragment();
		videoFrag = MediaFragment.newInstance();
		transaction.replace(R.id.mediaFragment, videoFrag);
		transaction.commit();

		// Add ViewPager and Related Fragments
		WaypointListFragment wayptListFrag = new WaypointListFragment();
		statusFrag = new StatusFragment();
		commandFrag = new CommandFragment();

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

		ImageButton playButton = (ImageButton) findViewById(R.id.btnPlaybackPlay);
		ImageButton stopButton = (ImageButton) findViewById(R.id.btnPlaybackStop);
		ImageButton fasterButton = (ImageButton) findViewById(R.id.btnPlaybackFaster);
		ImageButton slowerButton = (ImageButton) findViewById(R.id.btnPlaybackSlower);

		playButton.setOnClickListener(mClickListener);
		stopButton.setOnClickListener(mClickListener);
		fasterButton.setOnClickListener(mClickListener);
		slowerButton.setOnClickListener(mClickListener);

		playbackSeekBar = (SeekBar) findViewById(R.id.seekBar);
		playbackSeekBar.setOnSeekBarChangeListener(this);
	}

	// Handle inflation of Menu when menu button pressed
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		View v = (View) menu.findItem(R.id.vehicle_connect).getActionView();

		MenuItem connectItem = menu.findItem(R.id.vehicle_connect);
		testItem = connectItem;

		// LayoutInflater inflater = (LayoutInflater)
		// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View v = inflater.inflate(R.layout.connection_layout, null);
		// connectImage = (ImageView) v.findViewById(R.id.connectImage);
		connectImage = (ImageView) v.findViewById(R.id.connectImage);

		if (mainApp.isConnectedToVehicle()) {
			connectImage.setImageResource(R.drawable.icon_connected);
		} else {
			connectImage.setImageResource(R.drawable.icon_disconnected);
		}
		connectImage.setBackgroundResource(R.drawable.connection_animation);
		connectAnimation = (AnimationDrawable) connectImage.getBackground();

		connectImage.setOnClickListener(mClickListener);
		// connectItem.setActionView(connectImage);

		this.menu = menu;
		return true;
	}

	// Handle menuitem presses
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Bundle args;

		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, PreferencesMenu.class));

			return (true);
		case R.id.load_mission:
			args = new Bundle();
			args.putString("startDirectory", MISSION_DIRECTORY);
			args.putString("dialogType", "open");
			args.putString("fileType", MISSION_FILE_EXTENSION);
			args.putString("title", "Choose mission file to open");

			FileChooserDialog openFileDialog = FileChooserDialog.newInstance(
					this, args);
			openFileDialog.show(getFragmentManager(), "openFileDialogFragment");

			return (true);
		case R.id.save_mission:
			if (wayptList.size() == 0) { // No waypoints to save
				Toast.makeText(this, "There is no mission to save!",
						Toast.LENGTH_SHORT).show();
			} else {
				args = new Bundle();
				args.putString("startDirectory", MISSION_DIRECTORY);
				args.putString("dialogType", "save");
				args.putString("fileType", MISSION_FILE_EXTENSION);
				args.putString("title",
						"Choose folder and name for mission file to save");

				FileChooserDialog saveFileDialog = FileChooserDialog
						.newInstance(this, args);
				saveFileDialog.show(getFragmentManager(),
						"saveFileDialogFragment");
			}
			return (true);
		case R.id.playback_mission:
			mainApp.setInPlayback(true);

			args = new Bundle();
			args.putString("startDirectory", PLAYBACK_DIRECTORY);
			args.putString("dialogType", "open");
			args.putString("fileType", LOG_FILE_EXTENSION);
			args.putString("title", "Choose log playback file to open");

			FileChooserDialog playbackFileDialog = FileChooserDialog
					.newInstance(this, args);
			playbackFileDialog.show(getFragmentManager(),
					"playbackFileDialogFragment");

			return (true);
		case R.id.swap_map_video:
			Log.d("Test", "Swapping");
			FragmentManager fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();

			Fragment f1 = fm.findFragmentById(R.id.mapFragment);
			Fragment f2 = fm.findFragmentById(R.id.mediaFragment);

			ft.remove(f1);
			ft.remove(f2);
			ft.commit();
			fm.executePendingTransactions();

			ft = fm.beginTransaction();
			// ft.replace(R.id.mapFragment, videoFrag);
			ft.add(R.id.mediaFragment, f1);
			ft.add(R.id.mapFragment, f2);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

			ft.commit();

			return (true);
		case R.id.vehicle_connect:

			return (true);
		}

		return (super.onOptionsItemSelected(item));
	}

	public Handler UIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.getData().getInt("VEHICLE_CONNECT")) {
			case 1: {
				connectAnimation.stop();
				connectImage.setBackgroundDrawable(null);
				connectImage.setImageResource(R.drawable.icon_disconnected);

				Toast.makeText(MainActivity.this, "Disconnected from Vehicle",
						Toast.LENGTH_SHORT).show();
			}
				break;
			case 2: {
				connectAnimation.stop();
				connectImage.setBackgroundDrawable(null);
				connectImage.setImageResource(R.drawable.icon_connected);

				Toast.makeText(MainActivity.this, "Connected to Vehicle",
						Toast.LENGTH_SHORT).show();
			}
				break;
			}
		};
	};

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			playbackMissionTask.setIndex(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

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

	private OnClickListener mClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.btnPlaybackPlay:
				if (playbackMissionTask != null) {
					if (playbackMissionTask.isRunning()) {
						if (playbackMissionTask.getIsPaused()) {
							playbackMissionTask.setIsPaused(false);

							ImageButton btn = (ImageButton) v
									.findViewById(R.id.btnPlaybackPlay);
							btn.setImageResource(R.drawable.icon_pause);
						} else {
							playbackMissionTask.setIsPaused(true);

							ImageButton btn = (ImageButton) v
									.findViewById(R.id.btnPlaybackPlay);
							btn.setImageResource(R.drawable.icon_play);
						}
					}
				}

				break;

			case R.id.btnPlaybackStop:
				if (playbackMissionTask != null) {
					if (playbackMissionTask.isRunning()) {
						playbackMissionTask.exitPlayback();
					}
				}
				break;

			case R.id.btnPlaybackFaster:
				if (playbackMissionTask != null) {
					if (playbackMissionTask.isRunning()) {
						playbackMissionTask.incSpeed();

						TextView lblMultiplier = (TextView) findViewById(R.id.lblMultiplier);

						lblMultiplier.setText("x"
								+ playbackMissionTask.getSpeed());
					}
				}
				break;

			case R.id.btnPlaybackSlower:
				if (playbackMissionTask != null) {
					if (playbackMissionTask.isRunning()) {
						playbackMissionTask.decSpeed();

						TextView lblMultiplier = (TextView) findViewById(R.id.lblMultiplier);

						lblMultiplier.setText("x"
								+ playbackMissionTask.getSpeed());
					}
				}
				break;

			case R.id.connectImage:
				if (!mainApp.isConnectedToVehicle()) {
					connectImage.setImageDrawable(null);
					connectImage
							.setBackgroundResource(R.drawable.connection_animation);
					connectAnimation = (AnimationDrawable) connectImage
							.getBackground();

					testItem.setActionView(connectImage);
					connectAnimation.start();

					// Handle ROS Connect

					// Grab preferences of the application
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(MainActivity.this);

					// Configure ROS Node connection with host ip and port
					// addresses from preferences menu
					// Grab nodeMainExecutor and nodeConfiguration from global
					// set.
					mainApp = (MainApplication) getApplicationContext();
					nodeMainExecutor = mainApp.getNodeMainExecutor();
					nodeConfiguration = mainApp.getNodeConfiguration();

					nodeConfiguration = NodeConfiguration
							.newPublic(InetAddressFactory.newNonLoopback()
									.getHostAddress());

					String hostMaster = prefs.getString("ros_IP", "");
					Integer port = Integer.parseInt(prefs.getString("ros_port",
							""));
					URI uri = URI.create("http://" + hostMaster + ":" + port);

					Log.d("Test", "Master URI is " + uri);
					nodeConfiguration.setMasterUri(uri);
					nodeMainExecutor = DefaultNodeMainExecutor.newDefault();

					// Set global variables to be used elsewhere.
					mainApp.setNodeMainExecutor(nodeMainExecutor);
					mainApp.setNodeConfiguration(nodeConfiguration);

					// Create nodes
					ROSVehicleBridge rosVehBridge = new ROSVehicleBridge(
							vehicleStatus, wayptList, statusFrag, commandFrag,
							getApplicationContext(), MainActivity.this);

					// Execute Nodes
					nodeMainExecutor.execute(rosVehBridge, nodeConfiguration);

					mainApp.setNodeMainExecutor(nodeMainExecutor);

					// Give message and change button context
					Toast.makeText(MainActivity.this, "Connecting to Vehicle",
							Toast.LENGTH_LONG).show();

					mainApp.setROSVehicleBridge(rosVehBridge);
				} else {
					connectImage.setImageDrawable(null);
					connectImage
							.setBackgroundResource(R.drawable.connection_animation);
					connectAnimation = (AnimationDrawable) connectImage
							.getBackground();

					testItem.setActionView(connectImage);
					connectAnimation.start();

					// Shutdown Node
					// TODO Move to Async Task to remove UI Stop when
					// Disconnecting.
					mainApp = (MainApplication) getApplicationContext();
					nodeMainExecutor = mainApp.getNodeMainExecutor();
					nodeMainExecutor.shutdown();

					mainApp.setROSVehicleBridge(null);

					// Give message and change button context
					Toast.makeText(MainActivity.this,
							"Disconnecting from  Vehicle", Toast.LENGTH_LONG)
							.show();
				}
				break;

			}
		}
	};

	@Override
	public void onClick(DialogInterface dialog, int which) {

	}

	public void getFilePath(String path) {
		Toast.makeText(this, "Path is " + path, Toast.LENGTH_SHORT).show();
	}

	private void saveMission(File fileName) throws Exception {
		// WaypointList should have already been checked for values
		CSVWriter writer = new CSVWriter(new FileWriter(fileName));

		// Fields in Waypoint Info. Check WaypointInfo for matching fields and
		// header

		// Initialize Fields with the header. Current Is 11 items:
		// Number, Name, Latitude, Longitude, speedTo, altitude, holdTime,
		// panAngle, tiltAngle, yawFrom, posAcc
		String[] fields = new String[] { "Number", "Latitude",
				"Longitude", "speedTo", "altitude", "holdTime", "yawFrom",
				"posAcc", "panAngle", "tiltAngle" };

		writer.writeNext(fields);

		// Write each waypoint field
		WaypointInfo tempWaypt;

		for (int i = 0; i < wayptList.size(); i++) {
			tempWaypt = wayptList.get(i);

			fields[0] = Integer.toString(i);
			fields[1] = Double.toString(tempWaypt.getLatitude() * 1e7);
			fields[2] = Double.toString(tempWaypt.getLongitude() * 1e7);
			fields[3] = Double.toString(tempWaypt.getSpeedTo());
			fields[4] = Double.toString(tempWaypt.getAltitude());
			fields[5] = Double.toString(tempWaypt.getHoldTime());
			fields[6] = Double.toString(tempWaypt.getYawFrom() * 1000);
			fields[7] = Double.toString(tempWaypt.getPosAcc());
			fields[8] = Double.toString(tempWaypt.getPanAngle() * 1000);
			fields[9] = Double.toString(tempWaypt.getTiltAngle() * 1000);

			writer.writeNext(fields);
		}

		writer.close();
	}

	private void loadMission(File fileName) throws Exception {
		String next[] = {};
		List<String[]> list = new ArrayList<String[]>();

		// Clear current wayptList
		wayptList.clear();

		CSVReader reader = new CSVReader(new FileReader(fileName));

		// Throw away header row
		reader.readNext();

		// Read data to list to be parsed later
		for (;;) {
			next = reader.readNext();
			if (next != null) {
				list.add(next);
			} else {
				break;
			}
		}

		// Parse list into waypoints and add to waypoint list.
		// This structure follows that of WaypointInfo, which has 11 items
		WaypointInfo tempWaypt = null;

		for (int i = 0; i < list.size(); i++) {
			tempWaypt = new WaypointInfo();
			// Ignore list.get(i)[0] which is the waypoint number
			tempWaypt.setName("Waypoint");
			tempWaypt.setLatitude(Double.parseDouble(list.get(i)[1]) / 1e7);
			tempWaypt.setLongitude(Double.parseDouble(list.get(i)[2]) / 1e7);
			tempWaypt.setSpeedTo(Double.parseDouble(list.get(i)[3]));
			tempWaypt.setAltitude(Double.parseDouble(list.get(i)[4]));
			tempWaypt.setHoldTime(Double.parseDouble(list.get(i)[5]));
			tempWaypt.setYawFrom(Double.parseDouble(list.get(i)[6]) / 1000);
			tempWaypt.setPosAcc(Double.parseDouble(list.get(i)[7]));
			tempWaypt.setPanAngle(Double.parseDouble(list.get(i)[8]) / 1000);
			tempWaypt.setTiltAngle(Double.parseDouble(list.get(i)[9]) / 1000);

			wayptList.add(tempWaypt);
		}

		reader.close();
	}

	private void playbackMission(File fileName) throws Exception {
		String next[] = {};
		List<String[]> list = new ArrayList<String[]>();

		// If playback File chosen, hide map slider and show playback controls
		RelativeLayout playbackControls = (RelativeLayout) findViewById(R.id.playbackControls);
		playbackControls.setVisibility(View.VISIBLE);

		mapFrag.hideSlidingDrawer();
		videoFrag.showPlaybackView();

		CSVReader reader = new CSVReader(new FileReader(fileName));

		// Throw away header row
		reader.readNext();

		// Read data to list to be parsed later
		for (;;) {
			next = reader.readNext();
			if (next != null) {
				list.add(next);
			} else {
				break;
			}
		}

		reader.close();

		// Kill current playback if one is already running
		if (playbackMissionTask != null) {
			if (playbackMissionTask.isRunning()) {
				playbackMissionTask.exitPlayback();
			}
		}

		// Set SeekBar max value
		playbackSeekBar.setMax(list.size() - 1);

		playbackMissionTask = new playbackMission();
		playbackMissionTask.execute(list);
	}

	@Override
	public void onFinishFileChooserDialog(String type, String filePath,
			String fileName) {
		Log.d("TEST", "FilePath = " + filePath + " FileName = " + fileName);

		if (type.equals("open")) {
			try {
				// File to open is
				File file = new File(filePath + "/" + fileName);
				Log.d("Test", "File to Open is " + file.toString());

				if (file.toString().endsWith(MISSION_FILE_EXTENSION)) {
					Toast.makeText(this, "Mission loaded!", Toast.LENGTH_SHORT)
							.show();
					loadMission(file);
				} else if (file.toString().endsWith(LOG_FILE_EXTENSION)) {
					Toast.makeText(this, "Log loaded!", Toast.LENGTH_SHORT)
							.show();
					// Grab mission file that exists in Log Folder
					String missionFileName = fileName.substring(0,
							fileName.lastIndexOf('.'))
							+ "." + MISSION_FILE_EXTENSION;
					File missionFile = new File(filePath + "/"
							+ missionFileName);
					
					if (missionFile.exists()) {
						loadMission(missionFile);
					} else {
						Toast.makeText(this, "No Mission File to Load!", Toast.LENGTH_SHORT)
						.show();
					}
					
					playbackMission(file);
				}
			} catch (Exception e1) {
				Toast.makeText(this, "Unable to Load File!", Toast.LENGTH_SHORT)
						.show();
				e1.printStackTrace();
			}
		} else if (type.equals("save")) {
			try {
				// Check that path has been created. If not, make it
				File file = new File(filePath);
				file.mkdirs();

				// Add .mission extension to end of filename
				file = new File(filePath + "/" + fileName + ".mission");
				Log.d("Test", "File to Save is " + file.toString());

				saveMission(file);
				Toast.makeText(this, "Mission saved!", Toast.LENGTH_SHORT)
						.show();
			} catch (Exception e) {
				Toast.makeText(this, "Mission was unable to save!",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	}

	private class playbackMission extends
			AsyncTask<List<String[]>, Object, Void> {
		private static final double MAX_SPEED = 4;
		private static final double MIN_SPEED = 1 / MAX_SPEED;

		private int index = 0;
		private int lastIndex = 0;
		private double speed = 1;

		private Boolean isRunning = false;
		private Boolean isPaused = false;

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(List<String[]>... param) {
			List<String[]> logList = param[0];

			lastIndex = logList.size() - 1;
			isRunning = true;
			Log.d("Test", "Last index is " + lastIndex);

			// Setup
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			while (isRunning) {
				if (!isPaused) {
					if (index <= lastIndex - 1) {
						StatusInfo tempStatus = setStatusInfo(logList, index);
						String fileName = getImageName(logList, index);

						publishProgress(tempStatus, fileName);

						String timeStr1 = logList.get(index)[1];
						String timeStr2 = logList.get(index + 1)[1];

						double t1 = Double.parseDouble(timeStr1.replaceAll(
								"\\D+", ""));
						double t2 = Double.parseDouble(timeStr2.replaceAll(
								"\\D+", ""));

						// Time is calculated to be 1/100th of a second, so
						// multiply
						// by 10 to put in ms.
						double sleepTime = (t2 - t1) * 10;

						try {
							Thread.sleep((long) (sleepTime / speed));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						StatusInfo tempStatus = setStatusInfo(logList,
								lastIndex);
						String fileName = getImageName(logList, lastIndex);

						publishProgress(tempStatus, fileName);

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						// Hold until index is changed
						while (index == lastIndex) {
						}
					}

					index++;

					if (index > lastIndex)
						index = lastIndex;
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			return null;
		}

		private StatusInfo setStatusInfo(List<String[]> list, int pos) {
			StatusInfo tempStatus = new StatusInfo();

			// Current Layout follows Index, TimeStamp, ImageFileName,
			// VehicleName, Latitude, Longitude, Heading, Speed, Altitude,
			// PanAngle, TiltAngle, BatteryStatus, GPSStatus, CurrentWaypoint
			tempStatus.setVehicleName(list.get(pos)[3]);
			tempStatus.setLatitude(Double.parseDouble(list.get(pos)[4]) / 1e7);
			tempStatus.setLongitude(Double.parseDouble(list.get(pos)[5]) / 1e7);
			tempStatus
					.setHeading(Double.parseDouble(list.get(pos)[6]) / 1000.0);
			tempStatus.setSpeed(Double.parseDouble(list.get(pos)[7]));
			tempStatus.setAltitude(Double.parseDouble(list.get(pos)[8]));
			tempStatus.setPanAngle(Double.parseDouble(list.get(pos)[9]));
			tempStatus.setTiltAngle(Double.parseDouble(list.get(pos)[10]));
			tempStatus
					.setBatteryStatus(Double.parseDouble(list.get(pos)[11]) / 1000.0);
			tempStatus.setGpsStatus(Integer.parseInt(list.get(pos)[12]));
			tempStatus.setCurrWaypoint(Integer.parseInt(list.get(pos)[13]));

			return tempStatus;
		}

		private String getImageName(List<String[]> list, int pos) {
			String[] fileNameSplit = list.get(pos)[2].split("/");

			String fileName = fileNameSplit[fileNameSplit.length - 2] + "/"
					+ fileNameSplit[fileNameSplit.length - 1];

			return fileName;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public double getSpeed() {
			return speed;
		}

		public void incSpeed() {
			double tempSpeed = speed * 2;

			if (tempSpeed > MAX_SPEED)
				tempSpeed = MAX_SPEED;

			this.speed = tempSpeed;
		}

		public void decSpeed() {
			double tempSpeed = speed / 2;

			if (tempSpeed < MIN_SPEED)
				tempSpeed = MIN_SPEED;

			this.speed = tempSpeed;
		}

		public Boolean isRunning() {
			return isRunning;
		}

		public void exitPlayback() {
			this.isRunning = false;
		}

		public Boolean getIsPaused() {
			return isPaused;
		}

		public void setIsPaused(Boolean isPaused) {
			this.isPaused = isPaused;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Hide playback controls and show map controls
			RelativeLayout playbackControls = (RelativeLayout) findViewById(R.id.playbackControls);
			playbackControls.setVisibility(View.GONE);

			mapFrag.showSlidingDrawer();
		}

		@Override
		protected void onProgressUpdate(Object... objects) {
			VehicleStatus vehicleStatus = mainApp.getVehicleStatus();

			vehicleStatus.setVehicleStatus((StatusInfo) objects[0]);

			mapFrag.update();
			// Send message to UI Handler under the Status Fragment
			// to update UI.
			if (statusFrag != null) {
				Handler uiHandler = statusFrag.UIHandler;

				Bundle b = new Bundle();
				b.putInt("VEHICLE_STATUS", 1);
				Message msg = Message.obtain(uiHandler);
				msg.setData(b);
				msg.sendToTarget();
			}

			if (videoFrag != null) {
				Handler uiHandler = videoFrag.UIHandler;

				Bundle b = new Bundle();
				b.putInt("PLAYBACK_IMAGE", 1);
				b.putString("IMAGE_PATH", objects[1].toString());
				Message msg = Message.obtain(uiHandler);
				msg.setData(b);
				msg.sendToTarget();
			}

			playbackSeekBar.setProgress(index);
		}
	}

}
