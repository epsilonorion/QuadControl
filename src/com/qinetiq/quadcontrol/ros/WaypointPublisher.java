/**WaypointPublisher.java*****************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Test component for creating a ROS publisher connection.
 *      		  Currently a container example of publishing messages.  New
 *      		  version holds method for sending commands.
 *    Call Path : MainActivity->WaypointPublisher
 *    		XML :
 * Dependencies : WaypointList, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol.ros;

import java.util.Arrays;
import java.util.List;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import com.google.common.collect.Lists;

import android.util.Log;

public class WaypointPublisher implements NodeMain {

	@Override
	public void onError(Node node, Throwable throwable) {

	}

	@Override
	public void onShutdown(Node node) {
		node.shutdown();
	}

	@Override
	public void onShutdownComplete(Node node) {

	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		
		Log.d("Start", "WaypointPublisher Node Started");

		final Publisher<vehicle_control.WaypointTrajectory> publisher = connectedNode
				.newPublisher("WaypointTrajectory",
						vehicle_control.WaypointTrajectory._TYPE);

		final Publisher<vehicle_control.Waypoint> wayptPublisher = connectedNode
				.newPublisher("Waypoint", vehicle_control.Waypoint._TYPE);
	
		connectedNode.executeCancellableLoop(new CancellableLoop() {

			@Override
			protected void setup() {
			}

			@Override
			protected void loop() throws InterruptedException {
				vehicle_control.WaypointTrajectory test = publisher
						.newMessage();

				List<vehicle_control.Waypoint> WayptL = Lists.newArrayList();

				vehicle_control.Waypoint waypt = wayptPublisher.newMessage();

				waypt.setLatitude(0);
				waypt.setLongitude(0);
				waypt.setSpeed(0);
				waypt.setHoldTime((short)0);

				WayptL.add(waypt);

				waypt = wayptPublisher.newMessage();

				waypt.setLatitude(1);
				waypt.setLongitude(1);
				waypt.setSpeed(1);
				waypt.setHoldTime((short)1);

				WayptL.add(waypt);

				waypt = wayptPublisher.newMessage();
				waypt.setLatitude(2);
				waypt.setLongitude(2);
				waypt.setSpeed(2);
				waypt.setHoldTime((short)2);

				WayptL.add(waypt);

				waypt = wayptPublisher.newMessage();
				waypt.setLatitude(3);
				waypt.setLongitude(3);
				waypt.setSpeed(3);
				waypt.setHoldTime((short)3);

				WayptL.add(waypt);

				waypt = wayptPublisher.newMessage();
				waypt.setLatitude(4);
				waypt.setLongitude(4);
				waypt.setSpeed(4);
				waypt.setHoldTime((short)4);

				WayptL.add(waypt);

				test.setNumWaypts(WayptL.size());

				test.setPoints(WayptL);

				publisher.publish(test);

				Thread.sleep(100);

			}
		});

	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("android_qinetiq/waypoint_publisher");
	}

}
