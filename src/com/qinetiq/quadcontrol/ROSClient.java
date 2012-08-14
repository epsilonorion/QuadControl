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

package com.qinetiq.quadcontrol;

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

import android.os.RemoteException;
import android.util.Log;

public class ROSClient implements NodeMain {
	private static final String SERVICE_NAME = "SendWaypoints";
	private WaypointList wayptObject;

	public ROSClient(WaypointList wayptObject) {
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
		final Publisher<quadcontrol_msgs.WaypointTrajectory> publisher = connectedNode
				.newPublisher("WaypointTrajectory",
						quadcontrol_msgs.WaypointTrajectory._TYPE);

		final Publisher<quadcontrol_msgs.Waypoint> wayptPublisher = connectedNode
				.newPublisher("Waypoint", quadcontrol_msgs.Waypoint._TYPE);

		quadcontrol_msgs.WaypointTrajectory waypointList = publisher
				.newMessage();
		List<quadcontrol_msgs.Waypoint> waypoints = Lists.newArrayList();

		WaypointInfo point;
		quadcontrol_msgs.Waypoint waypt;

		for (int i = 0; i < wayptObject.size(); i++) {
			point = wayptObject.get(i);

			waypt = wayptPublisher.newMessage();
			waypt.setLatitude(point.getLatitude());
			waypt.setLongitude(point.getLongitude());
			waypt.setSpeed((int) point.getSpeedTo());
			waypt.setHoldTime((int) point.getHoldTime());

			waypoints.add(waypt);
		}

		waypointList.setNumWaypts(waypoints.size());
		waypointList.setPoints(waypoints);

		ServiceClient<quadcontrol_msgs.SendWaypointsRequest, quadcontrol_msgs.SendWaypointsResponse> serviceClient;
		try {
			serviceClient = connectedNode.newServiceClient("send_waypoints",
					"quadcontrol_msgs/SendWaypoints");
		} catch (ServiceNotFoundException e) {
			throw new RosRuntimeException(e);
		}
		final quadcontrol_msgs.SendWaypointsRequest request = serviceClient
				.newMessage();

		request.setList(waypointList);

		serviceClient
				.call(request,
						new ServiceResponseListener<quadcontrol_msgs.SendWaypointsResponse>() {
							@Override
							public void onSuccess(
									quadcontrol_msgs.SendWaypointsResponse response) {
								connectedNode
										.getLog()
										.info(String
												.format("Returned number of waypoints %d",
														response.getNumWaypts()));

								Log.d("TEST", "Returned number of waypoints %d"
										+ response.getNumWaypts());
							}

							@Override
							public void onFailure(
									org.ros.exception.RemoteException e) {
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
		return GraphName.of("android_qinetiq/client");
	}

}
