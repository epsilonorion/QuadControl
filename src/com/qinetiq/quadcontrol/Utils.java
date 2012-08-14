package com.qinetiq.quadcontrol;

import android.app.Activity;
import android.content.Intent;

public class Utils {
	private static int sTheme;

	public final static int THEME_DEFAULT = 0;
    public final static int THEME_WHITE = 1;
    public final static int THEME_BLACK = 2;

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme) {
		sTheme = theme;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));

	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity) {
		switch (sTheme)
        {
        default:
        case THEME_DEFAULT:
            activity.setTheme(R.style.AppTheme);
            break;
        case THEME_WHITE:
            activity.setTheme(R.style.InvertedTheme);
            break;
        case THEME_BLACK:
            activity.setTheme(R.style.BaseTheme);
            break;
        }
	}
}
