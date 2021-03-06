package pt.up.fe.beta.labtablet.db_handlers;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.models.Form;
import pt.up.fe.beta.labtablet.utils.Utils;

public class FormMgr {

    /**
     * Returns the currently registered form items (if any), or an empty set otherwise
     * @param context used to access the preference manager
     */
    public static ArrayList<Form> getCurrentBaseForms(Context context) {
        SharedPreferences settings = context.getSharedPreferences(
                context.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (settings.contains(Utils.BASE_FORMS_ENTRY)) {

            return new Gson().fromJson(settings
                            .getString(Utils.BASE_FORMS_ENTRY, ""),
                    Utils.ARRAY_FORM);
        }

        return new ArrayList<>();
    }

    /**
     * Replaces the current entry with the new items
     * @param context used to access the preference manager
     * @param inputItems entry items
     */
    public static void overwriteBaseFormsEntry(Context context, ArrayList<Form> inputItems) {
        SharedPreferences settings = context.getSharedPreferences(
                context.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(Utils.BASE_FORMS_ENTRY);
        editor.putString(Utils.BASE_FORMS_ENTRY, new Gson().toJson(inputItems));
        editor.apply();
    }

    /**
     * Updates a form entry (replaces it with the new one)
     * @param currentForm entry to update
     * @param context used to access the preference manager
     */
    public static void updateFormEntry(Form currentForm, Context context) {
        SharedPreferences settings = context.getSharedPreferences(
                context.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        ArrayList<Form> baseForms = new Gson().fromJson(
                settings.getString(Utils.BASE_FORMS_ENTRY, ""),
                Utils.ARRAY_FORM);

        //Equals operator looks for the name, so we can do this
        baseForms.remove(currentForm);
        baseForms.add(currentForm);

        editor.remove(Utils.BASE_FORMS_ENTRY);
        editor.putString(Utils.BASE_FORMS_ENTRY, new Gson().toJson(baseForms));
        editor.apply();
    }

    public static void removeBaseFormEntry(Context context, Form form) {
        SharedPreferences settings = context.getSharedPreferences(
                context.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        ArrayList<Form> baseForms = new Gson().fromJson(
                settings.getString(Utils.BASE_FORMS_ENTRY, ""),
                Utils.ARRAY_FORM);

        //Equals operator looks for the name, so we can do this
        baseForms.remove(form);

        editor.remove(Utils.BASE_FORMS_ENTRY);
        editor.putString(Utils.BASE_FORMS_ENTRY, new Gson().toJson(baseForms));
        editor.apply();
    }
}
