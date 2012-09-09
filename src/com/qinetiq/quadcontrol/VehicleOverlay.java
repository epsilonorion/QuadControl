package com.qinetiq.quadcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class VehicleOverlay extends Overlay {

	private VehicleStatus vehicleStatusInfo;
	private Context context;
	private final int drawable;

	public VehicleOverlay(VehicleStatus vehicleStatusInfo, Context context, int drawable) {
		this.vehicleStatusInfo = vehicleStatusInfo;
		this.context = context;
		this.drawable = drawable;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		StatusInfo statusInfo = vehicleStatusInfo.getVehicleStatus();
		GeoPoint geoPoint = new GeoPoint((int)(statusInfo.getLatitude() * 1E6), (int)(statusInfo.getLongitude() * 1E6));
		Point screenPoint = new Point();

		mapView.getProjection().toPixels(geoPoint, screenPoint);

		Bitmap vehicleImage = BitmapFactory.decodeResource(
				context.getResources(), drawable);

		int x = screenPoint.x - vehicleImage.getWidth() / 2;
		int y = screenPoint.y - vehicleImage.getHeight();

		canvas.drawBitmap(vehicleImage, screenPoint.x - vehicleImage.getWidth()
				/ 2, screenPoint.y - vehicleImage.getHeight() / 2, null);

	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		// Handle tapping on the overlay here
		return true;
	}

}
