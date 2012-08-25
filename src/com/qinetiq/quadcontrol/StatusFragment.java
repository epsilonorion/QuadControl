/**StatusFragment.java*******************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Fragment for displaying vehicle information received
 *      		  through ROS.  Currently a container, code complete in other
 *      		  version.  Displayed with ViewPager
 *    Call Path : MainActivity->StatusFragment
 *          XML : res->layout->status_fragment
 * Dependencies : ViewFragmentAdapter, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusFragment extends Fragment {
	VehicleStatus vehicleStatus = null;

	TextView VehicleNameStatus;
	TextView LatitudeStatus;
	TextView LongitudeStatus;
	TextView AltitudeStatus;
	TextView HeadingStatus;
	TextView SpeedStatus;
	TextView PanAngleStatus;
	TextView TiltAngleStatus;
	TextView BatteryStatus;
	TextView GPSStatus;
	TextView CurrWaypointStatus;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.status_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Handle Grabbing the VehicleStatus Object
		Bundle bundle = getArguments();
		if (bundle != null) {
			vehicleStatus = bundle.getParcelable("vehicleStatusObject");
		}

		VehicleNameStatus = (TextView) getActivity().findViewById(
				R.id.lblVehicleNameValue);

		LatitudeStatus = (TextView) getActivity().findViewById(
				R.id.lblLatitudeStatusValue);
		LongitudeStatus = (TextView) getActivity().findViewById(
				R.id.lblLongitudeStatusValue);
		AltitudeStatus = (TextView) getActivity().findViewById(
				R.id.lblAltitudeStatusValue);
		HeadingStatus = (TextView) getActivity().findViewById(
				R.id.lblHeadingStatusValue);

		SpeedStatus = (TextView) getActivity().findViewById(
				R.id.lblSpeedStatusValue);
		PanAngleStatus = (TextView) getActivity().findViewById(
				R.id.lblPanAngleStatusValue);
		TiltAngleStatus = (TextView) getActivity().findViewById(
				R.id.lblTiltAngleStatusValue);
		BatteryStatus = (TextView) getActivity().findViewById(
				R.id.lblBatteryStatusValue);

		GPSStatus = (TextView) getActivity()
				.findViewById(R.id.lblGPStatusValue);
		CurrWaypointStatus = (TextView) getActivity().findViewById(
				R.id.lblCurrWaypointStatusValue);

		// // Grab preferences of the application
		// SharedPreferences prefs = PreferenceManager
		// .getDefaultSharedPreferences(getActivity());
		//
		// VehicleSubscriber vehSub;
		//
		// NodeMainExecutor nodeMainExecutor;
		// NodeConfiguration nodeConfiguration;
		//
		// nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory
		// .newNonLoopback().getHostAddress());
		//
		// String hostMaster = prefs.getString("ros_IP", "");
		// Integer port = Integer.parseInt(prefs.getString("ros_port", ""));
		// URI uri = URI.create("http://" + hostMaster + ":" + port);
		// Log.d("Josh", uri.toString());
		// nodeConfiguration.setMasterUri(uri);
		//
		// nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
		//
		// vehSub = new VehicleSubscriber(vehicleStatus, statusFragment);
		//
		// nodeMainExecutor.execute(vehSub, nodeConfiguration);

	}

	public void updateText() {
		Log.d("TEST", "I GOT HERE");

		if (vehicleStatus != null) {
			StatusInfo vehicleStatusInfo = vehicleStatus.getVehicleStatus();

			VehicleNameStatus.setText(vehicleStatusInfo.getVehicleName()
					+ " Status");

			LatitudeStatus.setText("" + vehicleStatusInfo.getLatitude());
			LongitudeStatus.setText("" + vehicleStatusInfo.getLongitude());
			AltitudeStatus.setText("" + vehicleStatusInfo.getAltitude());
			HeadingStatus.setText("" + vehicleStatusInfo.getHeading());

			SpeedStatus.setText("" + vehicleStatusInfo.getSpeed());
			PanAngleStatus.setText("" + vehicleStatusInfo.getPanAngle());
			TiltAngleStatus.setText("" + vehicleStatusInfo.getTiltAngle());
			BatteryStatus.setText("" + vehicleStatusInfo.getBatteryStatus());

			GPSStatus.setText("" + vehicleStatusInfo.getGpsStatus());
			CurrWaypointStatus
					.setText("" + vehicleStatusInfo.getCurrWaypoint());
		}
	}

	public Handler UIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.getData().getInt("VEHICLE_STATUS")) {
			case 1: {
				updateText();
			}
				break;
			}
		};
	};
}
