
package pt.up.fe.beta.labtablet.models.Dendro.Ontologies;

public class Dcterms {
    private Object creator;
    private Object description;
    private Object subject;
    private Object title;
    private Object modified;

    public Object getModified() {
        return this.modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public Object getCreator() {
        return this.creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Object getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Object getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
