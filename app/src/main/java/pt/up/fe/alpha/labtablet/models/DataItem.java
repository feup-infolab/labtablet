package pt.up.fe.alpha.labtablet.models;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Item to hold metadata, as well as the path of the imported file, for each of the imported data resources
 */
public class DataItem {

    private String localPath;
    private String humanReadableSize;
    private String mimeType;
    private ArrayList<Descriptor> fileLevelMetadata;

    public String getResourceName() {
        return new File(getLocalPath()).getName();
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

        if (fileLevelMetadata == null || fileLevelMetadata.size() == 0) {
         return;
        }

        for (Descriptor desc : fileLevelMetadata) {
            if (desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                desc.setValue(description);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DataItem
                && ((DataItem) o).getLocalPath().equals(this.localPath);

    }

}
