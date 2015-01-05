package pt.up.fe.labtablet.fragments;


import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.FavoriteListAdapter;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.db_handlers.FormMgr;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.OnItemClickListener;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Shows the list with the available favorites in the app
 */
public class ListFavoritesFragment extends Fragment {

    private ArrayList<FavoriteItem> items;
    private RecyclerView itemList;

    private FavoriteListAdapter adapter;
    private OnItemClickListener itemClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_favorite_list, container, false);

        setHasOptionsMenu(true);
        items = new ArrayList<>();

        ActionBar mActionBar = getActivity().getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("ListFavorites" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, getActivity());
        } else {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setSubtitle("");
        }



        itemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FavoriteItem selectedItem = items.get(position);

                FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                //switch to the favorite view
                FavoriteDetailsFragment favoriteDetails = new FavoriteDetailsFragment();
                Bundle args = new Bundle();
                args.putString("favorite_name", selectedItem.getTitle());
                favoriteDetails.setArguments(args);
                transaction.replace(R.id.frame_container, favoriteDetails);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // anything to consider here?
            }
        };

        adapter = new FavoriteListAdapter(items,
                R.layout.item_favorite_list,
                itemClickListener);

        itemList = (RecyclerView) rootView.findViewById(R.id.favorite_list);
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
        itemList.setItemAnimator(new DefaultItemAnimator());
        itemList.setAdapter(adapter);
        itemList.animate();

        setHasOptionsMenu(true);

        this.onResume();
        return rootView;
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

        items.clear();
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                FavoriteItem newItem = new FavoriteItem("" + new Date().getTime());
                newItem.setSize(FileMgr.humanReadableByteCount(FileMgr.folderSize(inFile), false));
                newItem.setTitle(inFile.getName());
                newItem.setDate_modified(Utils.getDate(inFile.lastModified()));
                items.add(newItem);
            }
        }
        adapter.notifyDataSetChanged();
    }

}