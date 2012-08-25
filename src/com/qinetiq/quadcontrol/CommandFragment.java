/**CommandFragment.java*******************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class for controlling the Command Fragment.  Control
 *      		  involves receiving button presses from a user and sending
 *       		  these commands to a connected vehicle through ROS
 *    Call Path : MainActivity->CommandFragment
 * 			XML : res->layout->command_fragment
 * Dependencies : ViewFragmentAdapter, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import java.net.URI;

import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.command_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Pull objects out of bundle
		Bundle bundle = getArguments();
		if (bundle != null) {
			vehicleStatusObject = bundle.getParcelable("vehicleStatusObject");
		}
		if (bundle != null) {
			wayptListObject = bundle.getParcelable("wayptObject");
		}

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
				if (!ConnectedToVehicle) {
					// Handle ROS Connect

					// Grab preferences of the application
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(getActivity());

					// Configure ROS Node connection with host ip and port
					// addresses from preferences menu
					nodeConfiguration = NodeConfiguration
							.newPublic(InetAddressFactory.newNonLoopback()
									.getHostAddress());

					String hostMaster = prefs.getString("ros_IP", "");
					Integer port = Integer.parseInt(prefs.getString("ros_port",
							""));
					URI uri = URI.create("http://" + hostMaster + ":" + port);
					Log.d("Josh", uri.toString());
					nodeConfiguration.setMasterUri(uri);

					nodeMainExecutor = DefaultNodeMainExecutor.newDefault();

					StatusFragment statusFragment = (StatusFragment) getFragmentManager()
							.findFragmentByTag("android:switcher:" + R.id.pager + ":3");
					
					// Create nodes
					vehSub = new VehicleSubscriber(vehicleStatusObject, statusFragment);
					// wayptPub = new WaypointPublisher();
					// testService = new ROSService();
					wayptClient = new WaypointClient(wayptListObject);

					// Execute Nodes
					nodeMainExecutor.execute(vehSub, nodeConfiguration);

					// Give message and change button context
					Toast.makeText(getActivity(), "Connecting to Vehicle",
							Toast.LENGTH_SHORT).show();

					Button btnConnectToVehicle = (Button) getActivity()
							.findViewById(R.id.btnConnectVehicle);
					btnConnectToVehicle.setText("Disconnect From Vehicle");

					ConnectedToVehicle = true;
				} else {
					// Shutdown Nodes
					nodeMainExecutor.shutdown();

					// Give message and change button context
					Toast.makeText(getActivity(), "Disconnected from  Vehicle",
							Toast.LENGTH_SHORT).show();

					Button btnConnectToVehicle = (Button) getActivity()
							.findViewById(R.id.btnConnectVehicle);
					btnConnectToVehicle.setText("Connect To Vehicle");

					ConnectedToVehicle = false;
				}

				break;

			case R.id.btnSendWaypoints:
				if (ConnectedToVehicle) {
					nodeMainExecutor.execute(wayptClient, nodeConfiguration);
					nodeMainExecutor.shutdownNodeMain(wayptClient);

					Toast.makeText(getActivity(), "Sending Waypoints",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.btnStartMission:
				if (ConnectedToVehicle) {
					commandClient = new CommandClient(0);

					nodeMainExecutor.execute(commandClient, nodeConfiguration);

					Toast.makeText(getActivity(), "Starting Mission",
							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.btnPauseMission:
				if (ConnectedToVehicle) {
					commandClient = new CommandClient(1);

					nodeMainExecutor.execute(commandClient, nodeConfiguration);

					Toast.makeText(getActivity(), "Pausing Mission",
							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}

				break;

			case R.id.btnHaltMission:
				if (ConnectedToVehicle) {
					commandClient = new CommandClient(2);

					nodeMainExecutor.execute(commandClient, nodeConfiguration);
					
					Toast.makeText(getActivity(), "Halting Mission",
							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(getActivity(), "Vehicle Not Connected",
							Toast.LENGTH_SHORT).show();
				}

				break;
			case R.id.btnReturnToBase:
				if (ConnectedToVehicle) {
					commandClient = new CommandClient(3);

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
}
