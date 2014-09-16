package pt.up.fe.labtablet.models;

import java.io.File;

import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

public class Descriptor {

    private String name;
    private String descriptor;
    private String value;
    private String description;
    private String tag;

    private String filePath;
    private String dateModified;
    private int state;

    //when applicable
    private String size;

    public Descriptor() {
        this.name = "Undefined";
        this.filePath = "";
        this.state = Utils.DESCRIPTOR_STATE_NOT_VALIDATED;
        this.dateModified = Utils.getDate();
        this.size = "";
    }

    public Descriptor(String name, String descriptor, String value, String tag) {
        this.name = name;
        this.descriptor = descriptor;
        this.value = value;
        this.tag = tag;
        this.state = Utils.DESCRIPTOR_STATE_NOT_VALIDATED;
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
        return this.description;
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
}
