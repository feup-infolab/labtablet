package pt.up.fe.alpha.labtablet.models;

import java.util.ArrayList;

public class FormQuestion {

    private FormEnumType type;
    private String value;
    private final String question;
    private boolean mandatory;
    private int duration;


    private ArrayList<Column> columns;
    private ArrayList<ArrayList<String>> rows;
    private final ArrayList<String> allowedValues;

    public FormQuestion(FormEnumType type, String question, ArrayList<String> allowedValues) {
        this.type = type;
        this.question = question;
        this.allowedValues = allowedValues;
        this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.value = "";
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

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<Column> columns) {
        this.columns = columns;
    }

    public ArrayList<ArrayList<String>> getRows() {
        return rows == null ? new ArrayList<ArrayList<String>>() : this.rows;
    }

    public void setRows(ArrayList<ArrayList<String>> rows) {
        this.rows = rows;
    }

    public void addRow(ArrayList<String> row) {
        if (this.rows == null)
            this.rows = new ArrayList<>();

        this.rows.add(row);
    }
}
