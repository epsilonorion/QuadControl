/**CommandClient.java********************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 26, 2012
 *      Purpose : Component for creating a ROS Client connection to send
 *      		  command message. 
 *    Call Path : MainActivity->CommandFragment->CommandClient
 * 			XML : 
 * Dependencies : CommandFragment, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CommandClient implements NodeMain {
	int Command = -1;
	
	Context context;
	
	public CommandClient(int Command, Context context) {
		this.Command = Command;
		this.context = context;
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
		ServiceClient<vehicle_control.VehicleCommandRequest, vehicle_control.VehicleCommandResponse> serviceClient;
		
		try {
			serviceClient = connectedNode.newServiceClient("vehicle_command",
					vehicle_control.VehicleCommand._TYPE);
			
			final vehicle_control.VehicleCommandRequest request = serviceClient.newMessage();

			request.setCommand(Command);

			serviceClient
					.call(request,
							new ServiceResponseListener<vehicle_control.VehicleCommandResponse>() {
								@Override
								public void onSuccess(
										vehicle_control.VehicleCommandResponse response) {
									connectedNode
											.getLog()
											.info(String
													.format("Command received was %d",
															response.getCommandReceived()));

									Log.d("TEST", "Command received was "
											+ response.getCommandReceived());
								}

								@Override
								public void onFailure(
										org.ros.exception.RemoteException e) {
									throw new RosRuntimeException(e);
								}
							});

		} catch (ServiceNotFoundException e) {
			Log.d("TEST", "Send Command Failed");
			Looper.prepare();
			Toast.makeText(context, "Send Command failed!",
					Toast.LENGTH_SHORT).show();
			Log.d("TEST", "Sent Toast");
//			throw new RosRuntimeException(e);
		}
		

	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("android_qinetiq/vehicle_command_client");
	}

}
