package pt.up.fe.alpha.labtablet.models.Dendro;


public class DendroConfiguration {
    private String username;
    private String password;
    private String address;
    private boolean validated;

    public DendroConfiguration() {
        this.address = "";
        this.username = "";
        this.password = "";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }
}
