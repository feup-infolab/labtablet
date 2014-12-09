package pt.up.fe.labtablet.fragments;


import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.FavoriteListAdapter;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Shows the list with the available favorites in the app
 */
public class ListFavoritesFragment extends ListFragment {

    private ArrayList<FavoriteItem> mFavoriteItems;
    private FavoriteListAdapter mFavoriteListAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        ActionBar mActionBar = getActivity().getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("ListFavorites" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, getActivity());
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setSubtitle("");
        }

        mFavoriteItems = new ArrayList<FavoriteItem>();
        mFavoriteListAdapter = new FavoriteListAdapter(getActivity(), mFavoriteItems);
        setListAdapter(mFavoriteListAdapter);

        this.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        FavoriteItem selectedItem = mFavoriteItems.get(position);

        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        //switch to the favorite view
        FavoriteDetailsFragment favoriteDetails = new FavoriteDetailsFragment();
        Bundle args = new Bundle();
        args.putString("favorite_name", selectedItem.getTitle());
        favoriteDetails.setArguments(args);
        transaction.replace(R.id.frame_container, favoriteDetails);
        transaction.addToBackStack("nopes");
        transaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_dataset, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new_dataset:
                FragmentTransaction transaction = super.getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                NewFavoriteBaseFragment favoriteDetails = new NewFavoriteBaseFragment();
                Bundle args = new Bundle();
                args.putString("favorite_name", "");
                favoriteDetails.setArguments(args);
                transaction.replace(R.id.frame_container, favoriteDetails);
                transaction.addToBackStack("nopes");
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String path = Environment.getExternalStorageDirectory().toString();
        File f = new File(path + "/" + getResources().getString(R.string.app_name));
        if (!f.exists()) {
            Log.i("MakeDir", "" + f.mkdir());
        }

        File[] files = f.listFiles();

        mFavoriteItems.clear();
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                FavoriteItem newItem = new FavoriteItem("" + new Date().getTime());
                newItem.setSize(FileMgr.humanReadableByteCount(FileMgr.folderSize(inFile), false));
                newItem.setTitle(inFile.getName());
                newItem.setDate_modified(Utils.getDate(inFile.lastModified()));
                mFavoriteItems.add(newItem);
            }
        }
        mFavoriteListAdapter.notifyDataSetChanged();
    }
}