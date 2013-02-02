package com.qinetiq.quadcontrol.ros;

import java.util.List;

import org.ros.concurrent.CancellableLoop;
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import com.google.common.collect.Lists;
import com.qinetiq.quadcontrol.MainActivity;
import com.qinetiq.quadcontrol.MainApplication;
import com.qinetiq.quadcontrol.StatusInfo;
import com.qinetiq.quadcontrol.VehicleStatus;
import com.qinetiq.quadcontrol.WaypointInfo;
import com.qinetiq.quadcontrol.WaypointList;
import com.qinetiq.quadcontrol.fragments.CommandFragment;
import com.qinetiq.quadcontrol.fragments.StatusFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class ROSVehicleBridge implements NodeMain {
	public static final int START_COMMAND = 0;
	public static final int PAUSE_COMMAND = 1;
	public static final int HALT_COMMAND = 2;
	public static final int RTB_COMMAND = 3;
	public static final int LAUNCH_COMMAND = 4;
	public static final int LAND_COMMAND = 5;

	private VehicleStatus vehicleStatus;
	private WaypointList wayptList;
	private StatusFragment statusFragment;
	private CommandFragment commandFragment;
	private Context context;
	private MainActivity main;

	private MainApplication mainApp;

	private ConnectedNode connectedNode;
	private boolean nodeStarted = false;

	Subscriber<mission_msgs.VehicleStatus> subscriber = null;
	private ServiceClient<mission_msgs.SetMissionRequest, mission_msgs.SetMissionResponse> waypointClient = null;
	private ServiceClient<mission_msgs.ControlMissionRequest, mission_msgs.ControlMissionResponse> commandClient = null;

	int maxConnectAttempts = 1;

	public ROSVehicleBridge(VehicleStatus vehicleStatus,
			WaypointList wayptList, StatusFragment statusFragment,
			CommandFragment commandFragment, Context context, MainActivity main) {
		Log.d("ClassCreate", "ROSVehicleBridge");

		this.vehicleStatus = vehicleStatus;
		this.wayptList = wayptList;
		this.statusFragment = statusFragment;
		this.commandFragment = commandFragment;
		this.context = context;
		this.main = main;

		mainApp = (MainApplication) context.getApplicationContext();

		// Grab preferences of the application
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mainApp);

		// Handle Theme Setup based on preference value.
		maxConnectAttempts = Integer.parseInt(prefs.getString("client_timeout",
				""));
	}

	@Override
	public void onError(Node node, Throwable throwable) {
		Log.d("FunctionCall", "ROSVehicleBridge onError");

		node.shutdown();
	}

	@Override
	public void onShutdown(Node node) {
		Log.d("FunctionCall", "ROSVehicleBridge onShutdown");

		if (main != null) {
			Handler uiHandler = main.UIHandler;

			Bundle b = new Bundle();
			b.putInt("VEHICLE_CONNECT", 1);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();
		}

		mainApp.setConnectedToVehicle(false);
	}

	@Override
	public void onShutdownComplete(Node node) {
		Log.d("FunctionCall", "ROSVehicleBridge onShutdownComplete");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		Log.d("FunctionCall", "ROSVehicleBridge onStart");

		this.connectedNode = connectedNode;

		// Create Subscriber to vehicle status and populate vehicle status data
		// and fragment
		try {
			subscriber = connectedNode
					.newSubscriber("VehicleStatus",
							mission_msgs.VehicleStatus._TYPE);
		} catch (Exception e) {
			Log.d("Test", "Create statusSubscriber exception");
			e.printStackTrace();
		}

		if (subscriber != null) {
			subscriber
			.addMessageListener(new MessageListener<mission_msgs.VehicleStatus>() {
				@Override
				public void onNewMessage(
						mission_msgs.VehicleStatus message) {
					StatusInfo tempStatus = new StatusInfo();

					tempStatus.setVehicleName(message.getVehicleName());
					tempStatus.setLatitude(message.getLatitude() / 1e7);
					tempStatus.setLongitude(message.getLongitude() / 1e7);
					tempStatus.setHeading(message.getHeading() / 1000.0);
					tempStatus.setSpeed(message.getSpeed());
					tempStatus.setAltitude(message.getAltitude());
					tempStatus.setPanAngle(message.getPanAngle());
					tempStatus.setTiltAngle(message.getTiltAngle());
					tempStatus.setBatteryStatus(message
							.getBatteryStatus() / 1000.0);
					tempStatus.setGpsStatus(message.getGpsStatus());
					tempStatus.setCurrWaypoint(message
							.getCurrWaypoint());
					tempStatus.setState(message.getState());

					vehicleStatus.setVehicleStatus(tempStatus);

					// Send message to UI Handler under the Status
					// Fragment
					// to update UI.
					if (statusFragment != null) {
						Handler uiHandler = statusFragment.UIHandler;

						Bundle b = new Bundle();
						b.putInt("VEHICLE_STATUS", 1);
						Message msg = Message.obtain(uiHandler);
						msg.setData(b);
						msg.sendToTarget();
					}

				}
			});
		}
		// Try to create waypointClient
		int connectAttempt = 0;

		do {
			try {
				waypointClient = connectedNode.newServiceClient("SetMission",
						mission_msgs.SetMission._TYPE);

				break;
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Test", "Create waypointClient exception");

				connectAttempt++;
			}
		} while (connectAttempt <= maxConnectAttempts);

		// Unable to connected to waypointClient, shutdown node
		if (waypointClient == null) {
			Handler uiHandler = commandFragment.UIHandler;

			Bundle b = new Bundle();
			b.putInt("WAYPOINT_SENT", 0);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();

			connectedNode.shutdown();
		}

		Log.d("Test", "waypointClient created");

		// Try to create CommandClient
		connectAttempt = 0;

		do {
			try {
				commandClient = connectedNode.newServiceClient(
						"ControlMission", mission_msgs.ControlMission._TYPE);

				break;
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Test", "Create commandClient exception");
				connectAttempt++;
			}
		} while (connectAttempt <= maxConnectAttempts);

		// Unable to connected to waypointClient, shutdown node
		if (commandClient == null) {
			Handler uiHandler = commandFragment.UIHandler;

			Bundle b = new Bundle();
			b.putInt("COMMAND_SENT", 0);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();

			connectedNode.shutdown();
		}

		Log.d("Test", "commandClient created");

		// Set variables to show vehicles is connected.
		mainApp.setConnectedToVehicle(true);

		if (main != null) {
			Handler uiHandler = main.UIHandler;

			Bundle b = new Bundle();
			b.putInt("VEHICLE_CONNECT", 2);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();
		}
	}

	// Function to send Waypoints. Called from Command Fragment
	public void sendWaypoints() {
		// Grab Message types for Waypoint and WaypointList
		final Publisher<mission_msgs.WaypointList> publisher = connectedNode
				.newPublisher("WaypointList", mission_msgs.WaypointList._TYPE);

		final Publisher<mission_msgs.Waypoint> wayptPublisher = connectedNode
				.newPublisher("Waypoint", mission_msgs.Waypoint._TYPE);

		mission_msgs.WaypointList waypointList = publisher.newMessage();
		List<mission_msgs.Waypoint> waypoints = Lists.newArrayList();

		WaypointInfo point;
		mission_msgs.Waypoint waypt;

		// Populate waypoint list message
		for (int i = 0; i < wayptList.size(); i++) {
			point = wayptList.get(i);

			waypt = wayptPublisher.newMessage();
			waypt.setLatitude((int) (point.getLatitude() * 1e7));
			waypt.setLongitude((int) (point.getLongitude() * 1e7));
			waypt.setSpeedTo((int) point.getSpeedTo());
			waypt.setAltitude((int) point.getAltitude());
			waypt.setHoldTime((short) point.getHoldTime());
			waypt.setYawFrom((int) (point.getYawFrom() * 1000));
			waypt.setPosAcc((int) point.getPosAcc());
			waypt.setPanAngle((int) (point.getPanAngle() * 1000));
			waypt.setTiltAngle((int) (point.getTiltAngle() * 1000));

			waypoints.add(waypt);
		}

		waypointList.setNumWaypts(waypoints.size());
		waypointList.setPoints(waypoints);

		// Create WaypointClient Message for request
		final mission_msgs.SetMissionRequest request = waypointClient
				.newMessage();

		request.setList(waypointList);

		// Send Client Request and wait for response
		waypointClient.call(request,
				new ServiceResponseListener<mission_msgs.SetMissionResponse>() {
					@Override
					public void onSuccess(
							mission_msgs.SetMissionResponse response) {
						connectedNode.getLog().info(
								String.format(
										"Returned number of waypoints %d",
										response.getNumWaypts()));

						Log.d("ROSVehicleBridge",
								"Waypoint Client sent and returned "
										+ response.getNumWaypts()
										+ " waypoints");

						// Toast.makeText(context,
						// "Waypoints have been sent",
						// Toast.LENGTH_LONG).show();
						if (commandFragment != null) {
							Handler uiHandler = commandFragment.UIHandler;

							Bundle b = new Bundle();
							b.putInt("WAYPOINT_SENT", 2);
							Message msg = Message.obtain(uiHandler);
							msg.setData(b);
							msg.sendToTarget();
						}

					}

					@Override
					public void onFailure(org.ros.exception.RemoteException e) {
						Log.d("ROSVehicleBridge",
								"Waypoint Client failed on send");

						if (commandFragment != null) {
							Handler uiHandler = commandFragment.UIHandler;

							Bundle b = new Bundle();
							b.putInt("WAYPOINT_SENT", 1);
							Message msg = Message.obtain(uiHandler);
							msg.setData(b);
							msg.sendToTarget();
						}

						connectedNode.shutdown();
						// throw new RosRuntimeException(e);
					}
				});
	}

	// Function to send Commands. Called from Command Fragment
	public void sendCommand(int command) {

		final mission_msgs.ControlMissionRequest request = commandClient
				.newMessage();

		request.setCommand(command);

		commandClient
				.call(request,
						new ServiceResponseListener<mission_msgs.ControlMissionResponse>() {
							@Override
							public void onSuccess(
									mission_msgs.ControlMissionResponse response) {
								connectedNode.getLog().info(
										String.format(
												"Command received was %d",
												response.getCommandReceived()));

								Log.d("ROSVehicleBridge",
										"Command Client sent and returned "
												+ response.getCommandReceived()
												+ " command");

								if (commandFragment != null) {
									Handler uiHandler = commandFragment.UIHandler;

									Bundle b = new Bundle();
									b.putInt("COMMAND_SENT", 2);
									Message msg = Message.obtain(uiHandler);
									msg.setData(b);
									msg.sendToTarget();
								}
							}

							@Override
							public void onFailure(
									org.ros.exception.RemoteException e) {

								Log.d("ROSVehicleBridge",
										"Command Client failed on send");

								if (commandFragment != null) {
									Handler uiHandler = commandFragment.UIHandler;

									Bundle b = new Bundle();
									b.putInt("COMMAND_SENT", 1);
									Message msg = Message.obtain(uiHandler);
									msg.setData(b);
									msg.sendToTarget();
								}
							}
						});
	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("quad_control/ros_vehicle_bridge");
	}

}
