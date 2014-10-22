package pt.up.fe.labtablet.models;

/**
 * Descriptor to hold metadata for each of the imported data resources
 */
public class DataDescriptorItem {

    private String filePath;
    private String description;
    private String descriptor;
    private String humanReadableSize;
    private String importDate;
    private String mimeType;

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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
}
