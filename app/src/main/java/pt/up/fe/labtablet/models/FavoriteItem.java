package pt.up.fe.labtablet.models;

import java.util.ArrayList;

public class FavoriteItem {
	private String title;
	private String size;
	private String description;
	private String path;
	private ArrayList<String> authors;
	private String date_modified;
    private ArrayList<Descriptor> descriptors;


	public ArrayList<String> getAuthors() { return this.authors; }
	public String getTitle() { return title; }
	public String getSize() { return size; }
	public String getPath() { return path; }
	public String getDate_modified() { return date_modified; }
	public String getDescription() { return this.description; }


    public void setAuthors(ArrayList<String> authors) { this.authors = authors; }
	public void setSize(String size) { this.size = size; }
	public void setTitle(String title) { this.title = title; }
	public void setPath(String path) { this.path = path; }
	public void setDate_modified(String last_modified) { this.date_modified = last_modified; }
	public void setDescription(String description) { this.description = description; }
    public ArrayList<Descriptor> getDescriptors() {  return descriptors; }
    public void setDescriptors(ArrayList<Descriptor> descriptors) { this.descriptors = descriptors; }
}
