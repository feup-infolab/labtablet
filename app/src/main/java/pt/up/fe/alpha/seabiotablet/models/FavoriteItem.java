package pt.up.fe.alpha.seabiotablet.models;

import java.util.ArrayList;

import pt.up.fe.alpha.seabiotablet.utils.Utils;

public class FavoriteItem {
    private String title;

    private String size;
    private String path;
    private String date_modified;
    private ArrayList<DataItem> dataItems;
    private ArrayList<Descriptor> metadataItems;

    private ArrayList<Descriptor> metadataRecommendations;
    private ArrayList<FormInstance> linkedForms;


    public FavoriteItem(String title) {
        this.title = title;
        this.dataItems = new ArrayList<>();
        this.metadataItems = new ArrayList<>();
        this.linkedForms = new ArrayList<>();
        this.metadataRecommendations = new ArrayList<>();
    }

    public void setMetadataRecommendations(ArrayList<Descriptor> metadataRecommendations) {
        this.metadataRecommendations = metadataRecommendations;
    }

    public ArrayList<FormInstance> getLinkedForms() {
        return linkedForms;
    }

    public ArrayList<DataItem> getDataItems() {
        return dataItems;
    }

    public void setDataItems(ArrayList<DataItem> dataItems) {
        this.dataItems = dataItems;
    }

    public ArrayList<Descriptor> getMetadataItems() {
        return metadataItems;
    }

    public void setMetadataItems(ArrayList<Descriptor> metadataItems) {
        this.metadataItems = metadataItems;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(String last_modified) {
        this.date_modified = last_modified;
    }

    public void addMetadataItem(Descriptor newDescriptor) {
        this.metadataItems.add(newDescriptor);
    }

    public void addDataItem(DataItem newDataItem) {
        this.dataItems.add(newDataItem);
    }

    public void addFormInstance(FormInstance form) {

        if (linkedForms.contains(form)) {
            linkedForms.remove(form);
        }

        linkedForms.add(form);
    }

    public String getDescription() {
        for (Descriptor desc : metadataItems) {
            if (desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                return desc.getValue();
            }
        }
        return "";
    }
}
