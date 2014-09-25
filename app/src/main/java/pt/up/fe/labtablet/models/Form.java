package pt.up.fe.labtablet.models;

import java.util.ArrayList;


public class Form {

    private String formName;
    private String formDescription;
    private ArrayList<FormQuestion> formQuestions;

    public Form(String name) {
        formQuestions = new ArrayList<FormQuestion>();
        this.formDescription = "";
        this.formName = name;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
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

    public void setFormDescription(String formDescription) {
        this.formDescription = formDescription;
    }

}
