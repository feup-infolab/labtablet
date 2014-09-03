package pt.up.fe.labtablet.models;

/**
 * Created by ricardo on 19-03-2014.
 */
public class AssociationItem {

    private String fileExtension;
    private String extensionDescription;
    private Descriptor descriptor;

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }
    public Descriptor getDescriptor() { return descriptor;  }
    public void setDescriptor(Descriptor descriptor) { this.descriptor = descriptor; }
    public String getExtensionDescription() {return this.extensionDescription; }
    public void setExtensionDescription(String description) {this.extensionDescription = description;}

}
