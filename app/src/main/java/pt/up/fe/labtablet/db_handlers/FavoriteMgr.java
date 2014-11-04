package pt.up.fe.labtablet.db_handlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Class to handle favorite management and access to the "DB"
 */
public class FavoriteMgr {

    /**
     * Returns the available descriptors for that specific settings entry
     * (either a favorite or the global entry)
     * @param settingsEntry entry for the preferences settings
     * @param mContext context
     * @return An array list of the fetched descriptors
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
}
