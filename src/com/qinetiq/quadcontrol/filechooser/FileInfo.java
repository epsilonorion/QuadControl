package com.qinetiq.quadcontrol.filechooser;

// Class for holding data from item in list.
public class FileInfo implements Comparable<FileInfo> {
	private String name;
	private String data;
	private String path;
	private String lastModified;

	public FileInfo(String name, String date, String path, String lastModified) {
		this.name = name;
		this.data = date;
		this.path = path;
		this.lastModified = lastModified;
	}

	public String getName() {
		return name;
	}

	public String getData() {
		return data;
	}

	public String getPath() {
		return path;
	}

	public String getLastModified() {
		return lastModified;
	}

	@Override
	public int compareTo(FileInfo o) {
		if (this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
		else
			throw new IllegalArgumentException();
	}
}
