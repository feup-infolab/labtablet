package pt.up.fe.labtablet.models.ckan;

import java.util.List;

/**
 * Represents a CKAN group
 *
 * @author      Ross Jones <ross.jones@okfn.org>
 * @version     1.7
 * @since       2012-05-01
 */
public class Group {

    public class Response {
        public boolean success;
        public Group result;
    }


    private String id;
    private String name;
    private String capacity;
    private String title;
    private String type;
    private String description;
    private String image_url;
    private String created;
    private String approval_status;
    private List<Package> packages;
    private List<Extra> extras;

    public Group() {}

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCreated() {
        return created;
    }

    public void setApproval_status(String approval_status) {
        this.approval_status = approval_status;
    }

    public String getApproval_status() {
        return approval_status;
    }

    public void setExtras(List<Extra> extras) {
        this.extras = extras;
    }

    public List<Extra> getExtras() {
        return extras;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public String toString() {
        return "<Group: " + this.getName() + ", " + this.getTitle() + "  (" + this.getType()+ ")>";
    }

}






