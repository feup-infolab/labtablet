package pt.up.fe.alpha.labtablet.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;


public class Form {

    private String formName;
    private String parent;
    private String formDescription;
    private ArrayList<FormQuestion> formQuestions;
    private boolean descriptionSet;
    private String timestamp;

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public Form(String name, String parent) {
        formQuestions = new ArrayList<>();
        this.parent = parent;
        this.formDescription = "";
        this.timestamp = "";
        this.formName = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
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

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Form)) {
            return false;
        }

        Form f = (Form) o;
        return f.getFormName().equals(formName);
    }

    public String[] getQuestions() {
        String[] questionsSet = new String[formQuestions.size()];

        for (int i = 0; i < formQuestions.size(); ++i) {
            questionsSet[i] = formQuestions.get(i).getQuestion();
        }

        return questionsSet;
    }

    public String[] getAnswers() {
        String[] answerSet = new String[formQuestions.size()];
        for (int i = 0; i < formQuestions.size(); ++i) {
            answerSet[i] = formQuestions.get(i).getValue();
        }

        return answerSet;
    }


    @Override
    public String toString() {
        return this.timestamp == null ? "Unknown timestamp" : this.getTimestamp();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
