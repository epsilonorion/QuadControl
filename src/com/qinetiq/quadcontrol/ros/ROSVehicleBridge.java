package com.qinetiq.quadcontrol.ros;

import java.util.List;

import org.ros.concurrent.CancellableLoop;
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
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
import com.qinetiq.quadcontrol.MainApplication;
import com.qinetiq.quadcontrol.StatusInfo;
import com.qinetiq.quadcontrol.VehicleStatus;
import com.qinetiq.quadcontrol.WaypointInfo;
import com.qinetiq.quadcontrol.WaypointList;
import com.qinetiq.quadcontrol.fragments.CommandFragment;
import com.qinetiq.quadcontrol.fragments.StatusFragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ROSVehicleBridge implements NodeMain {
	private VehicleStatus vehicleStatus;
	private WaypointList wayptList;
	private StatusFragment statusFragment;
	private CommandFragment commandFragment;
	private Context context;

	private MainApplication mainApp;

	private ConnectedNode connectedNode;
	private boolean nodeStarted = false;

	private ServiceClient<vehicle_control.SendWaypointsRequest, vehicle_control.SendWaypointsResponse> waypointClient = null;
	private ServiceClient<vehicle_control.VehicleCommandRequest, vehicle_control.VehicleCommandResponse> commandClient = null;

	public static final int START_COMMAND = 0;
	public static final int PAUSE_COMMAND = 1;
	public static final int HALT_COMMAND = 2;
	public static final int RTB_COMMAND = 3;

	public ROSVehicleBridge(VehicleStatus vehicleStatus,
			WaypointList wayptList, StatusFragment statusFragment,
			CommandFragment commandFragment, Context context) {
		Log.d("ClassCreate", "ROSVehicleBridge");

		this.vehicleStatus = vehicleStatus;
		this.wayptList = wayptList;
		this.statusFragment = statusFragment;
		this.commandFragment = commandFragment;
		this.context = context;

		mainApp = (MainApplication) context.getApplicationContext();
	}

	@Override
	public void onError(Node node, Throwable throwable) {
		Log.d("FunctionCall", "ROSVehicleBridge onError");

		node.shutdown();
	}

	@Override
	public void onShutdown(Node node) {
		Log.d("FunctionCall", "ROSVehicleBridge onShutdown");

		if (commandFragment != null) {
			Handler uiHandler = commandFragment.UIHandler;

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
		Subscriber<vehicle_control.StatusInfo> subscriber = connectedNode
				.newSubscriber("VehicleStatus",
						vehicle_control.StatusInfo._TYPE);
		subscriber
				.addMessageListener(new MessageListener<vehicle_control.StatusInfo>() {
					@Override
					public void onNewMessage(vehicle_control.StatusInfo message) {
						StatusInfo tempStatus = new StatusInfo();

						tempStatus.setVehicleName(message.getVehicleName());
						tempStatus.setLatitude(message.getLatitude() / 1e7);
						tempStatus.setLongitude(message.getLongitude() / 1e7);
						tempStatus.setHeading(message.getHeading() / 1000.0);
						tempStatus.setSpeed(message.getSpeed());
						tempStatus.setAltitude(message.getAltitude());
						tempStatus.setPanAngle(message.getPanAngle());
						tempStatus.setTiltAngle(message.getTiltAngle());
						tempStatus.setBatteryStatus(message.getBatteryStatus() / 1000.0);
						tempStatus.setGpsStatus(message.getGpsStatus());
						tempStatus.setCurrWaypoint(message.getCurrWaypoint());

						vehicleStatus.setVehicleStatus(tempStatus);

						// Send message to UI Handler under the Status Fragment
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

		// Create WaypointClient Object
		try {
			waypointClient = connectedNode.newServiceClient("send_waypoints",
					"vehicle_control/SendWaypoints");
		} catch (ServiceNotFoundException e) {
			if (commandFragment != null) {
				Handler uiHandler = commandFragment.UIHandler;

				Bundle b = new Bundle();
				b.putInt("WAYPOINT_SENT", 0);
				Message msg = Message.obtain(uiHandler);
				msg.setData(b);
				msg.sendToTarget();
			}
		}

		// Create CommandClient Object
		try {
			commandClient = connectedNode.newServiceClient("vehicle_command",
					vehicle_control.VehicleCommand._TYPE);
		} catch (ServiceNotFoundException e) {
			if (commandFragment != null) {
				Handler uiHandler = commandFragment.UIHandler;

				Bundle b = new Bundle();
				b.putInt("COMMAND_SENT", 0);
				Message msg = Message.obtain(uiHandler);
				msg.setData(b);
				msg.sendToTarget();
			}
		}
		
		// Set variables to show vehicles is connected.
		mainApp.setConnectedToVehicle(true);

		if (commandFragment != null) {
			Handler uiHandler = commandFragment.UIHandler;

			Bundle b = new Bundle();
			b.putInt("VEHICLE_CONNECT", 2);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();
		}
	}

	// Function to send Waypoints. Called from Command Fragment
	public void sendWaypoints() {
		// Grab Message types for Waypoint and WaypointTrajectory
		final Publisher<vehicle_control.WaypointTrajectory> publisher = connectedNode
				.newPublisher("WaypointTrajectory",
						vehicle_control.WaypointTrajectory._TYPE);

		final Publisher<vehicle_control.Waypoint> wayptPublisher = connectedNode
				.newPublisher("Waypoint", vehicle_control.Waypoint._TYPE);

		vehicle_control.WaypointTrajectory waypointList = publisher
				.newMessage();
		List<vehicle_control.Waypoint> waypoints = Lists.newArrayList();

		WaypointInfo point;
		vehicle_control.Waypoint waypt;

		// Populate waypoint list message
		for (int i = 0; i < wayptList.size(); i++) {
			point = wayptList.get(i);

			waypt = wayptPublisher.newMessage();
			waypt.setLatitude((int) (point.getLatitude() * 1e7));
			waypt.setLongitude((int) (point.getLongitude() * 1e7));
			waypt.setSpeed((int) point.getSpeedTo());
			waypt.setHoldTime((short) point.getHoldTime());
			waypt.setHeight((int) point.getAltitude());
			waypt.setYawFrom((int) (point.getYawFrom() * 100));

			waypoints.add(waypt);
		}

		waypointList.setNumWaypts(waypoints.size());
		waypointList.setPoints(waypoints);

		// Create WaypointClient Message for request
		final vehicle_control.SendWaypointsRequest request = waypointClient
				.newMessage();

		request.setList(waypointList);

		// Send Client Request and wait for response
		waypointClient
				.call(request,
						new ServiceResponseListener<vehicle_control.SendWaypointsResponse>() {
							@Override
							public void onSuccess(
									vehicle_control.SendWaypointsResponse response) {
								connectedNode
										.getLog()
										.info(String
												.format("Returned number of waypoints %d",
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
							public void onFailure(
									org.ros.exception.RemoteException e) {
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
		final vehicle_control.VehicleCommandRequest request = commandClient
				.newMessage();

		request.setCommand(command);

		commandClient
				.call(request,
						new ServiceResponseListener<vehicle_control.VehicleCommandResponse>() {
							@Override
							public void onSuccess(
									vehicle_control.VehicleCommandResponse response) {
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
		return GraphName.of("android_qinetiq/ros_vehicle_bridge");
	}

}
