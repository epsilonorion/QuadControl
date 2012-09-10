/**VehiclePreferenceFragment.java*********************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class for building and controlling the vehicle preference
 *      		  fragment.  Handles preferences that are used for vehicle
 *      		  components.
 *    Call Path : MainActivity->PreferencesMenu->VehiclePreferenceFragment
 *          XML : res->menu->xml->vehicle_preferences
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

public class VehiclePreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_DEFAULT_SPEED = "default_speed";
	public static final String KEY_DEFAULT_ALTITUDE = "default_altitude";
	public static final String KEY_DEFAULT_HOLD_TIME = "default_hold_time";
	public static final String KEY_DEFAULT_YAW_FROM = "default_yaw_from";
	public static final String KEY_DEFAULT_PAN_POSITION = "default_pan_position";
	public static final String KEY_DEFAULT_TILT_POSITION = "default_tilt_position";
	
	private EditTextPreference mDefaultSpeed;
	private EditTextPreference mDefaultAltitude;
	private EditTextPreference mDefaultHoldTime;
	private EditTextPreference mDefaultYawFrom;
	private EditTextPreference mDefaultPanPosition;
	private EditTextPreference mDefaultTiltPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Add Preference XML File
		addPreferencesFromResource(R.xml.vehicle_preferences);

		// Initialize Preference Variables for global editing.
		mDefaultSpeed = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_DEFAULT_SPEED);
		mDefaultAltitude = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_DEFAULT_ALTITUDE);
		mDefaultHoldTime = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_DEFAULT_HOLD_TIME);
		mDefaultYawFrom = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_DEFAULT_YAW_FROM);
		mDefaultPanPosition = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_DEFAULT_PAN_POSITION);
		mDefaultTiltPosition = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_DEFAULT_TILT_POSITION);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Grab handle to SharedPreferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());

		// Setup initial values
		mDefaultSpeed.setSummary("Current default speed is "
				+ prefs.getString(KEY_DEFAULT_SPEED, ""));
		mDefaultAltitude.setSummary("Current default altitude is "
				+ prefs.getString(KEY_DEFAULT_ALTITUDE, ""));
		mDefaultHoldTime.setSummary("Current default hold time is "
				+ prefs.getString(KEY_DEFAULT_HOLD_TIME, ""));
		mDefaultYawFrom.setSummary("Current default yaw from heading is "
				+ prefs.getString(KEY_DEFAULT_YAW_FROM, ""));
		mDefaultPanPosition.setSummary("Current default pan position is "
				+ prefs.getString(KEY_DEFAULT_PAN_POSITION, ""));
		mDefaultTiltPosition.setSummary("Current default tilt position is "
				+ prefs.getString(KEY_DEFAULT_TILT_POSITION, ""));

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

		if (key.equals(KEY_DEFAULT_SPEED)) {
			mDefaultSpeed.setSummary("Current default speed is "
					+ sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_DEFAULT_ALTITUDE)) {
			mDefaultAltitude.setSummary("Current default altitude is "
					+ sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_DEFAULT_HOLD_TIME)) {
			mDefaultHoldTime.setSummary("Current default hold time is "
					+ sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_DEFAULT_YAW_FROM)) {
			mDefaultYawFrom.setSummary("Current default yaw from heading is "
					+ sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_DEFAULT_PAN_POSITION)) {
			mDefaultPanPosition.setSummary("Current default pan position is "
					+ sharedPreferences.getString(key, ""));
		} else if (key.equals(KEY_DEFAULT_TILT_POSITION)) {
			mDefaultTiltPosition.setSummary("Current default tilt position is "
					+ sharedPreferences.getString(key, ""));
		}
		

	}
}
