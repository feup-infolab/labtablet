package pt.up.fe.alpha.labtablet.models;

/**
 * Represents a column in a multi instance response
 */
public class Column {
    private String title;
    private String context;

    public Column(String title, String context) {
        this.title = title;
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return this.getTitle() + (this.getContext().isEmpty() ? "" : " (" + this.getContext() + ")");
    }
}
