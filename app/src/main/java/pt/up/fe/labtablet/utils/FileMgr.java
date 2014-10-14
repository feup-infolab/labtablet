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
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.labtablet.models.Descriptor;

import static pt.up.fe.labtablet.utils.DBCon.getDescriptors;

public class FileMgr {

    /**
     * Converts the lenght of a file to a more readable format (eg 23.5Mb)
     * @param bytes
     * @param si
     * @return
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Tries to match the file extension with a mime type
     * @param url Path to the file
     * @return
     */
    public static String getMimeType(String url) {
        return MimeTypeMap
                .getSingleton()
                .getMimeTypeFromExtension(MimeTypeMap
                        .getFileExtensionFromUrl(url.substring(
                                url.lastIndexOf("."))));
    }

    /**
     * This function tries to get the real path, when decoding an image or other media files inside
     * the DCIM folder or the gallery
     * @param context
     * @param contentUri
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * Copy a file to another location
     * @param src
     * @param dst
     * @throws IOException
     */
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

    /**
     * Moves a file to another location
     * @param src
     * @param dst
     * @throws IOException
     */
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



    /**
     * Calculates the size of a folder, taking into account its contents
     * @param directory
     * @return
     */
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

    /**
     * Creates the folder for the metadata
     * @param mContext
     * @param path
     */
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

    /**
     * Recursively follows the contents of the directory and deletes them
     * @param file
     */
    private static boolean deleteDirectory(File file) {
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

    /**
     * Removes a favorite from the records and deletes its data
     * @param favoriteName
     * @param mContext
     */
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

    /**
     * Updates both favorite name and its metadata (location + value)
     */
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

    /**
     * Fetches the username and password given by the user
     */
    public static DendroConfiguration getDendroConf(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        return new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);
    }

}
