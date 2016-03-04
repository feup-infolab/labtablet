package pt.up.fe.alpha.labtablet.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.models.ChangelogItem;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * This class is used to manage any log the application may produce,
 * including saving them to a permanent state
 */
public class ChangelogManager {

    public static void addLog(ChangelogItem item, Context mContext) {

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        ArrayList<ChangelogItem> items = new ArrayList<>();
        if (!settings.contains(Utils.CHANGELOG_CONFIG_ENTRY)) {
            items.add(item);
            editor.putString(
                    Utils.CHANGELOG_CONFIG_ENTRY,
                    new Gson().toJson(items, Utils.ARRAY_CHANGELOG_ITEM));
            editor.apply();
            Toast.makeText(mContext, "Created", Toast.LENGTH_SHORT).show();
            return;
        }

        items = new Gson().fromJson(
                settings.getString(Utils.CHANGELOG_CONFIG_ENTRY, ""),
                Utils.ARRAY_CHANGELOG_ITEM);
        items.add(item);

        editor.putString(Utils.CHANGELOG_CONFIG_ENTRY, new Gson().toJson(
                items,
                Utils.ARRAY_CHANGELOG_ITEM));

        editor.apply();
    }

    public static ArrayList<ChangelogItem> getItems(Context mContext) {

        ArrayList<ChangelogItem> items;
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if (!settings.contains(Utils.CHANGELOG_CONFIG_ENTRY)) {
            items = new ArrayList<>();
            editor.putString(
                    Utils.CHANGELOG_CONFIG_ENTRY,
                    new Gson().toJson(items, Utils.ARRAY_CHANGELOG_ITEM));
            editor.apply();
            Toast.makeText(mContext, "Created", Toast.LENGTH_SHORT).show();
            return items;
        }

        items = new Gson().fromJson(settings
                        .getString(Utils.CHANGELOG_CONFIG_ENTRY, ""),
                Utils.ARRAY_CHANGELOG_ITEM);

        return items;
    }

    public static void clearLogs(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        if (!settings.contains(Utils.CHANGELOG_CONFIG_ENTRY)) {
            return;
        }

        editor.remove(Utils.CHANGELOG_CONFIG_ENTRY);
        editor.apply();
    }

    public static void remove(Context mContext, ChangelogItem upItem) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        if (!settings.contains(Utils.CHANGELOG_CONFIG_ENTRY)) {
            return;
        }

        ArrayList<ChangelogItem> storedItems = new Gson().fromJson(
                settings.getString(Utils.CHANGELOG_CONFIG_ENTRY, ""),
                Utils.ARRAY_CHANGELOG_ITEM);

        storedItems.remove(upItem);

        editor.putString(Utils.CHANGELOG_CONFIG_ENTRY, new Gson().toJson(
                storedItems,
                Utils.ARRAY_CHANGELOG_ITEM));

        editor.apply();
    }
}
