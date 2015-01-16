package pt.up.fe.labtablet.models;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.application.LabTablet;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Instance of a descriptor. Descriptors are used to provide context of production to a specific
 * favorite (dataset) they are designed to hold any resource from simple text to files and media
 * resources
 */
public class Descriptor {

    private String name;
    private String descriptor;
    private String value;
    private String description;
    private String tag;

    private ArrayList<String> allowed_values;

    private String filePath;
    private String dateModified;
    private int state;

    //when applicable
    private String size;

    public Descriptor() {
        this.name = LabTablet.getContext().getString(R.string.undefined);
        this.filePath = "";
        this.state = Utils.DESCRIPTOR_STATE_NOT_VALIDATED;
        this.dateModified = Utils.getDate();
        this.size = "";
        this.allowed_values = new ArrayList<>();
    }

    public Descriptor(String name, String descriptor, String value, String tag) {
        this.name = name;
        this.descriptor = descriptor;
        this.value = value;
        this.tag = tag;
        this.state = Utils.DESCRIPTOR_STATE_NOT_VALIDATED;
        this.allowed_values = new ArrayList<>();
    }

    public boolean hasFile() {
        return !this.filePath.equals("");
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescription() {
        return this.description == null ? "" : this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void validate() {
        this.state = Utils.DESCRIPTOR_STATE_VALIDATED;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public ArrayList<String> getAllowed_values() {
        return allowed_values;
    }

    public void setAllowed_values(ArrayList<String> allowed_values) {
        this.allowed_values = allowed_values;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        File f = new File(filePath);
        this.size = FileMgr.humanReadableByteCount(f.length(), true);

    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Descriptor)) {
            return false;
        }

        //Compare by the file path in the first place
        Descriptor desc = (Descriptor) o;
        if (!desc.getFilePath().equals("")) {
            return desc.getFilePath().equals(this.getFilePath());
        } else {
            return desc.getValue().equals(this.value);
        }

    }


}
