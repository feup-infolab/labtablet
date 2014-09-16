package pt.up.fe.labtablet.models;

public class FavoriteItem {
    private String title;
    private String size;
    private String description;
    private String path;
    private String date_modified;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(String last_modified) {
        this.date_modified = last_modified;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
