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

import android.os.RemoteException;
import android.util.Log;

public class WaypointClient implements NodeMain {
	private WaypointList wayptObject;

	public WaypointClient(WaypointList wayptObject) {
		this.wayptObject = wayptObject;
	}

	@Override
	public void onError(Node node, Throwable throwable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onShutdown(Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onShutdownComplete(Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart(final ConnectedNode connectedNode) {
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
			waypt.setLatitude((int)(point.getLatitude() * 1e7));
			waypt.setLongitude((int)(point.getLongitude() * 1e7));
			waypt.setSpeed((int) point.getSpeedTo());
			waypt.setHoldTime((short) point.getHoldTime());
			waypt.setHeight((int)point.getAltitude());
			waypt.setYawFrom((int)(point.getYawFrom() * 100));

			waypoints.add(waypt);
		}

		waypointList.setNumWaypts(waypoints.size());
		waypointList.setPoints(waypoints);

		ServiceClient<vehicle_control.SendWaypointsRequest, vehicle_control.SendWaypointsResponse> serviceClient;
		try {
			serviceClient = connectedNode.newServiceClient("send_waypoints",
					"vehicle_control/SendWaypoints");
		} catch (ServiceNotFoundException e) {
			Log.d("Error", "WaypointClient - Send_Waypoints Error");
			
			throw new RosRuntimeException(e);
		}
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

								Log.d("TEST", "Returned number of waypoints "
										+ response.getNumWaypts());
							}

							@Override
							public void onFailure(
									org.ros.exception.RemoteException e) {
								Log.d("Error", "WaypointClient - Running onFailure return Error");
								
								throw new RosRuntimeException(e);
							}
						});

		// ServiceClient<beginner_tutorials.AddTwoIntsRequest,
		// beginner_tutorials.AddTwoIntsResponse> serviceClient;
		// try {
		// serviceClient = connectedNode.newServiceClient("add_two_ints",
		// beginner_tutorials.AddTwoInts._TYPE);
		// } catch (ServiceNotFoundException e) {
		// throw new RosRuntimeException(e);
		// }
		// final beginner_tutorials.AddTwoIntsRequest request = serviceClient
		// .newMessage();
		// request.setA(2);
		// request.setB(2);
		// serviceClient
		// .call(request,
		// new ServiceResponseListener<beginner_tutorials.AddTwoIntsResponse>()
		// {
		// @Override
		// public void onSuccess(
		// beginner_tutorials.AddTwoIntsResponse response) {
		// connectedNode.getLog().info(
		// String.format("%d + %d = %d",
		// request.getA(), request.getB(),
		// response.getSum()));
		//
		// Log.d("TEST", "Received back the value "
		// + response.getSum());
		// }
		//
		// @Override
		// public void onFailure(
		// org.ros.exception.RemoteException e) {
		// throw new RosRuntimeException(e);
		// }
		// });

	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("android_qinetiq/WaypointClient");
	}

}
