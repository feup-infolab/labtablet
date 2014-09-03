package pt.up.fe.labtablet.models;

import java.util.Date;

import pt.up.fe.labtablet.utils.Utils;

public class Descriptor {

    private String name;
    private String descriptor;
    private String value;
    private String description;
    private String tag;

    private String filePath;
    private long descriptor_id;
    private String dateModified;
    private int state;

    public Descriptor() {
        this.name = "Undefined";
        this.filePath = "";
        this.state = Utils.DESCRIPTOR_STATE_NOT_VALIDATED;
        this.dateModified = Utils.getDate();
        this.descriptor_id = new Date().getTime();
    }

    @Override
    public boolean equals(Object desc) {
        if(! (desc instanceof Descriptor) )
            return false;

        if (desc == null)
            return false;

        return this.descriptor_id == ((Descriptor)desc).getID();
    }

    public long getID() {
        return this.descriptor_id;
    }

    public boolean hasFile() {
        return !this.filePath.equals("");
    }


    public Descriptor(String name, String descriptor, String value, String tag) {
        this.name = name;
        this.descriptor = descriptor;
        this.value = value;
        this.tag = tag;
        this.state = Utils.DESCRIPTOR_STATE_NOT_VALIDATED;
    }

    public String getTag() {return this.tag;}

    public void setTag(String tag) {this.tag = tag;}

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

    public String getDescriptor() { return descriptor; }

    public void setDescriptor(String descriptor) { this.descriptor = descriptor; }

    public String getDescription() { return this.description; }

    public void setDescription(String description) {this.description = description; }

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

    public String getFilePath() {  return filePath; }

    public void setFilePath(String filePath) {  this.filePath = filePath; }
}
