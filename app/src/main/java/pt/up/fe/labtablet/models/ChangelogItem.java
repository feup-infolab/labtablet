package pt.up.fe.labtablet.models;

import java.sql.Timestamp;
import java.util.Date;

public class ChangelogItem {
	
	private String type;
	private String message;
    private String title;
	private String date;
    private String id;
	private boolean read;

    public ChangelogItem() {
        this.id = new Timestamp(new Date().getTime()).toString();
    }
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getImg_url() {
		return type;
	}
	public void setImg_url(String img_url) {
		this.type = img_url;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getID(){return this.id;}


    @Override
    public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (!(obj instanceof ChangelogItem))
                return false;

        return ((ChangelogItem) obj).getID().equals(this.id);
    }
}
