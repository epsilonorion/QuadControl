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

package com.qinetiq.quadcontrol.ros;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import com.qinetiq.quadcontrol.MainActivity;
import com.qinetiq.quadcontrol.MainApplication;
import com.qinetiq.quadcontrol.R;
import com.qinetiq.quadcontrol.StatusInfo;
import com.qinetiq.quadcontrol.VehicleStatus;
import com.qinetiq.quadcontrol.fragments.CommandFragment;
import com.qinetiq.quadcontrol.fragments.StatusFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

public class VehicleSubscriber implements NodeMain {
	public VehicleStatus vehicleStatusObject;
	StatusFragment statusFragment = null;
	CommandFragment commandFragment = null;
	Context context;
	MainApplication mainApp;
	MainActivity mainActivity;

	public VehicleSubscriber(VehicleStatus vehicleStatusObject,
			StatusFragment statusFragment, CommandFragment commandFragment,
			Context context) {
		Log.d("VehicleSubscriber", "onCreate");
		this.context = context;
		this.commandFragment = commandFragment;
		this.vehicleStatusObject = vehicleStatusObject;
		this.statusFragment = statusFragment;

		mainApp = (MainApplication) context.getApplicationContext();
	}

	@Override
	public void onError(Node node, Throwable throwable) {
		Log.d("VehicleSubscriber", "onError");

		node.shutdown();
	}

	@Override
	public void onShutdown(Node node) {
		Log.d("VehicleSubscriber", "onShutdown");

		if (commandFragment != null) {
			Handler uiHandler = commandFragment.UIHandler;

			Bundle b = new Bundle();
			b.putInt("VEHICLE_CONNECT", 0);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();
		}

		// mainActivity = mainApp.getMainActivity();
		//
		// Log.d("VehicleSubscriber", "Getting MainActivity");
		// if (mainActivity != null) {
		// Log.d("VehicleSubscriber", "Have MainActivity");
		//
		// Handler uiHandler2 = mainActivity.UIHandler;
		//
		// Bundle b = new Bundle();
		// b.putInt("VEHICLE_CONNECT", 0);
		// Message msg = Message.obtain(uiHandler2);
		// msg.setData(b);
		// msg.sendToTarget();
		// }

		mainApp.setConnectedToVehicle(false);
	}

	@Override
	public void onShutdownComplete(Node node) {
		Log.d("VehicleSubscriber", "onShutdownComplete");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		Log.d("Start", "VehicleSubscriber Node Started");

		mainApp.setConnectedToVehicle(true);

		if (commandFragment != null) {
			Handler uiHandler = commandFragment.UIHandler;

			Bundle b = new Bundle();
			b.putInt("VEHICLE_CONNECT", 1);
			Message msg = Message.obtain(uiHandler);
			msg.setData(b);
			msg.sendToTarget();
		}

		Subscriber<vehicle_control.StatusInfo> subscriber = connectedNode
				.newSubscriber("VehicleStatus",
						vehicle_control.StatusInfo._TYPE);
		subscriber
				.addMessageListener(new MessageListener<vehicle_control.StatusInfo>() {
					@Override
					public void onNewMessage(vehicle_control.StatusInfo message) {
						Log.d("TEST", "New Status Message Received");

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

						vehicleStatusObject.setVehicleStatus(tempStatus);

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

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("android_qinetiq/vehicle_subscriber");
	}

}
