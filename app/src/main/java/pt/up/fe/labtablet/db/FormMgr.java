package pt.up.fe.labtablet.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.models.FormQuestion;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Handles access to the DB for form items management
 * (either base forms or filled ones)
 */
public class FormMgr {

    /**
     * Update a record in the DB
     * @param inForm
     * @param mContext
     */
    public static void updateForm(Form inForm, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains("forms")) {
            Log.e("OVERWRITE", "Entry was not found for folder ");
            Toast.makeText(mContext, "Error updating form", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Form> savedForms = new Gson().fromJson(
                settings.getString("forms", ""),
                Utils.ARRAY_FORM
        );

        for (Form f : savedForms) {
            if (f.getFormName().equals(inForm.getFormName())) {
                f.setFormQuestions(inForm.getFormQuestions());
                f.setDescription(inForm.getFormDescription());
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.remove("forms");
        editor.putString("forms", new Gson().toJson(savedForms));
        editor.apply();
    }

    /**
     * Adds a form record
     * @param form
     * @param mContext
     */
    public static void addForm(Form form, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains("forms")) {
            Log.e("ADD", "Entry was not found for folder ");
        }

        SharedPreferences.Editor editor = settings.edit();
        ArrayList<Form> mForms = new Gson().fromJson(settings.getString("forms", ""), Utils.ARRAY_FORM);
        if (mForms == null) {
            mForms = new ArrayList<Form>();
        }
        mForms.add(form);
        editor.remove("forms");
        editor.putString("forms", new Gson().toJson(mForms, Utils.ARRAY_FORM));
        editor.apply();
    }

    /**
     * Returns the available forms that the user previously created
     * @param mContext
     * @return
     */
    public static ArrayList<Form> getForms(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains("forms")) {
            Log.e("GET FORMS", "Entry was not found");
            return new ArrayList<Form>();
        }
        return new Gson().fromJson(
                settings.getString("forms", ""),
                Utils.ARRAY_FORM);
    }

    /**
     * Removes a form from the records, only should remove the single record and not
     * the forms that were already filled (data)
     * @param formName
     * @param mContext
     */
    public static void deleteForm(String formName, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        if (!settings.contains("forms")) {
            Log.e("DELETE_FORM", "ENTRY WAS NOT FOUND!");
            return;
        }

        ArrayList<Form> forms = getForms(mContext);
        for (Form f: forms) {
            if  (f.getFormName().equals(formName)) {
                forms.remove(f);
                break;
            }
        }
        overwriteForms(forms, mContext);
    }

    /**
     * Replaces current form questions with new ones
     * @param formName
     * @param formQuestions
     * @param mContext
     */
    public static void overwriteFormQuestions(String formName,
                                              ArrayList<FormQuestion> formQuestions,
                                              Context mContext) {

        if (getForm(mContext, formName) == null)
            return;


    }

    /**
     * Replace all forms with the received ones
     * @param forms
     * @param mContext
     */
    public static void overwriteForms(ArrayList<Form> forms, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains("forms")) {
            Log.e("OVERWRITE", "Entry was not found for folder ");
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.remove("forms");
        editor.putString("forms", new Gson().toJson(forms, Utils.ARRAY_FORM));
        editor.apply();
    }

    public static Form getForm(Context mContext, String formName) {
        ArrayList<Form> forms = getForms(mContext);
        for (Form f : forms) {
            if (f.getFormName().equals(formName)) {
                return f;
            }
        }
        return null;
    }
}
