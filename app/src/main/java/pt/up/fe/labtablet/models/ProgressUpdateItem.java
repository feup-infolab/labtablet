package pt.up.fe.labtablet.models;

/**
 * Created by ricardo on 10/22/14.
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

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
