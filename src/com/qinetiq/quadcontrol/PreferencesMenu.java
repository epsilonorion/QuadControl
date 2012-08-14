/**PreferencesMenu.java*******************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Activity that handles the building of the PreferencesMenu.
 *      		  Called when a user presses the settings option under the 
 *      		  menu.  Sets up the preference headers which in turn sets up
 *      		  each preference menu as fragments.
 *    Call Path : MainActivity->PreferencesMenu
 *          XML : res->xml->preference_headers
 * Dependencies : 
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PreferencesMenu extends PreferenceActivity {

	@Override
	protected void onDestroy() {
		Toast.makeText(getApplicationContext(), "Finished Changing Settings",
				Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	@Override
	public void onBuildHeaders(List<Header> target) {

		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (prefs.getString("theme_list", "").compareTo("Black Theme") == 0) {
			setTheme(R.style.AppThemeBlack);
		} else if (prefs.getString("theme_list", "").compareTo("White Theme") == 0) {
			setTheme(R.style.AppThemeLight);
		}
		super.onCreate(savedInstanceState);
	}
}
