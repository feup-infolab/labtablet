
package pt.up.fe.alpha.labtablet.models.Dendro.Ontologies;

public class Ddr {
    private Object handle;
    private Object rootFolder;
    private Object fileExtension;

    public Object getFileExtension() {
        return this.fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Object getHandle() {
        return this.handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public Object getRootFolder() {
        return this.rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }
}
