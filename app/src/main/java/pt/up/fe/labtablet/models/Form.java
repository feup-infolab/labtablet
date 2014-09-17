package pt.up.fe.labtablet.models;

import java.util.ArrayList;


public class Form {

    private String formName;
    private ArrayList<FormQuestion> formQuestions;

    public Form(String name) {
        formQuestions = new ArrayList<FormQuestion>();
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

}
