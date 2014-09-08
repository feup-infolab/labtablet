package pt.up.fe.labtablet.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Created by ricardo on 21-03-2014.
 */
public class ChangelogManager {

    public static void addLog(ChangelogItem item, Context mContext) {

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        ArrayList<ChangelogItem> items = new ArrayList<ChangelogItem>();
        if (!settings.contains(Utils.CHANGELOG_CONFIG_ENTRY)) {
            items.add(item);
            editor.putString(
                    Utils.CHANGELOG_CONFIG_ENTRY,
                    new Gson().toJson(items, Utils.ARRAY_CHANGELOG_ITEM));
            editor.apply();
            Toast.makeText(mContext, "Created", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<ChangelogItem> storedItems = new Gson().fromJson(
                settings.getString(Utils.CHANGELOG_CONFIG_ENTRY, ""),
                Utils.ARRAY_CHANGELOG_ITEM);
        items = storedItems;
        items.add(item);

        editor.putString(Utils.CHANGELOG_CONFIG_ENTRY, new Gson().toJson(
                updateLogs(items),
                Utils.ARRAY_CHANGELOG_ITEM));

        editor.apply();
        return;
    }

    public static ArrayList<ChangelogItem> getItems (Context mContext) {

        ArrayList<ChangelogItem> items;
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if (!settings.contains(Utils.CHANGELOG_CONFIG_ENTRY)) {
            items = new ArrayList<ChangelogItem>();
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

        return updateLogs(items);
    }

    public static ArrayList<ChangelogItem> updateLogs(ArrayList<ChangelogItem> items) {
        for(ChangelogItem item : items) {
            if(item.isRead()) {
                items.remove(item);
            }
        }
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
                updateLogs(storedItems),
                Utils.ARRAY_CHANGELOG_ITEM));

        editor.apply();
    }

    public static void addItems(ArrayList<ChangelogItem> items, Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        if (!settings.contains(Utils.CHANGELOG_CONFIG_ENTRY)) {
            editor.putString(
                    Utils.CHANGELOG_CONFIG_ENTRY,
                    new Gson().toJson(items, Utils.ARRAY_CHANGELOG_ITEM));
            editor.apply();
            Toast.makeText(mContext, "Created", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<ChangelogItem> storedItems = new Gson().fromJson(
                settings.getString(Utils.CHANGELOG_CONFIG_ENTRY, ""),
                Utils.ARRAY_CHANGELOG_ITEM);

        for(ChangelogItem item : items) { storedItems.add(item); }
        editor.remove(Utils.CHANGELOG_CONFIG_ENTRY);
        editor.putString(
                Utils.CHANGELOG_CONFIG_ENTRY,
                new Gson().toJson(storedItems, Utils.ARRAY_CHANGELOG_ITEM));
        editor.apply();
    }

    public static String addedLog(String descriptor, String value) {
        return descriptor + " - " + value;
    }

    public static String createdLog(String value) {
        return "Created favorite " + value;
    }
}
