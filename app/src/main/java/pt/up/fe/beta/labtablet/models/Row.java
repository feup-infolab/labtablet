package pt.up.fe.beta.labtablet.models;

/**
 * A form question row (when FQType = multi row response)
 */
public class Row {

    private String value;

    public Row(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String header) {
        this.value = header;
    }

}
