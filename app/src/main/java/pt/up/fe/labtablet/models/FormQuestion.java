package pt.up.fe.labtablet.models;

import java.util.ArrayList;

public class FormQuestion {

    private FormEnumType type;
    private String value;
    private String question;
    private boolean mandatory;

    private int duration;
    private ArrayList<String> allowedValues;

    public FormQuestion(FormEnumType type, String question, ArrayList<String> allowedValues) {
        this.type = type;
        this.question = question;
        this.allowedValues = allowedValues;
        this.value = "";
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

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getAllowedValues() {
        return allowedValues;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
}
