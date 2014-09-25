package pt.up.fe.labtablet.models;

import java.util.ArrayList;


public class Form {

    private String formName;
    private String formDescription;
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

    public int getDuration() {

        int total = 0;
        for (FormQuestion fq : getFormQuestions()) {
            total += fq.getDuration();
        }
        return total;
    }

    public String getFormDescription() {
        return formDescription;
    }
}
