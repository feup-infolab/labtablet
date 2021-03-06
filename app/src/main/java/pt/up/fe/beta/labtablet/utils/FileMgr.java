package pt.up.fe.beta.labtablet.utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
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

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.api.ChangelogManager;
import pt.up.fe.beta.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.beta.labtablet.models.ChangelogItem;
import pt.up.fe.beta.labtablet.models.DataItem;
import pt.up.fe.beta.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.beta.labtablet.models.Descriptor;
import pt.up.fe.beta.labtablet.models.FavoriteItem;

public class FileMgr {

    /**
     * Converts the lenght of a file to a more readable format (eg 23.5Kb)
     * @param bytes length
     * @param si 1000 vs 1024
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
     * @return the mime type found
     */
    public static String getMimeType(String url) {
        return MimeTypeMap
                .getSingleton()
                .getMimeTypeFromExtension(MimeTypeMap
                        .getFileExtensionFromUrl(url.substring(
                                url.toLowerCase().lastIndexOf("."))));
    }

    /**
     * This function tries to get the real path, when decoding an image or other media files inside
     * the DCIM folder or the gallery
     * @param context used to access the preference manager
     * @param contentUri file uri
     * @return the path for the file
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        int version = Build.VERSION.SDK_INT;

        if (version >= 19)
            return getRealPathFromURI_API19(context, contentUri);
        else if (version < 11)
            return getRealPathFromURI_BelowAPI11(context, contentUri);
        else
            return getRealPathFromURI_API11to18(context, contentUri);
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    /**
     * Copy a file to another location
     * @param src source
     * @param dst destination
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
     * @param src source
     * @param dst destination
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
        if (!src.delete()){
            Log.e("moveFile", "Failed to delete file");
        }

        inStream.close();
        outStream.close();
    }



    /**
     * Calculates the size of a folder, taking into account its contents
     * @param directory directory to analyse
     * @return the length of the directory
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
     * @param mContext to boradcast the created file
     * @param path path to the directory
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
     * @param file directory to delete
     */
    private static boolean deleteDirectory(File file) {
        boolean result = false;
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else if (!f.delete()){
                        Log.e("deleteDirectory", "Failed to delete file " + f.getName());
                    }
                }
            }
            result = file.delete();
        }
        return result;
    }

    /**
     * Removes a favorite from the records and deletes its data
     * @param favoriteName entry to remove
     * @param mContext context to get the settings
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

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(src)) {
            Log.e("RenameDir", "Entry was not found for folder");
            return false;
        }

        String basePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + mContext.getResources().getString(R.string.app_name)
                + File.separator;


        FavoriteItem item = FavoriteMgr.getFavorite(mContext, src);
        FavoriteMgr.removeFavoriteEntry(mContext, item, false);

        //Change entry name
        item.setTitle(dst);

        //Update metadata records that have an associated file
        ArrayList<Descriptor> metadataRecords = item.getMetadataItems();
        for (Descriptor desc : metadataRecords) {
            if (desc.getTag().equals(Utils.TITLE_TAG)) {
                desc.setValue(dst);
            }
            if (desc.hasFile()) {
                desc.setFilePath(basePath +
                        dst + File.separator +
                        "meta" + File.separator +
                        desc.getValue());
            }
        }

        //Update linked data resources
        ArrayList<DataItem> dataRecords = item.getDataItems();
        for (DataItem desc : dataRecords) {
            desc.setLocalPath(basePath + dst + File.separator + new File(desc.getLocalPath()).getName());
            ArrayList<Descriptor> dataLevelDecriptors = desc.getFileLevelMetadata();
            desc.setFileLevelMetadata(dataLevelDecriptors);
        }

        item.setMetadataItems(metadataRecords);
        item.setDataItems(dataRecords);

        //Move folder
        File from = new File(basePath, src);
        File to = new File(basePath, dst);

        FavoriteMgr.registerFavorite(mContext, item);

        return from.renameTo(to);
    }

    /**
     * Fetches the username and password given by the user
     */
    public static DendroConfiguration getDendroConf(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        return new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);
    }

}
