package pt.up.fe.labtablet.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;


public class Form {

    private final String formName;
    private String formDescription;
    private String elapsedTime;
    private ArrayList<FormQuestion> formQuestions;
    private boolean descriptionSet;

    public Form(String name) {
        formQuestions = new ArrayList<FormQuestion>();
        this.formDescription = "";
        this.formName = name;
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
}
