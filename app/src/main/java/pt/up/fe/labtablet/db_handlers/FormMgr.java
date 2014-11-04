package pt.up.fe.labtablet.db_handlers;

import android.app.Activity;
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

    /**
     * Returns all the created forms objects
     * @param mContext
     * @return
     */
    public static ArrayList<Form> getBaseForms(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(Utils.BASE_FORMS_ENTRY)) {
            Log.i("OVERWRITE", "Entry was not found for base forms ");
            return new ArrayList<Form>();
        }

        return new Gson().fromJson(settings.getString(Utils.BASE_FORMS_ENTRY, ""), Utils.ARRAY_FORM);
    }

    /**
     * Adds a new base form
     */
    public static void registerBaseForm(Context mContext, Form newItem) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        ArrayList<Form> baseForms;
        if (!settings.contains(Utils.BASE_FORMS_ENTRY)) {
            Log.i("ADD", "Entry was not found for base forms ");
            baseForms = new ArrayList<Form>();
        } else {
            baseForms = new Gson().fromJson(settings.getString(Utils.BASE_FORMS_ENTRY, ""), Utils.ARRAY_FORM);
        }

        SharedPreferences.Editor editor = settings.edit();

        baseForms.add(newItem);
        editor.remove(Utils.BASE_FORMS_ENTRY);
        editor.putString(Utils.BASE_FORMS_ENTRY, new Gson().toJson(baseForms, Utils.ARRAY_FORM));
        editor.apply();
    }

    /**
     * Removes a specific base form from the resources
     * @param mContext
     * @param form
     */
    public static void removeBaseForm(Context mContext, Form form) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        ArrayList<Form> baseForms;
        if (!settings.contains(Utils.BASE_FORMS_ENTRY)) {
            Log.i("ADD", "Entry was not found for base forms ");
            return;
        }

        baseForms = new Gson().fromJson(settings.getString("forms", ""), Utils.ARRAY_FORM);
        SharedPreferences.Editor editor = settings.edit();

        baseForms.remove(form);
        editor.putString("forms", new Gson().toJson(baseForms, Utils.ARRAY_FORM));
        editor.apply();
    }

    /**
     * Updates a baseForm with new data
     * @param currentForm
     * @param mContext
     */
    public static void updateBaseForm(Form currentForm, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(Utils.BASE_FORMS_ENTRY)) {
            Log.e("UPDATE", "Entry was not found for base forms ");
            Toast.makeText(mContext, "Error updating form", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Form> savedForms = new Gson().fromJson(
                settings.getString(Utils.BASE_FORMS_ENTRY, ""),
                Utils.ARRAY_FORM
        );

        for (Form f : savedForms) {
            if (f.getFormName().equals(currentForm.getFormName())) {
                f.setFormQuestions(currentForm.getFormQuestions());
                f.setDescription(currentForm.getFormDescription());
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(Utils.BASE_FORMS_ENTRY);
        editor.putString(Utils.BASE_FORMS_ENTRY, new Gson().toJson(savedForms));
        editor.apply();
    }
}
