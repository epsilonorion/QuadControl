/**ViewFragmentAdapter.java***************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Class for creating ViewPager Fragment.  Creates initial 
 *      		  strings as well as begins the creation of the fragments 
 *      		  within the viewer.
 *    Call Path : MainActivity->ViewFragmentAdapter
 *    		XML :
 * Dependencies :
 ****************************************************************************/


package com.qinetiq.quadcontrol.fragments;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

public class ViewFragmentAdapter extends FragmentPagerAdapter {
	protected static final String[] CONTENT = new String[] { "Waypt Commands",
			"Waypt List", "Status", };
	
	private final List<Fragment> fragments;

	public ViewFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		
		this.fragments = fragments;
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}

	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return this.CONTENT[position % CONTENT.length];
	}
}
