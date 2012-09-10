/**MediaFragment.java*********************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 26, 2012
 *      Purpose : Fragment for controlling and displaying media.  The media is
 *      		  setup to play standard videos at the moment, but will evovle
 *      		  to play ROS videos and such.
 *    Call Path : MainActivity->MediaFragment
 * Dependencies : 
 *          XML : res->layout->media_fragment
 ****************************************************************************/

package com.qinetiq.quadcontrol.fragments;

import java.io.IOException;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import com.qinetiq.quadcontrol.MainActivity;
import com.qinetiq.quadcontrol.MainApplication;
import com.qinetiq.quadcontrol.R;
import com.qinetiq.quadcontrol.RosImageView;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MediaFragment extends Fragment implements SurfaceHolder.Callback {

	View view;
	final Uri MediaBaseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	private Uri playableUri = null;
	private RosImageView<sensor_msgs.CompressedImage> image;

	MediaPlayer mediaPlayer;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean pausing = false;

	private NodeMainExecutor nodeMainExecutor;
	private NodeConfiguration nodeConfiguration;

	private MainApplication mainApp;

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		this.view = inflater.inflate(R.layout.media_fragment, container, false);

		view.findViewById(R.id.rosview).setVisibility(View.GONE);

		playableUri = Uri.withAppendedPath(MediaBaseUri, "187");
		Log.d("Video", playableUri.toString());

		surfaceView = (SurfaceView) view.findViewById(R.id.surfaceview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		// surfaceHolder.setFixedSize(176, 144);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// getActivity().getWindow().setFormat(PixelFormat.UNKNOWN);

		image = (RosImageView<sensor_msgs.CompressedImage>) this.view
				.findViewById(R.id.rosview);

		mainApp = (MainApplication) getActivity().getApplicationContext();

		return this.view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.media_fragment_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		MainActivity mainActivity;
		mainActivity = (MainActivity) getActivity();
		switch (item.getItemId()) {

		case R.id.ShowSnapshotsItem:
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
				surfaceHolder.removeCallback(this);
			}
			view.findViewById(R.id.surfaceview).setVisibility(View.GONE);

			Toast.makeText(getActivity(), "Showing Snapshots",
					Toast.LENGTH_SHORT).show();

			if (mainApp.isConnectedToVehicle()) {
				nodeMainExecutor = mainApp.getNodeMainExecutor();
				nodeConfiguration = mainApp.getNodeConfiguration();
				nodeMainExecutor.shutdownNodeMain(image);
				image.setVisibility(View.VISIBLE);

				image.setTopicName("/camera/image_raw_throttled/compressed");
				image.setMessageType(sensor_msgs.CompressedImage._TYPE);
				image.setMessageToBitmapCallable(new BitmapFromCompressedImage());
				nodeMainExecutor.execute(image, nodeConfiguration);
			}

			return true;

		case R.id.Show320VideoItem:
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
				surfaceHolder.removeCallback(this);
			}
			view.findViewById(R.id.surfaceview).setVisibility(View.GONE);

			Toast.makeText(getActivity(), "Showing 320x240 Video Stream",
					Toast.LENGTH_SHORT).show();

			if (mainApp.isConnectedToVehicle()) {
				nodeMainExecutor = mainApp.getNodeMainExecutor();
				nodeConfiguration = mainApp.getNodeConfiguration();
				nodeMainExecutor.shutdownNodeMain(image);
				image.setVisibility(View.VISIBLE);

				image.setTopicName("/camera_out/image_raw_320x240/compressed");
				image.setMessageType(sensor_msgs.CompressedImage._TYPE);
				image.setMessageToBitmapCallable(new BitmapFromCompressedImage());
				nodeMainExecutor.execute(image, nodeConfiguration);
			}
			return true;

		case R.id.Show640VideoItem:
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
				surfaceHolder.removeCallback(this);
			}
			view.findViewById(R.id.surfaceview).setVisibility(View.GONE);

			Toast.makeText(getActivity(), "Showing 640x480 Video Stream",
					Toast.LENGTH_SHORT).show();

			if (mainApp.isConnectedToVehicle()) {
				nodeMainExecutor = mainApp.getNodeMainExecutor();
				nodeConfiguration = mainApp.getNodeConfiguration();
				nodeMainExecutor.shutdownNodeMain(image);
				image.setVisibility(View.VISIBLE);

				image.setTopicName("/camera_out/image_raw_640x480/compressed");
				image.setMessageType(sensor_msgs.CompressedImage._TYPE);
				image.setMessageToBitmapCallable(new BitmapFromCompressedImage());
				nodeMainExecutor.execute(image, nodeConfiguration);
			}
			return true;

		case R.id.LoadVideoItem:
			Toast.makeText(getActivity(), "Loading Video", Toast.LENGTH_SHORT)
					.show();

			// Kill all other video
			nodeMainExecutor = mainApp.getNodeMainExecutor();
			nodeMainExecutor.shutdownNodeMain(image);
			image.setVisibility(View.GONE);

			// Show Surface View
			surfaceView.setVisibility(View.VISIBLE);

			mediaPlayer = new MediaPlayer();

			// Start MediaPlayer
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.reset();
			}

			mediaPlayer.setAudioStreamType(2);
			mediaPlayer.setDisplay(surfaceHolder);

			try {
				// mediaPlayer.setDataSource("http://daily3gp.com/vids/747.3gp");
				// mediaPlayer.setDataSource("rtsp://v7.cache3.c.youtube.com/CjYLENy73wIaLQn3sbZMDCvMWxMYDSANFEIJbXYtZ29vZ2xlSARSBXdhdGNoYJLggLzG14qEUAw=/0/0/0/video.3gp");
				// mediaPlayer.setDataSource("rtsp://v1.cache4.c.youtube.com/CjYLENy73wIaLQmEq1oCZGdlBRMYDSANFEIJbXYtZ29vZ2xlSARSBXdhdGNoYJLggLzG14qEUAw=/0/0/0/video.3gp");
				// mediaPlayer.setDataSource("rtsp://v7.cache3.c.youtube.com/CjgLENy73wIaLwli69KYhrNlQRMYDSANFEIJbXYtZ29vZ2xlSARSB3JlbGF0ZWRg-Jei-PLGndxPDA==/0/0/0/video.3gp");
				// mediaPlayer.setDataSource(getActivity().getApplicationContext(),
				// Uri.parse("rtsp://v7.cache3.c.youtube.com/CjgLENy73wIaLwli69KYhrNlQRMYDSANFEIJbXYtZ29vZ2xlSARSB3JlbGF0ZWRg-Jei-PLGndxPDA==/0/0/0/video.3gp"));

				mediaPlayer.setDataSource("/mnt/sdcard/Movies/video.mp4");
				mediaPlayer.prepare();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG)
						.show();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG)
						.show();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG)
						.show();
			}

			mediaPlayer.start();

			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// mediaPlayer.release();
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	public void surfaceCreated(SurfaceHolder arg0) {
		/*
		 * Handle aspect ratio
		 */
		// int surfaceView_Width = surfaceView.getWidth();
		// int surfaceView_Height = surfaceView.getHeight();
		//
		// float video_Width = mediaPlayer.getVideoWidth();
		// float video_Height = mediaPlayer.getVideoHeight();
		//
		// float ratio_width = surfaceView_Width / video_Width;
		// float ratio_height = surfaceView_Height / video_Height;
		// float aspectratio = video_Width / video_Height;
		//
		// LayoutParams layoutParams = surfaceView.getLayoutParams();
		//
		// if (ratio_width > ratio_height) {
		// layoutParams.width = (int) (surfaceView_Height * aspectratio);
		// layoutParams.height = surfaceView_Height;
		// } else {
		// layoutParams.width = surfaceView_Width;
		// layoutParams.height = (int) (surfaceView_Width / aspectratio);
		// }
		//
		// surfaceView.setLayoutParams(layoutParams);
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

}