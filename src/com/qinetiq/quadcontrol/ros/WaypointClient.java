/**ROSClient.java*************************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Test component for creating a ROS Client connection.
 *      		  Currently handles method of grabbing waypoint list and sends
 *      		  to ROS Node.
 *    Call Path : MainActivity->ROSClient
 * 			XML : 
 * Dependencies : WaypointList, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol.ros;

import java.util.List;

import org.ros.concurrent.CancellableLoop;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Publisher;

import com.google.common.collect.Lists;
import com.qinetiq.quadcontrol.WaypointInfo;
import com.qinetiq.quadcontrol.WaypointList;
import com.qinetiq.quadcontrol.fragments.CommandFragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class WaypointClient implements NodeMain {
	private WaypointList wayptObject;
	private Context context;
	private CommandFragment commandFragment;

	public WaypointClient(WaypointList wayptObject,
			CommandFragment commandFragment, Context context) {
		Log.d("WaypointClient", "onCreate");

		this.commandFragment = commandFragment;
		this.wayptObject = wayptObject;
		this.context = context;
	}

	@Override
	public void onError(Node node, Throwable throwable) {
		Log.d("WaypointClient", "onError");
		// Toast.makeText(context, "Failed to send Waypoints",
		// Toast.LENGTH_LONG)
		// .show();
		
		if (commandFragment != null) {
			Handler uiHandler = commandFragment.UIHandler;

			Bundle b = new Bundle();
			b.putInt("WAYPOINT_SENT", 0);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();
		}
	}

	@Override
	public void onShutdown(Node node) {
		Log.d("WaypointClient", "onShutdown");

	}

	@Override
	public void onShutdownComplete(Node node) {
		Log.d("WaypointClient", "onShutdownComplete");

	}

	@Override
	public void onStart(final ConnectedNode connectedNode) {
		Log.d("Start", "WaypointClient Started");

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

		for (int i = 0; i < wayptObject.size(); i++) {
			point = wayptObject.get(i);

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

		ServiceClient<vehicle_control.SendWaypointsRequest, vehicle_control.SendWaypointsResponse> serviceClient;
		
		try {
			serviceClient = connectedNode.newServiceClient("send_waypoints",
					"vehicle_control/SendWaypoints");

			final vehicle_control.SendWaypointsRequest request = serviceClient
					.newMessage();

			request.setList(waypointList);

			serviceClient
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

									Log.d("TEST",
											"Returned number of waypoints "
													+ response.getNumWaypts());

									// Toast.makeText(context,
									// "Waypoints have been sent",
									// Toast.LENGTH_LONG).show();
									if (commandFragment != null) {
										Handler uiHandler = commandFragment.UIHandler;

										Bundle b = new Bundle();
										b.putInt("WAYPOINT_SENT", 1);
										Message msg = Message.obtain(uiHandler);
										msg.setData(b);
										msg.sendToTarget();
									}
									
								}

								@Override
								public void onFailure(
										org.ros.exception.RemoteException e) {
									Log.d("Error",
											"WaypointClient - Running onFailure return Error");

									// Toast.makeText(context,
									// "Failed to send Waypoints",
									// Toast.LENGTH_LONG).show();
									
									if (commandFragment != null) {
										Handler uiHandler = commandFragment.UIHandler;

										Bundle b = new Bundle();
										b.putInt("WAYPOINT_SENT", 0);
										Message msg = Message.obtain(uiHandler);
										msg.setData(b);
										msg.sendToTarget();
									}

									connectedNode.shutdown();
									// throw new RosRuntimeException(e);
								}
							});
		} catch (ServiceNotFoundException e) {
			Log.d("Error", "WaypointClient - Send_Waypoints Error");

			// Toast.makeText(context, "Failed to send Waypoints",
			// Toast.LENGTH_LONG).show();
			
			if (commandFragment != null) {
				Handler uiHandler = commandFragment.UIHandler;

				Bundle b = new Bundle();
				b.putInt("WAYPOINT_SENT", 0);
				Message msg = Message.obtain(uiHandler);
				msg.setData(b);
				msg.sendToTarget();
			}

			//throw new RosRuntimeException(e);
		}
	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("android_qinetiq/WaypointClient");
	}

}
