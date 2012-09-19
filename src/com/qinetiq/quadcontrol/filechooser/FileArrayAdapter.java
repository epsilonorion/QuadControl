package com.qinetiq.quadcontrol.filechooser;

import java.util.List;

import com.qinetiq.quadcontrol.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileArrayAdapter extends ArrayAdapter<FileInfo> {
	private Context context;
	private int id;
	private List<FileInfo> items;

	public FileArrayAdapter(Context context, int textViewResourceId,
			List<FileInfo> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.id = textViewResourceId;
		this.items = objects;
	}

	// Return information (option) of item of interest
	public FileInfo getItem(int i) {
		return items.get(i);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		// Inflate Adapter View
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
		
		// Populate view information
		final FileInfo o = items.get(position);
		if (o != null) {
			ImageView image1 = (ImageView) v.findViewById(R.id.fileTypeImage);
			TextView t1 = (TextView) v.findViewById(R.id.txtFileName);
			TextView t2 = (TextView) v.findViewById(R.id.txtFileDate);

			if (image1 != null) {
				if (o.getData().equals("Folder")) {
					image1.setImageResource(R.drawable.icon_folder);
				} else {
					image1.setImageResource(R.drawable.icon_document);
				}
			}
			if (t1 != null)
				t1.setText(o.getName());
			if (t2 != null)
				t2.setText(o.getLastModified());

		}
		return v;
	}

}
