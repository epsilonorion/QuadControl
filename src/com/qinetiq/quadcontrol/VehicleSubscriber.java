/**VehicleSubscriber.java*****************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Test component for creating a ROS subscriber connection.
 *      		  Currently handles method of capturing status information 
 *      		  from ROS status node.
 *    Call Path : MainActivity->VehicleSubscriber
 *    		XML :
 * Dependencies : ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import android.util.Log;

public class VehicleSubscriber implements NodeMain {

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
		Log.d("Start", "VehicleSubscriber Node Started");
		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("josh",
				std_msgs.String._TYPE);
		subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
			@Override
			public void onNewMessage(std_msgs.String message ) {
				Log.d("error", message.getData());
			}
		});

	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("android_qinetiq/vehicle_subscriber");
	}

}
