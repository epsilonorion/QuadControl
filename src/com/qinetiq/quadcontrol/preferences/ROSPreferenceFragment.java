/**ROSPreferenceFragment.java*************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class for building and controlling the ROS preference
 *      		  fragment.  Handles preferences that are used for ROS
 *      		  components.
 *    Call Path : MainActivity->PreferencesMenu->ROSPreferenceFragment
 *          XML : res->menu->xml->ros_preferences
 * Dependencies : PreferencesMenu
 ****************************************************************************/

package com.qinetiq.quadcontrol.preferences;

import com.qinetiq.quadcontrol.R;
import com.qinetiq.quadcontrol.R.xml;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class ROSPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_VEHICLE_LIST = "vehicle_type_list";
	public static final String KEY_ROS_IP = "ros_IP";
	public static final String KEY_ROS_PORT = "ros_port";
	public static final String KEY_CLIENT_TIMEOUT = "client_timeout";

	private EditTextPreference mROSIP;
	private EditTextPreference mROSPort;
	private EditTextPreference mClientTimeout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		PreferenceManager.setDefaultValues(getActivity(), R.xml.ros_preferences, false);
//		Utils.onActivityCreateSetTheme(getActivity());
		// Add Preference XML File
		addPreferencesFromResource(R.xml.ros_preferences);
		
		// Initialize Preference Variables for global editing.
		mROSIP = (EditTextPreference) getPreferenceScreen().findPreference(
				KEY_ROS_IP);
		mROSPort = (EditTextPreference) getPreferenceScreen().findPreference(
				KEY_ROS_PORT);
		mClientTimeout = (EditTextPreference) getPreferenceScreen().findPreference(
				KEY_CLIENT_TIMEOUT);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Grab handle to SharedPreferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());

		// Setup initial values
		mROSIP.setSummary("Current IP is " + prefs.getString(KEY_ROS_IP, ""));
		mROSPort.setSummary("Current port is "
				+ prefs.getString(KEY_ROS_PORT, ""));
		mClientTimeout.setSummary("Current timeout is "
				+ prefs.getString(KEY_CLIENT_TIMEOUT, ""));

		// Set up a listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this.getActivity())
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		
		// Unregister the listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this.getActivity())
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		Log.d("Josh", "Changed");
		
		if (key.equals(KEY_ROS_IP)) {
			mROSIP.setSummary("Current IP is "
					+ sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_ROS_PORT)) {
			mROSPort.setSummary("Current port is "
					+ sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_CLIENT_TIMEOUT)) {
			mClientTimeout.setSummary("Current timeout is "
					+ sharedPreferences.getString(key, ""));
		}

	}
}
