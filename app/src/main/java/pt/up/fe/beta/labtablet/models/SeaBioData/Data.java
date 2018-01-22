package pt.up.fe.beta.labtablet.models.SeaBioData;

/**
 * Created by ricardo on 05-07-2016.
 */
public class Data {
    private String id;
    private String code;
    private String name;

    public Data(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
