package pt.up.fe.labtablet.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.AssociationItem;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.Form;

public class FileMgr {

    public static void copy(File src, File dst) throws IOException {

        if (!dst.exists()) {
            Log.i("New File", "" + dst.createNewFile());
        }

        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static void moveFile(File src, File dst) throws IOException {
        if (!dst.exists()) {
            Log.i("New File", "" + dst.createNewFile());
        }

        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        src.delete();
        inStream.close();
        outStream.close();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url.toLowerCase());
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static void makeMetaDir(Context mContext, String path) {

        final File newFolder = new File(path);
        if (!newFolder.exists()) {
            if (!newFolder.mkdirs()) {
                ChangelogItem item = new ChangelogItem();
                item.setMessage("FieldMode" + "Failed to delete file " + newFolder.getAbsolutePath());
                item.setTitle(mContext.getResources().getString(R.string.developer_error));
                item.setDate(Utils.getDate());
                ChangelogManager.addLog(item, mContext);
            }
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFolder)));
        }
    }


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

    public static boolean deleteDirectory(File file) {
        boolean result = false;
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
            result = file.delete();
        }
        return result;
    }

    public static void removeFavorite(String favoriteName, Context mContext) {
        ProgressDialog dialog = ProgressDialog.show(mContext, "",
                "Processing", true);
        dialog.show();

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + mContext.getResources().getString(R.string.app_name)
                + "/" + favoriteName;

        final File file = new File(path);

        if (!deleteDirectory(file)) {
            Toast.makeText(mContext, "Unable to delete folder", Toast.LENGTH_LONG).show();
            dialog.dismiss();
            return;
        }

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(favoriteName)) {
            Log.e("REMOVE", "Entry was not found for folder");
            dialog.dismiss();
            return;
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(favoriteName);
        editor.apply();
        dialog.dismiss();
    }

    //Update both favorite and its metadata (location + value)
    public static boolean renameFavorite(String src, String dst, Context mContext) {
        String basePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + mContext.getResources().getString(R.string.app_name)
                + "/";
        File file = new File(basePath + src);
        File file2 = new File(basePath + dst);

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(src)) {
            Log.e("RenameDir", "Entry was not found for folder");
            return false;
        }

        ArrayList<Descriptor> previousRecords = getDescriptors(src, mContext);
        for (Descriptor desc : previousRecords) {
            if (desc.getTag().equals(Utils.TITLE_TAG)) {
                desc.setValue(dst);
            }
            if (!desc.getFilePath().equals("")) {
                //Update the file path
                desc.setFilePath(basePath + dst + "/meta/" + desc.getValue());
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(src);

        editor.apply();
        editor.putString(dst, new Gson().toJson(previousRecords, Utils.ARRAY_DESCRIPTORS));
        editor.apply();

        return file.renameTo(file2);
    }


    public static DendroConfiguration getDendroConf(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        return new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);
    }


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
