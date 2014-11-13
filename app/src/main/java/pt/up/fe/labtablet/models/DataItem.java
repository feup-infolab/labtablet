package pt.up.fe.labtablet.models;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.utils.Utils;

/**
 * Item to hold metadata, as well as the path of the imported file, for each of the imported data resources
 */
public class DataItem {

    private String resourceName;
    private String localPath;
    private String parent;
    private String humanReadableSize;
    private String mimeType;
    private ArrayList<Descriptor> fileLevelMetadata;

    public String getResourceName() {
        return new File(getLocalPath()).getName();
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
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
        for (Descriptor desc : fileLevelMetadata) {
            if (desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                return desc.getValue();
            }

        }
        return "";
    }



    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String filePath) {
        this.localPath = filePath.toLowerCase();
    }

    public void setDescription(String description) {
        for (Descriptor desc : fileLevelMetadata) {
            if (desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                desc.setValue(description);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DataItem)) {
            return false;
        }

        return ((DataItem) o).getLocalPath().equals(this.localPath);
    }

}
