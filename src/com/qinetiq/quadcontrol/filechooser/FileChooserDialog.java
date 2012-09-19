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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileChooserDialog extends DialogFragment implements
		android.view.View.OnClickListener {
	public static String TAG = "FileChooserDialog";

	private Button okButton;
	private ListView fileList;
	private File currentDir;
	private FileArrayAdapter adapter;
	private static Context context;

	private TextView txtPath;

	public static FileChooserDialog openInstance(Context c) {
		context = c;

		String title = "Open CSV File";

		FileChooserDialog f = new FileChooserDialog();
		Bundle args = new Bundle();
		args.putString("title", title);
		f.setArguments(args);
		return f;
	}

	public static FileChooserDialog saveInstance(Context c) {
		context = c;

		String title = "Save CSV File";

		FileChooserDialog f = new FileChooserDialog();
		Bundle args = new Bundle();
		args.putString("title", title);
		f.setArguments(args);
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View v = inflater.inflate(R.layout.file_chooser_dialog, null);

		// Get copy of txtPath
		txtPath = (TextView) v.findViewById(R.id.txtDirectoryPath);

		Dialog myDialog = new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setView(v)
				.setCancelable(true)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Toast.makeText(context, "TEXT",
										Toast.LENGTH_SHORT).show();
								dismiss();
							}
						}).create();

		fileList = (ListView) v.findViewById(R.id.fileList);
		fileList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// Get information about item clicked
				FileInfo o = adapter.getItem(position);

				// If item is a folder or parent directory, simply changed
				// directory to
				// said component, else take file as option clicked.
				if (o.getData().equalsIgnoreCase("folder")
						|| o.getData().equalsIgnoreCase("parent directory")) {
					currentDir = new File(o.getPath());
					fill(currentDir);
				} else {
					onFileClick(o);
				}

			}
		});

		// Initialize Current Directory
		currentDir = new File("/sdcard/");

		// Fill List Activity with current directories items
		fill(currentDir);

		return myDialog;
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
				if (ff.isDirectory())
					dir.add(new FileInfo(ff.getName(), "Folder", ff
							.getAbsolutePath(), new SimpleDateFormat(
							"yyyy.MM.dd hh:mm aaa").format(ff.lastModified())));
				else {
					fls.add(new FileInfo(ff.getName(), "File Size: "
							+ ff.length(), ff.getAbsolutePath(),
							new SimpleDateFormat("yyyy.MM.dd hh:mm aaa")
									.format(ff.lastModified())));
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
			dir.add(0,
					new FileInfo("..", "Parent Directory", f.getParent(),
							new SimpleDateFormat("yyyy.MM.dd hh:mm aaa")
									.format(f.lastModified())));

		// Set List Adapter for ArrayList
		adapter = new FileArrayAdapter(context, R.layout.file_view, dir);
		fileList.setAdapter(adapter);
	}

	private void onFileClick(FileInfo o) {
		((MainActivity) context).getFilePath(o.getPath());
	}
}
