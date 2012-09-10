/**ROSService.java************************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Test component for creating a ROS Service connection.
 *      		  Currently just a placeholder with test code in case a
 *      		  service node is needed.
 *    Call Path : MainActivity->ROSService
 * 			XML : 
 * Dependencies : ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol.ros;

import org.ros.exception.ServiceException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceResponseBuilder;
import org.ros.node.service.ServiceServer;

import android.util.Log;

public class ROSService extends AbstractNodeMain {

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
	public void onStart(ConnectedNode connectedNode) {
		connectedNode
				.newServiceServer(
						"add_two_ints",
						test_ros.AddTwoInts._TYPE,
						new ServiceResponseBuilder<test_ros.AddTwoIntsRequest, test_ros.AddTwoIntsResponse>() {
							@Override
							public void build(
									test_ros.AddTwoIntsRequest request,
									test_ros.AddTwoIntsResponse response) {
								response.setSum(request.getA() + request.getB());
								
								Log.d("TEST", "Sum is " + response.getSum());
							}
						});
	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("android_qinetiq/server");
	}

}
