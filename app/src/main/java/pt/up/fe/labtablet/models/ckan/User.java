package pt.up.fe.labtablet.models.ckan;


/**
 * Represents a CKAN User
 *
 * @author      Ross Jones <ross.jones@okfn.org>
 * @version     1.7
 * @since       2012-05-01
 */
public class User {

    private String id;
    private String name;
    private String fullname;
    private String email;
    private String created;
    private String about;
    
    public User() {}
    
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

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getFullname() {
        return fullname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCreated() {
        return created;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getAbout() {
        return about;
    }

}






