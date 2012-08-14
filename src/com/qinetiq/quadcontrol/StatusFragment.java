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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class StatusFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.status_fragment, container,
				false);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		TextView LatitudeStatus = (TextView) getActivity().findViewById(R.id.lblLatitudeStatusValue);
		TextView LongitudeStatus = (TextView) getActivity().findViewById(R.id.lblLongitudeStatusValue);
		TextView AltitudeStatus = (TextView) getActivity().findViewById(R.id.lblAltitudeStatusValue);
		TextView HeadingStatus = (TextView) getActivity().findViewById(R.id.lblHeadingStatusValue);
		
		LatitudeStatus.setText("TestValue1");
		LongitudeStatus.setText("TestValue2");
		AltitudeStatus.setText("TestValue3");
		HeadingStatus.setText("TestValue4");
	}
}
