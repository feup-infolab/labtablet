package pt.up.fe.labtablet.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;


public class Form {

    private String formName;
    private String parent;
    private String formDescription;
    private String elapsedTime;
    private ArrayList<FormQuestion> formQuestions;
    private boolean descriptionSet;
    private String linkedResourcePath;

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public Form(String name, String parent) {
        formQuestions = new ArrayList<FormQuestion>();
        this.parent = parent;
        this.formDescription = "";
        this.formName = name;
        this.linkedResourcePath = "";
    }


    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getLinkedResourcePath() {
        return linkedResourcePath;
    }

    public void setLinkedResourcePath(String linkedResourcePath) {
        this.linkedResourcePath = linkedResourcePath;
    }

    public void setDescription(String description) {
        this.formDescription = description;
        descriptionSet = true;
    }

    public boolean isDescriptionSet(){return descriptionSet;}

    public String getFormName() {
        return formName;
    }

    public ArrayList<FormQuestion> getFormQuestions() {
        return formQuestions;
    }

    public void setFormQuestions(ArrayList<FormQuestion> formQuestions) {
        this.formQuestions = formQuestions;
    }

    public void addQuestion(FormQuestion fq) {
        this.formQuestions.add(fq);
    }

    public String getDuration() {

        int total = 0;
        for (FormQuestion fq : getFormQuestions()) {
            total += fq.getDuration();
        }

        SimpleDateFormat df = new SimpleDateFormat("mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(total*1000);
    }

    public String getFormDescription() {
        return formDescription;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getElapsedTime() {
        return this.elapsedTime;
    }


    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Form)) {
            return false;
        }

        Form f = (Form) o;
        return f.getFormName().equals(formName);
    }
}
