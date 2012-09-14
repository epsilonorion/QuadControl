/**CommandFragment.java*******************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 26, 2012
 *      Purpose : Class for controlling the Command Fragment.  Control
 *      		  involves receiving button presses from a user and sending
 *       		  these commands to a connected vehicle through ROS
 *    Call Path : MainActivity->CommandFragment
 * 			XML : res->layout->command_fragment
 * Dependencies : ViewFragmentAdapter, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol.fragments;

import java.net.URI;

import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import com.qinetiq.quadcontrol.MainApplication;
import com.qinetiq.quadcontrol.R;
import com.qinetiq.quadcontrol.StatusInfo;
import com.qinetiq.quadcontrol.VehicleStatus;
import com.qinetiq.quadcontrol.WaypointList;
import com.qinetiq.quadcontrol.ros.CommandClient;
import com.qinetiq.quadcontrol.ros.VehicleSubscriber;
import com.qinetiq.quadcontrol.ros.WaypointClient;
import com.qinetiq.quadcontrol.util.Utils;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class CommandFragment extends Fragment {
	private VehicleStatus vehicleStatusObject = null;
	private WaypointList wayptListObject = null;

	boolean ConnectedToVehicle = false;

	private CommandClient commandClient;
	private VehicleSubscriber vehSub;
	private WaypointClient wayptClient;

	private NodeMainExecutor nodeMainExecutor;
	private NodeConfiguration nodeConfiguration;

	private MainApplication mainApp;

	private CommandFragment commandFragInstance;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.command_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		mainApp = (MainApplication)getActivity().getApplicationContext();
		wayptListObject = mainApp.getWayptList();
		vehicleStatusObject = mainApp.getVehicleStatus();
        
		commandFragInstance = this;
		
		// Capture our button from layout
		Button btnConnecToVehicle = (Button) getActivity().findViewById(
				R.id.btnConnectVehicle);
		Button btnSendWaypts = (Button) getActivity().findViewById(
				R.id.btnSendWaypoints);
		Button btnStartMission = (Button) getActivity().findViewById(
				R.id.btnStartMission);
		Button btnPauseMission = (Button) getActivity().findViewById(
				R.id.btnPauseMission);
		Button btnHaltMission = (Button) getActivity().findViewById(
				R.id.btnHaltMission);
		Button btnReturnToBase = (Button) getActivity().findViewById(
				R.id.btnReturnToBase);

		btnConnecToVehicle.setOnClickListener(mAddListener);
		btnSendWaypts.setOnClickListener(mAddListener);
		btnStartMission.setOnClickListener(mAddListener);
		btnPauseMission.setOnClickListener(mAddListener);
		btnHaltMission.setOnClickListener(mAddListener);
		btnReturnToBase.setOnClickListener(mAddListener);
	}

	// Create an anonymous implementation of OnClickListener
	private OnClickListener mAddListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.btnConnectVehicle:
				if (!mainApp.isConnectedToVehicle()) {
					// Handle ROS Connect

					// Grab preferences of the application
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(getActivity());

					// Configure ROS Node connection with host ip and port
					// addresses from preferences menu
					// Grab nodeMainExecutor and nodeConfiguration from global
					// set.
					mainApp = (MainApplication) getActivity()
							.getApplicationContext();
					nodeMainExecutor = mainApp.getNodeMainExecutor();
					nodeConfiguration = mainApp.getNodeConfiguration();

					nodeConfiguration = NodeConfiguration
							.newPublic(InetAddressFactory.newNonLoopback()
									.getHostAddress());

					String hostMaster = prefs.getString("ros_IP", "");
					Integer port = Integer.parseInt(prefs.getString("ros_port",
							""));
					URI uri = URI.create("http://" + hostMaster + ":" + port);
					nodeConfiguration.setMasterUri(uri);

					Log.d("Test", "hostMaster is " + hostMaster);
					Log.d("Test", "uri is " + uri);
					nodeMainExecutor = DefaultNodeMainExecutor.newDefault();

					// Set global variables to be used elsewhere.
					mainApp.setNodeMainExecutor(nodeMainExecutor);
					mainApp.setNodeConfiguration(nodeConfiguration);

//					StatusFragment statusFragment = (StatusFragment) getFragmentManager()
//							.findFragmentByTag(
//									"android:switcher:" + R.id.pager + ":2");

					StatusFragment statusFragment = mainApp.getStatusFrag();
					
					// Create nodes
					vehSub = new VehicleSubscriber(vehicleStatusObject,
							statusFragment, commandFragInstance, getActivity().getApplicationContext());
					// wayptPub = new WaypointPublisher();
					// testService = new ROSService();
					wayptClient = new WaypointClient(wayptListObject, getActivity().getApplicationContext());

					// Execute Nodes
					nodeMainExecutor.execute(vehSub, nodeConfiguration);

					// Give message and change button context
					Toast.makeText(getActivity(), "Connecting to Vehicle",
							Toast.LENGTH_SHORT).show();

					Button btnConnectToVehicle = (Button) getActivity()
							.findViewById(R.id.btnConnectVehicle);
					btnConnectToVehicle.setText("Connecting...");
				} else {
					// Shutdown Nodes
					nodeMainExecutor.shutdown();

					// Give message and change button context
					Toast.makeText(getActivity(), "Disconnected from  Vehicle",
							Toast.LENGTH_SHORT).show();
				}

				break;

			case R.id.btnSendWaypoints:
				if (mainApp.isConnectedToVehicle()) {
					nodeMainExecutor.execute(wayptClient, nodeConfiguration);

					Toast.makeText(getActivity(), "Sending Waypoints",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.btnStartMission:
				if (mainApp.isConnectedToVehicle()) {
					commandClient = new CommandClient(0, getActivity());
					Utils.setContext(getActivity());

					nodeMainExecutor.execute(commandClient, nodeConfiguration);

//					Toast.makeText(getActivity(), "Starting Mission",
//							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.btnPauseMission:
				if (mainApp.isConnectedToVehicle()) {
					commandClient = new CommandClient(1, getActivity());

					nodeMainExecutor.execute(commandClient, nodeConfiguration);

					Toast.makeText(getActivity(), "Pausing Mission",
							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}

				break;

			case R.id.btnHaltMission:
				if (mainApp.isConnectedToVehicle()) {
					commandClient = new CommandClient(2, getActivity());

					nodeMainExecutor.execute(commandClient, nodeConfiguration);

					Toast.makeText(getActivity(), "Halting Mission",
							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}

				break;
			case R.id.btnReturnToBase:
				if (mainApp.isConnectedToVehicle()) {
					commandClient = new CommandClient(3, getActivity());

					nodeMainExecutor.execute(commandClient, nodeConfiguration);

					Toast.makeText(getActivity(), "Returning to Base",
							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}

				break;

			}

		}
	};
	
	public Handler UIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.getData().getInt("VEHICLE_CONNECT")) {
			case 0: {
				Button btnConnectToVehicle = (Button) getActivity()
						.findViewById(R.id.btnConnectVehicle);
				btnConnectToVehicle.setText("Connect To Vehicle");
			}
				break;
			case 1: {
				Button btnConnectToVehicle = (Button) getActivity()
						.findViewById(R.id.btnConnectVehicle);
				btnConnectToVehicle.setText("Disconnect From Vehicle");
			}
				break;
			}
		};
	};
}
