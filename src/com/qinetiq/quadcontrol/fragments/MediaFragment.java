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
import java.lang.ref.WeakReference;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import com.qinetiq.quadcontrol.MainActivity;
import com.qinetiq.quadcontrol.MainApplication;
import com.qinetiq.quadcontrol.R;
import com.qinetiq.quadcontrol.RosImageView;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.Toast;

public class MediaFragment extends Fragment implements SurfaceHolder.Callback {

	View view;
	final Uri MediaBaseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//	private Uri playableUri = null;
	private RosImageView<sensor_msgs.CompressedImage> rosImageView;

	MediaPlayer mediaPlayer;
	SurfaceView videoView;
	SurfaceHolder surfaceHolder;
	ImageView playbackView;

	public ImageView getImageView() {
		return playbackView;
	}

	boolean pausing = false;

	private NodeMainExecutor nodeMainExecutor;
	private NodeConfiguration nodeConfiguration;

	private MainApplication mainApp;
	
	private static int countInstance = 1 ;

	public static MediaFragment newInstance() {
		MediaFragment frag = new MediaFragment();
        Bundle args = new Bundle();
        args.putInt("instanceNumber", countInstance);
        frag.setArguments(args);
        countInstance++ ;
        return frag;
    }

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		this.view = inflater.inflate(R.layout.media_fragment, container, false);

		// Hide rosView and videoView
		rosImageView = (RosImageView<sensor_msgs.CompressedImage>) this.view
				.findViewById(R.id.rosImageView);
		videoView = (SurfaceView) view.findViewById(R.id.videoView);
		playbackView = (ImageView) view.findViewById(R.id.playbackView);
		
		rosImageView.setVisibility(View.GONE);
		videoView.setVisibility(View.GONE);
		playbackView.setVisibility(View.VISIBLE);
		
//		playableUri = Uri.withAppendedPath(MediaBaseUri, "187");
//		Log.d("Video", playableUri.toString());

		
		surfaceHolder = videoView.getHolder();
		surfaceHolder.addCallback(this);
		// surfaceHolder.setFixedSize(176, 144);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// getActivity().getWindow().setFormat(PixelFormat.UNKNOWN);

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

		case R.id.ShowStreamItem:
			showRosView();

			Toast.makeText(getActivity(), "Showing Snapshots",
					Toast.LENGTH_SHORT).show();

			if (mainApp.isConnectedToVehicle()) {
				// Start Node
				nodeMainExecutor = mainApp.getNodeMainExecutor();
				nodeConfiguration = mainApp.getNodeConfiguration();
				nodeMainExecutor.shutdownNodeMain(rosImageView);
				
				rosImageView.setVisibility(View.VISIBLE);

				rosImageView.setTopicName("/image/stream/compressed");
				rosImageView.setMessageType(sensor_msgs.CompressedImage._TYPE);
				rosImageView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
				nodeMainExecutor.execute(rosImageView, nodeConfiguration);
			}

			return true;

		case R.id.LoadVideoItem:
			Toast.makeText(getActivity(), "Loading Video", Toast.LENGTH_SHORT)
					.show();

			showVideoView();

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

	public void showRosView() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			surfaceHolder.removeCallback(this);
		}
		view.findViewById(R.id.playbackView).setVisibility(View.GONE);
		view.findViewById(R.id.videoView).setVisibility(View.GONE);
		rosImageView.setVisibility(View.VISIBLE);
	}

	public void showVideoView() {
		// Kill all other video
		if (rosImageView.isRunning) {
			nodeMainExecutor = mainApp.getNodeMainExecutor();
			nodeMainExecutor.shutdownNodeMain(rosImageView);
		}

		rosImageView.setVisibility(View.GONE);
		view.findViewById(R.id.playbackView).setVisibility(View.GONE);
		
		// Show Surface View
		videoView.setVisibility(View.VISIBLE);
	}
	
	public void showPlaybackView() {
		// Kill all other video
		if (rosImageView.isRunning) {
			nodeMainExecutor = mainApp.getNodeMainExecutor();
			nodeMainExecutor.shutdownNodeMain(rosImageView);
		}
		
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			surfaceHolder.removeCallback(this);
		}

		videoView.setVisibility(View.GONE);
		rosImageView.setVisibility(View.GONE);
		view.findViewById(R.id.playbackView).setVisibility(View.VISIBLE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// mediaPlayer.release();
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

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

	}

	public Handler UIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.getData().getInt("PLAYBACK_IMAGE")) {
			case 1: {
				BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(
						playbackView);
				bitmapWorkerTask.execute(msg.getData().getString("IMAGE_PATH"));
			}
				break;
			}
		};
	};

	private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public BitmapWorkerTask(ImageView playbackView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(playbackView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			Log.d("MediaFragment", "String is /sdcard/QuadControl/playback/"
					+ params[0]);

			String bitmapString = "/sdcard/QuadControl/playback/" + params[0];

			return decodeSampledBitmapFromResource(bitmapString, 200, 200);
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView playbackView = imageViewReference.get();
				if (playbackView != null) {
					playbackView.setImageBitmap(bitmap);
				}
			}
		}

		public Bitmap decodeSampledBitmapFromResource(String bitmapString,
				int reqWidth, int reqHeight) {

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(bitmapString);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(bitmapString, options);
		}

		public int calculateInSampleSize(BitmapFactory.Options options,
				int reqWidth, int reqHeight) {
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {
				if (width > height) {
					inSampleSize = Math.round((float) height
							/ (float) reqHeight);
				} else {
					inSampleSize = Math.round((float) width / (float) reqWidth);
				}
			}
			return inSampleSize;
		}
	}

}
