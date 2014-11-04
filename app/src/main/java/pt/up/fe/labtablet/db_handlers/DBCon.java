package pt.up.fe.labtablet.db_handlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.AssociationItem;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Class to handle any database-related operation from any of the
 * involved models
 */
public class DBCon {



    /**
     * Returns the available associations
     * @param mContext
     * @return An array list of the registered associations
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
}
