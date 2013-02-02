package com.qinetiq.quadcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class VehicleOverlay extends Overlay {

	private VehicleStatus vehicleStatusInfo;
	private Context context;
	private final int drawable;
	MainApplication mainApp;

	public VehicleOverlay(VehicleStatus vehicleStatusInfo, Context context,
			int drawable) {
		this.vehicleStatusInfo = vehicleStatusInfo;
		this.context = context;
		this.drawable = drawable;
		this.mainApp = (MainApplication)this.context.getApplicationContext();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		if (mainApp.isConnectedToVehicle() || mainApp.isInPlayback()) {
			//Log.d("Test", "Drawing Quad");
			
			StatusInfo statusInfo = vehicleStatusInfo.getVehicleStatus();
			
			GeoPoint geoPoint = new GeoPoint(
					(int) (statusInfo.getLatitude() * 1E6),
					(int) (statusInfo.getLongitude() * 1E6));
			Point screenPoint = new Point();

			//Log.d("Test", "Lat = " + geoPoint.getLatitudeE6() + " Long = " + geoPoint.getLongitudeE6());
			
			mapView.getProjection().toPixels(geoPoint, screenPoint);

			Bitmap vehicleImage = BitmapFactory.decodeResource(
					context.getResources(), drawable);

			int x = screenPoint.x - vehicleImage.getWidth() / 2;
			int y = screenPoint.y - vehicleImage.getHeight();

			canvas.drawBitmap(vehicleImage,
					screenPoint.x - vehicleImage.getWidth() / 2, screenPoint.y
							- vehicleImage.getHeight() / 2, null);
		}

	}
}
