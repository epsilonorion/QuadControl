/**WaypointsOverlay.java*****************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 13, 2012
 *      Purpose : Overlay class for MapFragment.  Handles creation and
 *      		  modification of items in WaypointList and therefore overlay.
 *    Call Path : MainActivity->MapFragment->WaypointsOverlay
 *    		XML :
 * Dependencies : MapFragment
 ****************************************************************************/

package com.qinetiq.quadcontrol;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

// Represents list of markers

public class WaypointsOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
	Context context;

	private Drawable marker = null;
	private OverlayItem inDrag = null;
	private int indexDrag = -1;
	private ImageView dragImage = null;
	private int xDragImageOffset = 0;
	private int yDragImageOffset = 0;
	private int xDragTouchOffset = 0;
	private int yDragTouchOffset = 0;

	private Boolean addWayptFlag = false;
	private Boolean deleteWayptFlag = false;
	private Boolean moveWayptFlag = false;
	private Boolean modifyWayptFlag = false;
	
	private int indexCount = 0;

	WaypointList wayptList;
	SharedPreferences prefs;

	public WaypointsOverlay(Drawable marker, ImageView dragImage, Context c,
			WaypointList wayptList) {
		super(boundCenterBottom(marker));
		this.marker = marker;
		this.dragImage = dragImage;
		this.wayptList = wayptList;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(c);

		this.context = c;

		xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
		yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();

		populate();
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {

		if (super.onTap(p, mapView)) {
			return true;
		}

		if (addWayptFlag) {
			// Given current type of waypoint setup, the first added waypoint
			// will be the Return-To-Base (RTB) Waypoint. Each waypoint after
			// this will be called Waypoint. This is to be modified when more
			// advanced waypoint schemes are used.

			String label = "";

			if (wayptList.size() == 0) {
				label = "RTB";
			} else {
				label = "Waypoint";
			}

			// Add waypoint
			// Structure Setup WaypointInfo Object
			WaypointInfo waypt = new WaypointInfo(
					label,
					p.getLatitudeE6() / 1E6,
					p.getLongitudeE6() / 1E6,
					Double.parseDouble(prefs.getString("default_speed", "25")),
					Double.parseDouble(prefs.getString("default_altitude", "10")),
					Double.parseDouble(prefs.getString("default_hold_time", "10")),
					Double.parseDouble(prefs.getString("default_pan_position",
							"100")), Double.parseDouble(prefs.getString(
							"default_tilt_position", "100")), 
							Double.parseDouble(prefs.getString("default_yaw_from", "0")));

			// Add waypoint to waypoint list object
			wayptList.add(waypt);
		}

		return true;
	}

	@Override
	protected boolean onTap(int index) {
		// if (super.onTap(index)) {
		// return true;
		// }
		if (deleteWayptFlag) {
		} else {
			GeoPoint p = overlayItemList.get(index).getPoint();
			String latlng = String.valueOf(p.getLatitudeE6() / 1E6) + ","
					+ String.valueOf(p.getLongitudeE6() / 1E6);

			Toast.makeText(
					context,
					overlayItemList.get(index).getTitle() + ", Waypoint "
							+ index + " at " + latlng, Toast.LENGTH_SHORT)
					.show();
		}

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		boolean result = false;
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();

		// If modifying waypoint has been selected, setup to move the waypoint.
		if (moveWayptFlag) {
			if (action == MotionEvent.ACTION_DOWN) {
				indexCount = 0;
				for (OverlayItem item : overlayItemList) {
					Point p = new Point(0, 0);

					mapView.getProjection().toPixels(item.getPoint(), p);

					if (hitTest(item, marker, x - p.x, y - p.y)) {
						result = true;
						inDrag = item;
						indexDrag = overlayItemList.indexOf(inDrag);
						overlayItemList.remove(inDrag);
						populate();

						xDragTouchOffset = 0;
						yDragTouchOffset = 0;

						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset = x - p.x;
						yDragTouchOffset = y - p.y;
						break;
					}

					indexCount++;
				}
			} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
				setDragImagePosition(x, y);
				result = true;
			} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
				dragImage.setVisibility(View.GONE);

				GeoPoint pt = mapView.getProjection().fromPixels(
						x - xDragTouchOffset, y - yDragTouchOffset);
				
				WaypointInfo modifiedMarker = wayptList.get(indexDrag);
				
				modifiedMarker.setLatitude(pt.getLatitudeE6() / 1E6);
				modifiedMarker.setLongitude(pt.getLongitudeE6() / 1E6);
				
//				OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(),
//						inDrag.getSnippet());

				// Modified Waypoint
//				overlayItemList.add(toDrop);
				wayptList.modify(indexDrag,  modifiedMarker);
				populate();

				String latlng = String.valueOf(pt.getLatitudeE6() / 1E6) + ","
						+ String.valueOf(pt.getLongitudeE6() / 1E6);

				//wayptListFragment.modifyItem(indexCount, latlng);
				inDrag = null;
				result = true;
			}
		} else {
			if (action == MotionEvent.ACTION_UP) {
				int count = 0;
				for (OverlayItem item : overlayItemList) {
					Point p = new Point(0, 0);

					mapView.getProjection().toPixels(item.getPoint(), p);

					if (hitTest(item, marker, x - p.x, y - p.y)) {
						if (deleteWayptFlag) {
							result = true;
							wayptList.remove(count);
						}
						break;
					}
					count++;
				}
			}

		}

		return (result || super.onTouchEvent(event, mapView));
	}

	private void setDragImagePosition(int x, int y) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage
				.getLayoutParams();

		lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y
				- yDragImageOffset - yDragTouchOffset, 0, 0);
		
		dragImage.setLayoutParams(lp);
	}

	public void addItem(WaypointInfo waypt) {
		GeoPoint p = new GeoPoint((int) (waypt.getLatitude() * 1E6),
				(int) (waypt.getLongitude() * 1E6));

		String title = waypt.getName();
		String snippet = "geo:\n" + String.valueOf(p.getLatitudeE6()) + "\n"
				+ String.valueOf(p.getLongitudeE6());

		OverlayItem newItem = new OverlayItem(p, title, snippet);
		overlayItemList.add(newItem);
		populate();
	}

	public void removeItem(int index) {
		overlayItemList.remove(index);
		populate();
	}
	
	public void clearItems() {
		overlayItemList.clear();
		populate();
	}
	
	public void modifyItem(int index, WaypointInfo waypt) {
		GeoPoint p = new GeoPoint((int) (waypt.getLatitude() * 1E6),
				(int) (waypt.getLongitude() * 1E6));

		String title = waypt.getName();
		String snippet = "geo:\n" + String.valueOf(p.getLatitudeE6()) + "\n"
				+ String.valueOf(p.getLongitudeE6());

		OverlayItem modifiedItem = new OverlayItem(p, title, snippet);
		
		overlayItemList.add(index, modifiedItem);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return overlayItemList.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return overlayItemList.size();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);
	}

	public Boolean getAddWayptFlag() {
		return addWayptFlag;
	}

	public void setAddWayptFlag(Boolean addWayptFlag) {
		this.addWayptFlag = addWayptFlag;
	}

	public Boolean getDeleteWayptFlag() {
		return deleteWayptFlag;
	}

	public void setDeleteWayptFlag(Boolean deleteWayptFlag) {
		this.deleteWayptFlag = deleteWayptFlag;
	}

	public Boolean getMoveWayptFlag() {
		return moveWayptFlag;
	}

	public void setMoveWayptFlag(Boolean moveWayptFlag) {
		this.moveWayptFlag = moveWayptFlag;
	}

	public Boolean getModifyWayptFlag() {
		return modifyWayptFlag;
	}
	
	public void setModifyWayptFlag(Boolean modifyWayptFlag) {
		this.modifyWayptFlag = modifyWayptFlag;
	}
}