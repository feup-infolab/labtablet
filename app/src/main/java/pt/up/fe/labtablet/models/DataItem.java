package pt.up.fe.labtablet.models;

import java.util.ArrayList;

/**
 * Item to hold metadata, as well as the path of the imported file, for each of the imported data resources
 */
public class DataItem {

    private String fileName;
    private String localFilePath;
    private String parent;
    private String description;
    private String humanReadableSize;
    private String mimeType;

    private ArrayList<Descriptor> fileLevelMetadata;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public ArrayList<Descriptor> getFileLevelMetadata() {
        return fileLevelMetadata;
    }

    public void setFileLevelMetadata(ArrayList<Descriptor> fileLevelMetadata) {
        this.fileLevelMetadata = fileLevelMetadata;
    }
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getHumanReadableSize() {
        return humanReadableSize;
    }

    public void setHumanReadableSize(String humanReadableSize) {
        this.humanReadableSize = humanReadableSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String filePath) {
        this.localFilePath = filePath;
    }

}
