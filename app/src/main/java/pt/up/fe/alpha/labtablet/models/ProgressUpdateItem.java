package pt.up.fe.alpha.labtablet.models;

/**
 * Item to represent a progress info
 */
public class ProgressUpdateItem {


    private int progress;
    private String message;

    public ProgressUpdateItem(int progress, String message) {
        this.progress = progress;
        this.message = message;
    }

    public int getProgress() {
        return progress;
    }

    public String getMessage() {
        return message;
    }
}
