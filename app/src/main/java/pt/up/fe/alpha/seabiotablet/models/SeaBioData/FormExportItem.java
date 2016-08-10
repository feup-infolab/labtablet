package pt.up.fe.alpha.seabiotablet.models.SeaBioData;

import java.util.ArrayList;

import pt.up.fe.alpha.seabiotablet.models.FormInstance;

/**
 *
 */
public class FormExportItem {
    private ArrayList<FormInstance> forms;
    private String authorship;

    public FormExportItem(ArrayList<FormInstance> forms, String authorship) {
        this.forms = forms;
        this.authorship = authorship;
    }

    public ArrayList<FormInstance> getForms() {
        return forms;
    }

    public void setForms(ArrayList<FormInstance> forms) {
        this.forms = forms;
    }

    public String getAuthorship() {
        return authorship;
    }

    public void setAuthorship(String authorship) {
        this.authorship = authorship;
    }
}
