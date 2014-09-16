package pt.up.fe.labtablet.models;

import java.sql.Timestamp;
import java.util.Date;

public class ChangelogItem {

    private String message;
    private String title;
    private String date;
    private String id;

    public ChangelogItem() {
        this.id = new Timestamp(new Date().getTime()).toString();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getID() {
        return this.id;
    }


    @Override
    public boolean equals(Object obj) {
        //well this is a syntax suggestion from the android lint #yolo
        return obj != null &&
                (obj == this || obj instanceof ChangelogItem && ((ChangelogItem) obj).getID().equals(this.id));

    }
}
