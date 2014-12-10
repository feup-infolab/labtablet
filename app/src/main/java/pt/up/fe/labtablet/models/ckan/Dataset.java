package pt.up.fe.labtablet.models.ckan;

import java.util.List;

/**
 * Represents a CKAN Dataset (previously a Package)
 *
 * @author      Ross Jones <ross.jones@okfn.org>
 * @version     1.7
 * @since       2012-05-01
 */
public class Dataset {

    public class Response {
        public boolean success;
        public Dataset result;
    }

    public class SearchResponse {
        public boolean success;
        public SearchResults result;
    }

    /**
     * Represents the results of a search on a CKAN instance, and shows the
     * count and provides a list of dataset objects which match the term.
     *
     * @author      Ross Jones <ross.jones@okfn.org>
     * @version     1.7
     * @since       2012-05-01
     */
    public class SearchResults {
        //{"count": 4, "search_facets": {}, "facets": {}, "results":
        public int count;
        public List<Dataset> results;
    }

    private String id;
    private String name;
    private String title;
    private String revision_id;
    private String maintainer;
    private String maintainer_email;
    private String license_id;
    private String license;
    private String license_title;
    private String license_url;
    private String metadata_created;
    private String metadata_modified;
    private String author;
    private String author_email;
    private String download_url;
    private String state;
    private String version;
    private String type;
    private String notes;
    private String notes_rendered;
    private boolean isopen;
    private List<Tag> tags;
    private List<Group> groups;
    private List<Resource> resources;
    private String url;
    private String ckan_url;
    private List<Extra> extras;

    public Dataset(){}

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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setRevision_id(String revision_id) {
        this.revision_id = revision_id;
    }

    public String getRevision_id() {
        return revision_id;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer_email(String maintainer_email) {
        this.maintainer_email = maintainer_email;
    }

    public String getMaintainer_email() {
        return maintainer_email;
    }

    public void setLicense_id(String license_id) {
        this.license_id = license_id;
    }

    public String getLicense_id() {
        return license_id;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense_title(String license_title) {
        this.license_title = license_title;
    }

    public String getLicense_title() {
        return license_title;
    }

    public void setLicense_url(String license_url) {
        this.license_url = license_url;
    }

    public String getLicense_url() {
        return license_url;
    }

    public void setMetadata_created(String metadata_created) {
        this.metadata_created = metadata_created;
    }

    public String getMetadata_created() {
        return metadata_created;
    }

    public void setMetadata_modified(String metadata_modified) {
        this.metadata_modified = metadata_modified;
    }

    public String getMetadata_modified() {
        return metadata_modified;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor_email(String author_email) {
        this.author_email = author_email;
    }

    public String getAuthor_email() {
        return author_email;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes_rendered(String notes_rendered) {
        this.notes_rendered = notes_rendered;
    }

    public String getNotes_rendered() {
        return notes_rendered;
    }

    public void setIsopen(boolean isopen) {
        this.isopen = isopen;
    }

    public boolean isIsopen() {
        return isopen;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setCkan_url(String ckan_url) {
        this.ckan_url = ckan_url;
    }

    public String getCkan_url() {
        return ckan_url;
    }

    public List<Extra> getExtras() {
        return extras;
    }

    public void setExtras( List<Extra> extras ) {
        this.extras = extras;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources( List<Resource> resources ) {
        this.resources = resources;
    }

    public String toString() {
        return "<Dataset:" + this.getName() + " ," + this.getTitle() + "," + this.getAuthor() + ", " + this.getUrl() + ">";
    }
}






