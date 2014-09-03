package pt.up.fe.labtablet.fragments;


import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.FavoriteListAdapter;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

public class ListFavoritesFragment extends ListFragment {

    private ArrayList<FavoriteItem> mFavoriteItems;
    private FavoriteListAdapter mFavoriteListAdapter;
    private ActionBar mActionBar;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDividerHeight(0);
        setHasOptionsMenu(true);
        mActionBar = getActivity().getActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        mActionBar.setDisplayHomeAsUpEnabled(false);
        //((MainActivity) getActivity()).getDrawerToggle().setDrawerIndicatorEnabled(true);
        mFavoriteItems = new ArrayList<FavoriteItem>();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setSubtitle("");
        mFavoriteListAdapter = new FavoriteListAdapter(getActivity(), mFavoriteItems);
        setListAdapter(mFavoriteListAdapter);

        this.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        FavoriteItem selectedItem = mFavoriteItems.get(position);

        FragmentTransaction transaction = super.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);

        //switch to the dataset view
        FavoriteDetailsFragment datasetDetail = new FavoriteDetailsFragment();
        Bundle args = new Bundle();
        args.putString("favorite_name", selectedItem.getTitle());
        datasetDetail.setArguments(args);
        transaction.replace(R.id.frame_container, datasetDetail);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_dataset, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new_dataset:
                FragmentTransaction transaction = super.getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                NewFavoriteBaseFragment datasetDetail = new NewFavoriteBaseFragment();
                Bundle args = new Bundle();
                args.putString("favorite_name", "");
                datasetDetail.setArguments(args);
                transaction.replace(R.id.frame_container, datasetDetail);
                transaction.addToBackStack(null);
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
        File f = new File(path + "/" +getResources().getString(R.string.app_name));
        File[] files = f.listFiles();

        mFavoriteItems.clear();
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                FavoriteItem newItem = new FavoriteItem();
                newItem.setSize(FileMgr.humanReadableByteCount(FileMgr.folderSize(inFile), false));
                newItem.setTitle(inFile.getName());
                newItem.setDate_modified(Utils.getDate(inFile.lastModified()));
                mFavoriteItems.add(newItem);
            }
        }
        mFavoriteListAdapter.notifyDataSetChanged();
    }
}