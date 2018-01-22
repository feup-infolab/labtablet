package pt.up.fe.beta.labtablet.models;

/**
 * Registry for an entry when special events happen in LabTablet
 */
public class ChangelogItem {

    private String message;
    private String title;
    private String date;

    public ChangelogItem() {}

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


}
