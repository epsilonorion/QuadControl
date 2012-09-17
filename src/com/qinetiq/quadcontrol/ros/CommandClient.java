/**CommandClient.java********************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 26, 2012
 *      Purpose : Component for creating a ROS Client connection to send
 *      		  command message. 
 *    Call Path : MainActivity->CommandFragment->CommandClient
 * 			XML : 
 * Dependencies : CommandFragment, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol.ros;

import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

import com.qinetiq.quadcontrol.fragments.CommandFragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CommandClient implements NodeMain {
	int Command = -1;

	Context context;
	private CommandFragment commandFragment;

	public CommandClient(int Command, CommandFragment commandFragment,
			Context context) {
		Log.d("CommandClient", "onCreate");

		this.commandFragment = commandFragment;
		this.Command = Command;
		this.context = context;
	}

	@Override
	public void onError(Node node, Throwable throwable) {
		Log.d("CommandClient", "onError");

		// Toast.makeText(context, "Failed to send Command", Toast.LENGTH_LONG)
		// .show();
		
		if (commandFragment != null) {
			Handler uiHandler = commandFragment.UIHandler;

			Bundle b = new Bundle();
			b.putInt("COMMAND_SENT", 0);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();
		}
	}

	@Override
	public void onShutdown(Node node) {
		Log.d("CommandClient", "onShutdown");

	}

	@Override
	public void onShutdownComplete(Node node) {
		Log.d("CommandClient", "onShutdownComplete");

	}

	@Override
	public void onStart(final ConnectedNode connectedNode) {
		Log.d("CommandClient", "onStart");

		ServiceClient<vehicle_control.VehicleCommandRequest, vehicle_control.VehicleCommandResponse> serviceClient;

		try {
			serviceClient = connectedNode.newServiceClient("vehicle_command",
					vehicle_control.VehicleCommand._TYPE);

			final vehicle_control.VehicleCommandRequest request = serviceClient
					.newMessage();

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
									
									if (commandFragment != null) {
										Handler uiHandler = commandFragment.UIHandler;

										Bundle b = new Bundle();
										b.putInt("COMMAND_SENT", 1);
										Message msg = Message.obtain(uiHandler);
										msg.setData(b);
										msg.sendToTarget();
									}
								}

								@Override
								public void onFailure(
										org.ros.exception.RemoteException e) {
									// Toast.makeText(context,
									// "Failed to send Command",
									// Toast.LENGTH_LONG).show();
									// throw new RosRuntimeException(e);
									
									if (commandFragment != null) {
										Handler uiHandler = commandFragment.UIHandler;

										Bundle b = new Bundle();
										b.putInt("COMMAND_SENT", 0);
										Message msg = Message.obtain(uiHandler);
										msg.setData(b);
										msg.sendToTarget();
									}
								}
							});

		} catch (ServiceNotFoundException e) {
			// Looper.prepare();
			// Toast.makeText(context, "Failed to send Command",
			// Toast.LENGTH_LONG)
			// .show();
			// throw new RosRuntimeException(e);
			
			if (commandFragment != null) {
				Handler uiHandler = commandFragment.UIHandler;

				Bundle b = new Bundle();
				b.putInt("COMMAND_SENT", 0);
				Message msg = Message.obtain(uiHandler);
				msg.setData(b);
				msg.sendToTarget();
			}
		}

	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("android_qinetiq/vehicle_command_client");
	}

}
