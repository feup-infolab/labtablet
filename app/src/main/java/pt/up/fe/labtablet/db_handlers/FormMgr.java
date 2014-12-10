package pt.up.fe.labtablet.db_handlers;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayDeque;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.Utils;

public class FormMgr {

    /**
     * Returns the currently registered form items (if any), or an empty set otherwise
     * @param context
     * @return
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

        return new ArrayList<Form>();
    }

    /**
     * Replaces the current entry with the new items
     * @param context
     * @param inputItems
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
}
