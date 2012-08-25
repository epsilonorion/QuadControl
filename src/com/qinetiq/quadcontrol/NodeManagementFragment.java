package com.qinetiq.quadcontrol;

import java.net.URI;

import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class NodeManagementFragment extends Fragment {
	private VehicleSubscriber vehSub;
	private CommandClient wayptPub;
	private NodeMainExecutor nodeMainExecutor;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onStart() {
		super.onStart();
//		NodeConfiguration nodeConfiguration = NodeConfiguration
//				.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
//
//		SharedPreferences prefs = PreferenceManager
//				.getDefaultSharedPreferences(getApplicationContext());
//
////		String hostLocal =
////				InetAddressFactory.newNonLoopback().getHostAddress();
//		String hostMaster = prefs.getString("ros_IP", "");
//		Integer port = Integer.parseInt(prefs.getString("ros_port", ""));
//		URI uri = URI.create("http://" + hostMaster + ":" + port);
//		Log.d("Josh", uri.toString());
//		nodeConfiguration.setMasterUri(uri);
//
//		nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
//
//		vehSub = new VehicleSubscriber();
//		wayptPub = new WaypointPublisher();
//		nodeMainExecutor.execute(vehSub, nodeConfiguration);
//		nodeMainExecutor.execute(wayptPub, nodeConfiguration);
	}
}
