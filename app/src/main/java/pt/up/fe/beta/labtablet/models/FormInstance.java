package pt.up.fe.beta.labtablet.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class FormInstance {

    private String instanceTimestamp;
    private String parent;
    private ArrayList<FormQuestion> formQuestions;

    public FormInstance(Form templateForm) {
        this.parent = templateForm.getFormName();
        this.formQuestions = templateForm.getFormQuestions();

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd, HH:mm",
                Locale.getDefault());

        this.instanceTimestamp = sdfDate.format(new Date());
    }
    @Override
    public String toString() {
        return this.instanceTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FormInstance && o.toString().equals(this.toString());
    }

    public String getInstanceTimestamp() {
        return instanceTimestamp;
    }

    public void setInstanceTimestamp(String instanceTimestamp) {
        this.instanceTimestamp = instanceTimestamp;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public ArrayList<FormQuestion> getFormQuestions() {
        return formQuestions;
    }

    public void setFormQuestions(ArrayList<FormQuestion> formQuestions) {
        this.formQuestions = formQuestions;
    }
}
