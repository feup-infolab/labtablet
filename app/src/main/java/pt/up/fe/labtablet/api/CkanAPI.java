package pt.up.fe.labtablet.api;

import android.util.Log;

import java.net.UnknownHostException;
import java.util.ArrayList;

import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.models.ckan.CKANException;
import pt.up.fe.labtablet.models.ckan.Client;
import pt.up.fe.labtablet.models.ckan.Connection;
import pt.up.fe.labtablet.models.ckan.Dataset;
import pt.up.fe.labtablet.utils.Utils;

public class CkanAPI {

    private static Client mClient;

    public static void Connect() throws UnknownHostException {
        mClient = new Client(new Connection(
                Utils.search_repo
        ), "");
    }

    public static ArrayList<FavoriteItem> Search(String query) throws CKANException {
        ArrayList<FavoriteItem> items = new ArrayList<FavoriteItem>();
        try {
            Dataset.SearchResults search_results = mClient.findDatasets(query);
            for (Dataset dataset : search_results.results) {

                FavoriteItem item = new FavoriteItem("");
                item.setDate_modified(dataset.getMetadata_created().substring(0, 10));
                item.setTitle(dataset.getName() == null ? "" : dataset.getName());
                items.add(item);
            }
        } catch (CKANException e) {
            Log.e("CKAN", e.toString());
            throw e;
        }
        return items;
    }
}
