/**GeneralPreferenceFragment.java*********************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class for building and controlling the general app
 *      		  preference fragment of the preferences menu.
 *    Call Path : MainActivity->PreferencesMenu->GeneralPreferenceFragment
 *          XML : res->menu->xml->general_preferences
 * Dependencies : PreferencesMenu
 ****************************************************************************/

package com.qinetiq.quadcontrol.preferences;

import com.qinetiq.quadcontrol.R;
import com.qinetiq.quadcontrol.R.xml;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class GeneralPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_THEME_LIST = "theme_list";
	public static final String KEY_COORDINATE_TYPE_LIST = "coordinate_type_list";

	private ListPreference mThemeList;
	private ListPreference mCoordinateTypeList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		Utils.onActivityCreateSetTheme(this.getActivity());

		// Add Preference XML File
		addPreferencesFromResource(R.xml.general_preferences);

		// Initialize Preference Variables for global editing.
		mThemeList = (ListPreference) getPreferenceScreen().findPreference(
				KEY_THEME_LIST);
		mCoordinateTypeList = (ListPreference) getPreferenceScreen().findPreference(
				KEY_COORDINATE_TYPE_LIST);
	}
	
	@Override
	public void onResume() {
		super.onResume();

		// Grab handle to SharedPreferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());

		// Setup initial values
		mThemeList.setSummary("Current theme is " + prefs.getString(KEY_THEME_LIST, ""));
		mCoordinateTypeList.setSummary("Current type is " + prefs.getString(KEY_COORDINATE_TYPE_LIST, ""));

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

		if (key.equals(KEY_THEME_LIST)) {
			mThemeList.setSummary("Current theme is "
					+ sharedPreferences.getString(key, ""));
			
		} else if (key.equals(KEY_COORDINATE_TYPE_LIST)) {
			mCoordinateTypeList.setSummary("Current type is "
					+ sharedPreferences.getString(key, ""));
		}
		
		

	}
}
