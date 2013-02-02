package com.qinetiq.quadcontrol.filechooser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.qinetiq.quadcontrol.MainActivity;
import com.qinetiq.quadcontrol.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileChooserDialog extends DialogFragment implements
		OnClickListener {
	public static String TAG = "FileChooserDialog";

	private ListView fileList;
	private File currentDir;
	private File previousDir;

	private ArrayList<File> forwardDir;
	private FileArrayAdapter adapter;
	private static Context context;

	private TextView txtPath;
	private TextView txtSaveName;

	private String dialogType;
	private String fileType;

	// Listener to be used for calling activity
	public interface FileChooserDialogListener {
		void onFinishFileChooserDialog(String type, String filePath,
				String fileName);
	}

	public static FileChooserDialog newInstance(Context c, Bundle args) {
		context = c;

		FileChooserDialog fileChooser = new FileChooserDialog();
		fileChooser.setArguments(args);
		return fileChooser;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Initialize Current Directory and other variables from bundle arguments
		currentDir = new File(getArguments().getString("startDirectory"));
		dialogType = getArguments().getString("dialogType");
		fileType = getArguments().getString("fileType");
		String title = getArguments().getString("title");

		forwardDir = new ArrayList<File>();
		
		if ((dialogType == null) || (title == null)) {
			Log.e(TAG, "Arguments for type and title are not set");
			dismiss();
		}

		LayoutInflater inflater = LayoutInflater.from(getActivity());

		View v = null;
		Dialog myDialog = null;

		// Choose type of layout based on type argument
		if (dialogType.equals("open")) {
			v = inflater.inflate(R.layout.file_chooser_open_dialog, null);

			myDialog = new AlertDialog.Builder(getActivity())
					.setTitle(title)
					.setView(v)
					.setCancelable(true)
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dismiss();
								}
							}).create();

		} else if (dialogType.equals("save")) {
			v = inflater.inflate(R.layout.file_chooser_save_dialog, null);

			myDialog = new AlertDialog.Builder(getActivity())
					.setTitle(title)
					.setView(v)
					.setCancelable(true)
					.setPositiveButton("Save",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (txtSaveName.getText().toString()
											.equals("")) {
										Toast.makeText(context,
												"Enter the name of the file",
												Toast.LENGTH_SHORT).show();
									} else {
										FileChooserDialogListener activity = (FileChooserDialogListener) getActivity();
										activity.onFinishFileChooserDialog(
												dialogType, currentDir
														.toString(),
												txtSaveName.getText()
														.toString());
										dismiss();
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dismiss();
								}
							}).create();
		}

		if ((v == null) || (myDialog == null)) {
			dismiss();
			Log.e(TAG, "Dialog was not able to create the view or dialog");
		}

		// Create listener for back button
		ImageButton btnBackDirectory = (ImageButton) v
				.findViewById(R.id.btnBackDirectory);
		btnBackDirectory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!currentDir.getName().equalsIgnoreCase("sdcard")) {
					forwardDir.add(currentDir);

					currentDir = previousDir;
					fill(currentDir);
				}
			}
		});

		// Create listener for back button
		ImageButton btnForwardDirectory = (ImageButton) v
				.findViewById(R.id.btnForwardDirectory);
		btnForwardDirectory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!forwardDir.isEmpty()) {
					currentDir = forwardDir.get(forwardDir.size()-1);
					forwardDir.remove(forwardDir.size()-1);
					fill(currentDir);
				}
			}
		});

		// Get copy of txtPath and txtSaveName
		txtPath = (TextView) v.findViewById(R.id.txtDirectoryPath);
		txtSaveName = (TextView) v.findViewById(R.id.txtSaveName);

		// Create Listener and fill data for ListView
		fileList = (ListView) v.findViewById(R.id.fileList);
		fileList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// Get information about item clicked
				FileInfo f = adapter.getItem(position);

				// If item is a folder or parent directory, simply change
				// directory to said component, else take file as option
				// clicked.
				if (f.getData().equalsIgnoreCase("folder")
						|| f.getData().equalsIgnoreCase("parent directory")) {
					currentDir = new File(f.getPath());
					
					// Check if new currentDir is on path of saved forwardDir
					if (!forwardDir.contains(currentDir)) {
						forwardDir.clear();
					}
					
					fill(currentDir);
				} else {
					if (dialogType.equals("save")) {
						// Split and take first part of name before .
						String[] fSplit = f.getName().split("\\.");

						txtSaveName.setText(fSplit[0]);
					} else {
						onFileClick(f);
					}
				}

			}
		});

		// Fill List Activity with current directories items
		fill(currentDir);

		// Hide keyboard on start
		myDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		return myDialog;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	public void fill(File f) {
		File[] dirs = f.listFiles();
		txtPath.setText("Path: " + f.getAbsolutePath());

		// Create list of folders and files
		List<FileInfo> dir = new ArrayList<FileInfo>();
		List<FileInfo> fls = new ArrayList<FileInfo>();
		try {
			for (File ff : dirs) {
				if (ff.isDirectory()) {
					dir.add(new FileInfo(ff.getName(), "Folder", ff
							.getAbsolutePath(), new SimpleDateFormat(
							"yyyy.MM.dd hh:mm aaa").format(ff.lastModified())));
				} else {
					if (fileType.equals("")) { // If no fileType, show all
						fls.add(new FileInfo(ff.getName(), "File Size: "
								+ ff.length(), ff.getAbsolutePath(),
								new SimpleDateFormat("yyyy.MM.dd hh:mm aaa")
										.format(ff.lastModified())));
					} else { // If fileType, only show those with it
						if (ff.getName().endsWith("." + fileType)) {
							fls.add(new FileInfo(
									ff.getName(),
									"File Size: " + ff.length(),
									ff.getAbsolutePath(),
									new SimpleDateFormat("yyyy.MM.dd hh:mm aaa")
											.format(ff.lastModified())));
						}
					}
				}
			}
		} catch (Exception e) {

		}

		// Sort lists of folders and files
		Collections.sort(dir);
		Collections.sort(fls);

		// Merge Lists
		dir.addAll(fls);

		// Add back up to Parent Directory to Top of List
		if (!f.getName().equalsIgnoreCase("sdcard"))
			previousDir = new File(f.getParent());

		// // Add back up to Parent Directory to Top of List
		// if (!f.getName().equalsIgnoreCase("sdcard"))
		// dir.add(0,
		// new FileInfo("..", "Parent Directory", f.getParent(),
		// new SimpleDateFormat("yyyy.MM.dd hh:mm aaa")
		// .format(f.lastModified())));

		// Set List Adapter for ArrayList
		adapter = new FileArrayAdapter(context, R.layout.file_view, dir);
		fileList.setAdapter(adapter);
	}

	// Pass information back to any listener using interface
	private void onFileClick(FileInfo f) {
		// Return input text to activity
		FileChooserDialogListener activity = (FileChooserDialogListener) getActivity();
		activity.onFinishFileChooserDialog(dialogType, currentDir.toString(),
				f.getName());
		this.dismiss();
	}
}
