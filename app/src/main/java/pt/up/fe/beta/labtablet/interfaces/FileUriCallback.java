package pt.up.fe.beta.labtablet.interfaces;

import android.content.Context;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.async.AsyncCustomTaskHandler;
import pt.up.fe.beta.labtablet.async.AsyncPackageCreator;
import pt.up.fe.beta.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.beta.labtablet.models.DataItem;
import pt.up.fe.beta.labtablet.models.Dendro.DendroMetadataRecord;
import pt.up.fe.beta.labtablet.models.Descriptor;
import pt.up.fe.beta.labtablet.models.FavoriteItem;
import pt.up.fe.beta.labtablet.models.FormInstance;
import pt.up.fe.beta.labtablet.models.ProgressUpdateItem;
import pt.up.fe.beta.labtablet.utils.FileMgr;
import pt.up.fe.beta.labtablet.utils.Zipper;

import static pt.up.fe.beta.labtablet.utils.CSVHandler.generateCSV;

/**
 * Callback to return the file list when beaming contents to other devices
 */
public class FileUriCallback implements
        NfcAdapter.CreateBeamUrisCallback {

    private Context mContext;
    private String favoriteName;
    private Uri[] mFileUris = new Uri[10];

    public FileUriCallback(Context context, String favoriteName) {
        this.mContext = context;
        this.favoriteName = favoriteName;
    }
    /**
     * Create content URIs as needed to share with another device
     */
    @Override
    public Uri[] createBeamUris(NfcEvent event) {


        FavoriteItem item = FavoriteMgr.getFavorite(mContext, favoriteName);
        if (item.getTitle().equals("")) {
            Log.e("mNFC", "ERROR when getting the favorite");
            return mFileUris;
        }

        //Generate forms and forms' csv file
        if (item.getLinkedForms().size() > 0) {
            ArrayList<FormInstance> linkedForms = item.getLinkedForms();

            try {
                generateCSV(mContext, linkedForms, favoriteName);
            } catch (IOException e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        //Generate metadata file
        String from = Environment.getExternalStorageDirectory() + "/" + mContext.getResources().getString(R.string.app_name) + "/" + favoriteName;
        String to = Environment.getExternalStorageDirectory() + "/" + favoriteName + ".zip";

        ArrayList<DendroMetadataRecord> dendroStyleRecords = new ArrayList<>();
        ArrayList<Descriptor> descriptors = item.getMetadataItems();

        for (Descriptor desc : descriptors) {
            dendroStyleRecords.add(
                    new DendroMetadataRecord(desc.getDescriptor(), desc.getValue()));
        }

        try {
            File metadataRecord = new File(from, "metadata.json");
            String metadata = new Gson().toJson(dendroStyleRecords);
            FileOutputStream stream = new FileOutputStream(metadataRecord);
            stream.write(metadata.getBytes());
            stream.close();

            DataItem dataItem = new DataItem();
            dataItem.setLocalPath(metadataRecord.getPath());
            dataItem.setFileLevelMetadata(new ArrayList<Descriptor>());
            dataItem.setDescription("Generated metadata record (JSON package)");

            item.addDataItem(dataItem);
            FavoriteMgr.updateFavoriteEntry(favoriteName, item, mContext);

        } catch (IOException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }

        //Generate forms and forms' csv file
        if (item.getLinkedForms().size() > 0) {

            ArrayList<FormInstance> linkedForms = item.getLinkedForms();

            try {
                generateCSV(mContext, linkedForms, favoriteName);
            } catch (IOException e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }


        if (new File(from).listFiles().length > 0) {
            Zipper mZipper = new Zipper();

            Log.i("ZIP_FROM", from);
            Log.i("ZIP_TO", to);
            Boolean result = mZipper.zipFileAtPath(from, to, mContext);

            if (!result) {
                Log.e("ZIP", "Failed to create zip file");
                Toast.makeText(mContext, "Failed to create zip file", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        Log.e("ZIP", "COMPLETED");
        mFileUris[0] = Uri.parse(to);
        return mFileUris;
    }
}
