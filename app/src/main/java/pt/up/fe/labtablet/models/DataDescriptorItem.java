package pt.up.fe.labtablet.models;

import java.util.ArrayList;

/**
 * Descriptor to hold metadata for each of the imported data resources
 */
public class DataDescriptorItem {



    private String fileName;
    private String localFilePath;
    private String parent;
    private ArrayList<Descriptor> fileLevelMetadata;
    private String description;
    private String descriptor;
    private String humanReadableSize;
    private String importDate;
    private String mimeType;

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

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        this.importDate = importDate;
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

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
}
