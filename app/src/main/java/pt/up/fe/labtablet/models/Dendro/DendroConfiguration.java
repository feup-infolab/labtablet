package pt.up.fe.labtablet.models.Dendro;

/**
 * Created by ricardo on 14-05-2014.
 */
public class DendroConfiguration {
    public String username;
    public String password;
    public String address;

    public boolean validated;

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

    public boolean getValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }
}
