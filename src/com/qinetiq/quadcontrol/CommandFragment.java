/**CommandFragment.java*******************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class for controlling the Command Fragment.  Control
 *      		  involves receiving button presses from a user and sending
 *       		  these commands to a connected vehicle through ROS
 *    Call Path : MainActivity->CommandFragment
 * 			XML : res->layout->command_fragment
 * Dependencies : ViewFragmentAdapter, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CommandFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.command_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Capture our button from layout
		Button btnSendWaypts = (Button) getActivity().findViewById(
				R.id.btnSendWaypoints);
		Button btnStartMission = (Button) getActivity().findViewById(
				R.id.btnStartMission);
		Button btnPauseMission = (Button) getActivity().findViewById(
				R.id.btnPauseMission);
		Button btnHaltMission = (Button) getActivity().findViewById(
				R.id.btnHaltMission);
		Button btnReturnToBase = (Button) getActivity().findViewById(
				R.id.btnReturnToBase);

		btnSendWaypts.setOnClickListener(mAddListener);
		btnStartMission.setOnClickListener(mAddListener);
		btnPauseMission.setOnClickListener(mAddListener);
		btnHaltMission.setOnClickListener(mAddListener);
		btnReturnToBase.setOnClickListener(mAddListener);
	}

	// Create an anonymous implementation of OnClickListener
	private OnClickListener mAddListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.btnSendWaypoints:
				Toast.makeText(getActivity(), "Sending Waypoints through ROS",
						Toast.LENGTH_SHORT).show();
				break;

			case R.id.btnStartMission:
				Toast.makeText(getActivity(), "Starting Mission through ROS",
						Toast.LENGTH_SHORT).show();
				break;

			case R.id.btnPauseMission:
				Toast.makeText(getActivity(), "Pausing Mission through ROS",
						Toast.LENGTH_SHORT).show();
				break;

			case R.id.btnHaltMission:
				Toast.makeText(getActivity(), "Halting Mission through ROS",
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.btnReturnToBase:
				Toast.makeText(getActivity(),
						"Sending RTB Command through ROS", Toast.LENGTH_SHORT)
						.show();
				break;

			}

		}
	};
}
