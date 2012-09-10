/**WaypointEntryFragment.java*************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class for controlling the WaypointEntry Fragment.  The
 *      		  fragment focuses on manual entry and modification of
 *      		  waypoints.
 *    Call Path : MainActivity->WaypointEntryFragment
 * 			XML :
 * Dependencies : ViewFragmentAdapter, ROSJava, Android-Core
 ****************************************************************************/

package com.qinetiq.quadcontrol.fragments;

import com.qinetiq.quadcontrol.R;
import com.qinetiq.quadcontrol.R.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WaypointEntryFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.waypoint_entry_fragment, container,
				false);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
	}
}
