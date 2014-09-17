package pt.up.fe.labtablet.models;

import java.util.ArrayList;

public class FormQuestion {

    private FormEnumType type;
    private String value;
    private String question;
    private ArrayList<String> allowedValues;
    private ArrayList<Descriptor> metadata;

    public FormQuestion(FormEnumType type, String question, String value) {
        this.type = type;
        this.value = value;
        this.question = question;
        this.metadata = new ArrayList<Descriptor>();
        this.allowedValues = new ArrayList<String>();
    }

    public FormEnumType getType() {
        return type;
    }

    public void setType(FormEnumType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ArrayList<Descriptor> getMetadata() {
        return metadata;
    }

    public void setMetadata(ArrayList<Descriptor> metadata) {
        this.metadata = metadata;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public ArrayList<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(ArrayList<String> admissibleValues) {
        this.allowedValues = admissibleValues;
    }
}
