package pt.up.fe.labtablet.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Class to manage access to the DB for data assets/files
 */
public class DataResourcesMgr {

    /**
     * Returns the registered descriptions for a specific parent / favorite / project
     * @param mContext
     * @return
     */
    public static ArrayList<DataItem> getDataDescriptionItems(
            Context mContext, String favoriteName) {

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        ArrayList<DataItem> items = new Gson().fromJson(
                settings.getString(favoriteName + Utils.DATA_DESCRIPTOR_ENTRY, ""),
                Utils.ARRAY_DATA_DESCRIPTOR_ITEMS);


        if (items == null) {
            return new ArrayList<DataItem>();
        }

        return items;
    }

    /**
     * Updates a specific data level resource
     * @param mContext
     * @return
     */
    public static void updateDataDescriptionItems(
            Context mContext, ArrayList<DataItem> entries, String favoriteName) {

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        ArrayList<DataItem> items = new Gson().fromJson(
                settings.getString(favoriteName + Utils.DATA_DESCRIPTOR_ENTRY, ""),
                Utils.ARRAY_DATA_DESCRIPTOR_ITEMS);

        SharedPreferences.Editor editor = settings.edit();
        if (items != null) {
            editor.remove(Utils.DATA_DESCRIPTOR_ENTRY);
        }

        editor.putString(favoriteName + Utils.DATA_DESCRIPTOR_ENTRY,
                new Gson().toJson(entries, Utils.ARRAY_DATA_DESCRIPTOR_ITEMS));
        editor.apply();
    }

    /**
     * Overwrite data descriptors for a specific entry
     */
    public static void overwriteDataItems(Context mContext, ArrayList<DataItem> items, String favoriteName) {

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        String dbEntry = favoriteName + Utils.DATA_DESCRIPTOR_ENTRY;

        SharedPreferences.Editor editor = settings.edit();
        if (settings.contains(dbEntry)) {
            editor.remove(dbEntry);
        }

        editor.putString(favoriteName + Utils.DATA_DESCRIPTOR_ENTRY, new Gson().toJson(items));
        editor.apply();
    }

    /**
     * Adds a new entry for a file description.
     * If the entry does not exists, a new one is added
     *
     * @param mContext
     */
    public static void addDataItem(Context mContext, DataItem item, String favoriteName) {

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);


        ArrayList<DataItem> availableItems;
        SharedPreferences.Editor editor = settings.edit();
        if (settings.contains(favoriteName + Utils.DATA_DESCRIPTOR_ENTRY)) {
            availableItems = new Gson().fromJson(settings.getString(favoriteName + Utils.DATA_DESCRIPTOR_ENTRY, ""),
                    Utils.ARRAY_DATA_DESCRIPTOR_ITEMS);
        } else {
            availableItems = new ArrayList<DataItem>();
        }

        availableItems.add(item);
        editor.remove(Utils.DATA_DESCRIPTOR_ENTRY);
        editor.putString(favoriteName + Utils.DATA_DESCRIPTOR_ENTRY, new Gson().toJson(availableItems));
        editor.commit();
    }
}
