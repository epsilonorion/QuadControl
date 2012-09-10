package com.qinetiq.quadcontrol;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class RouteOverlay extends Overlay {
	private WaypointList wayptList;
	private Paint paint;

	public RouteOverlay(WaypointList wayptList) {
		this.wayptList = wayptList;
		this.paint = new Paint();
	}

	@Override
	public void draw(Canvas canvas, MapView mapview, boolean shadow) {
		super.draw(canvas, mapview, shadow);

		Drawable d;
		paint.setAntiAlias(true);

		// Draw route segment by segment, setting color and width of segment
		// according to the slope
		// information returned from the server for the route.
		GeoPoint geoPt1;
		GeoPoint geoPt2;
		Point pt1 = new Point(0, 0);
		Point pt2 = new Point(0, 0);

		if (wayptList.size() > 1) {
			for (int i = 0; i < wayptList.size() - 1; i++) {
				paint.setARGB(50, 0, 0, 255);
				paint.setStrokeWidth(6);

				// Get each points endpoints in pixels to be drawn to and from
				geoPt1 = new GeoPoint(
						(int) (wayptList.get(i).getLatitude() * 1e6),
						(int) (wayptList.get(i).getLongitude() * 1e6));

				geoPt2 = new GeoPoint(
						(int) (wayptList.get(i + 1).getLatitude() * 1e6),
						(int) (wayptList.get(i + 1).getLongitude() * 1e6));

				mapview.getProjection().toPixels(geoPt1, pt1);
				mapview.getProjection().toPixels(geoPt2, pt2);

				// Draw the segment
				canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, paint);
				
//				// Draw arrowHead
//				int width = 40;
//				int height = 60;
//				double arrowAngle = Math.atan2(pt2.y-pt1.y, pt2.x - pt1.x) * 180.0 / Math.PI; 
//				Path arrowPath = new Path();
//				arrowPath.reset();
//				arrowPath.moveTo(pt2.x, pt2.y);
//
//		        // draw outline of arrow
//		        paint.setStyle(Paint.Style.FILL);
//		        arrowPath.lineTo(pt2.x + 20, pt2.y + 50);
//		        arrowPath.lineTo(pt2.x - 20, pt2.y + 50);
//		        arrowPath.lineTo(pt2.x, pt2.y);
//		        
////		        Log.d("ANGLE", "Tangent is " + Math.atan2(pt2.y-pt1.y, pt2.x - pt1.x));
////		        Log.d("ANGLE", "Angle is " + arrowAngle);
//		        
//		        canvas.save(Canvas.MATRIX_SAVE_FLAG);
//		        canvas.rotate((float)(arrowAngle), pt2.x, pt2.y);
//				canvas.drawPath(arrowPath, paint);
//				canvas.restore();

			}
		}
	}
}
