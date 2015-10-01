package pt.up.fe.labtablet.fragments;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.MainActivity;
import pt.up.fe.labtablet.adapters.FavoriteListAdapter;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.OnItemClickListener;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Shows the list with the available favorites in the app
 */
public class ListFavoritesFragment extends Fragment {

    private ArrayList<FavoriteItem> items;

    private FavoriteListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_favorite_list, container, false);

        setHasOptionsMenu(true);
        items = new ArrayList<>();

        OnItemClickListener itemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FavoriteItem selectedItem = items.get(position);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

                //switch to the favorite view
                FavoriteDetailsFragment favoriteDetails = new FavoriteDetailsFragment();
                Bundle args = new Bundle();
                args.putString("favorite_name", selectedItem.getTitle());
                favoriteDetails.setArguments(args);
                transaction.replace(R.id.frame_container, favoriteDetails);
                //go back to the list of projects
                //transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // anything to consider here?
            }
        };

        adapter = new FavoriteListAdapter(items,
                itemClickListener);

        RecyclerView itemList = (RecyclerView) rootView.findViewById(R.id.favorite_list);
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
                ((MainActivity)getActivity()).promptProjectCreation();
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