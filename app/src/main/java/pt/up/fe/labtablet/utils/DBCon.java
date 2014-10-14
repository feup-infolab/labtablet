package pt.up.fe.labtablet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.AssociationItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.Form;

/**
 * Class to handle any database-related operation from any of the
 * involved models
 */
public class DBCon {

    /**
     * Returns the available descriptors for that specific settings entry
     * @param settingsEntry
     * @param mContext
     * @return
     */
    public static ArrayList<Descriptor> getDescriptors(String settingsEntry, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        String jsonData = settings.getString(settingsEntry, "");
        if (!jsonData.equals("") && !jsonData.equals("[]")) {
            return new Gson().fromJson(jsonData, Utils.ARRAY_DESCRIPTORS);
        }

        Toast.makeText(mContext, "No metadata was found. Default configuration loaded.", Toast.LENGTH_SHORT).show();
        ArrayList<Descriptor> baseCfg = new Gson().fromJson(
                settings.getString(Utils.DESCRIPTORS_CONFIG_ENTRY, ""),
                Utils.ARRAY_DESCRIPTORS);

        ArrayList<Descriptor> folderMetadata = new ArrayList<Descriptor>();

        String descName;
        for (Descriptor desc : baseCfg) {
            descName = desc.getName().toLowerCase();
            if (descName.contains("title")) {
                desc.setValue(settingsEntry);
                desc.validate();
                desc.setDateModified(Utils.getDate());
                folderMetadata.add(desc);
                overwriteDescriptors(settingsEntry, folderMetadata, mContext);
            }
        }
        return folderMetadata;
    }



    /**
     * Returns the available associations
     * @param mContext
     * @return
     */
    public static ArrayList<AssociationItem> getAssociations(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(Utils.ASSOCIATIONS_CONFIG_ENTRY)) {
            Log.e("GET", "No associations found");
            return new ArrayList<AssociationItem>();
        }

        return new Gson().fromJson(
                settings.getString(Utils.ASSOCIATIONS_CONFIG_ENTRY, ""),
                Utils.ARRAY_ASSOCIATION_ITEM);
    }

    /**
     * Replaces all the descriptors by the ones that it receives
     * @param settingsEntry
     * @param descriptors
     * @param mContext
     */
    public static void overwriteDescriptors(String settingsEntry, ArrayList<Descriptor> descriptors, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(settingsEntry)) {
            Log.e("OVERWRITE", "Entry was not found for folder " + settingsEntry);
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(settingsEntry);
        editor.putString(settingsEntry, new Gson().toJson(descriptors, Utils.ARRAY_DESCRIPTORS));
        editor.apply();
    }

    /**
     * Adds a set of descriptors (metadata) to the selected favorite
     * @param favoriteName
     * @param itemDescriptors
     * @param mContext
     */
    public static void addDescriptors(String favoriteName, ArrayList<Descriptor> itemDescriptors, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(favoriteName)) {
            Log.e("Add descriptors", "Entry was not found for folder " + favoriteName);
        } else {
            ArrayList<Descriptor> previousDescriptors = getDescriptors(favoriteName, mContext);
            previousDescriptors.addAll(itemDescriptors);
            overwriteDescriptors(favoriteName, previousDescriptors, mContext);
        }
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
     * Removes a form from the records, only shoudl remove the single record and not
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


}
