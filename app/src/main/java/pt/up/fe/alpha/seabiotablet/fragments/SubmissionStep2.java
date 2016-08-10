package pt.up.fe.alpha.seabiotablet.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.seabiotablet.activities.ItemPreviewActivity;
import pt.up.fe.alpha.seabiotablet.adapters.MetadataListAdapter;
import pt.up.fe.alpha.seabiotablet.api.SubmissionStepHandler;
import pt.up.fe.alpha.seabiotablet.db_handlers.FavoriteMgr;
import pt.up.fe.alpha.seabiotablet.models.DataItem;
import pt.up.fe.alpha.seabiotablet.models.Descriptor;
import pt.up.fe.alpha.seabiotablet.models.FavoriteItem;
import pt.up.fe.alpha.seabiotablet.utils.OnItemClickListener;
import pt.up.fe.alpha.seabiotablet.utils.Utils;


public class SubmissionStep2 extends Fragment implements OnItemClickListener {

    private RecyclerView lvMetadata;
    private MetadataListAdapter mAdapter;
    private String favoriteName;
    private static SubmissionStepHandler mHandler;
    private FavoriteItem favoriteItem;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SubmissionStep2 newInstance(String favoriteName, SubmissionStepHandler handler) {
        SubmissionStep2 fragment = new SubmissionStep2();
        Bundle args = new Bundle();
        args.putString("favorite_name", favoriteName);
        fragment.setArguments(args);
        mHandler = handler;
        return fragment;
    }

    public SubmissionStep2() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_submission_step2, container, false);
        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            favoriteName = getArguments().getString("favorite_name");
        } else {
            favoriteName = savedInstanceState.getString("favorite_name");
        }

        favoriteItem = FavoriteMgr.getFavorite(getActivity(), favoriteName);

        lvMetadata = (RecyclerView) rootView.findViewById(R.id.submission_validation_metadata_list);

        lvMetadata.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvMetadata.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MetadataListAdapter(
                favoriteItem.getMetadataItems(),
                this, getActivity() );
        lvMetadata.setAdapter(mAdapter);

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("favorite_name", getArguments().getString("favorite_name"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.submission_step2, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_dendro_metadata_confirm) {
            mHandler.nextStep(2);
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getActivity(), ItemPreviewActivity.class);

        intent.putExtra("metadata_item",
                new Gson().toJson(favoriteItem.getMetadataItems().get(position)));

        intent.putExtra("position", position);
        startActivityForResult(intent, Utils.ITEM_PREVIEW);
    }

    @Override
    public void onItemLongClick(View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.form_really_delete));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //remove entry and associated file (if any)
                Descriptor deletionItem = favoriteItem.getMetadataItems().get(position);
                if (deletionItem.hasFile()) {
                    if (new File(deletionItem.getFilePath()).delete()) {
                        favoriteItem.getMetadataItems().remove(position);
                        mAdapter.notifyItemRemoved(position);
                    } else {
                        Toast.makeText(getActivity(), "Failed to remove resource ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    favoriteItem.getMetadataItems().remove(position);
                    mAdapter.notifyItemRemoved(position);
                }


                FavoriteMgr.updateFavoriteEntry(favoriteItem.getTitle(), favoriteItem, getActivity());
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    @Override
    public void onDeleteRequested(View view, int position) {
        Toast.makeText(getActivity(), "DELETION NOT IMPLEMENTED HERE YET (SubmissionStep2)", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;

         if (requestCode == Utils.ITEM_PREVIEW) {

            Bundle extras = data.getExtras();

            if (resultCode == Utils.DATA_ITEM_CHANGED) {
                if (extras == null
                        || !extras.containsKey("data_item")
                        || !extras.containsKey("position")) {
                    throw new AssertionError("Received no data from item preview");
                }

                DataItem item = new Gson()
                        .fromJson(data.getStringExtra("data_item"), DataItem.class);

                favoriteItem.getDataItems().remove(extras.getInt("position"));
                favoriteItem.addDataItem(item);

            } else if (resultCode == Utils.METADATA_ITEM_CHANGED) {
                if (extras == null
                        || !extras.containsKey("metadata_item")
                        || !extras.containsKey("position")) {
                    throw new AssertionError("Received no data from item preview");
                }

                Descriptor item = new Gson()
                        .fromJson(data.getStringExtra("metadata_item"), Descriptor.class);

                favoriteItem.getMetadataItems().remove(extras.getInt("position"));
                favoriteItem.addMetadataItem(item);
            }

            FavoriteMgr.updateFavoriteEntry(favoriteItem.getTitle(), favoriteItem, getActivity());
            onResume();
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        if (favoriteName != null) {
            favoriteItem = FavoriteMgr.getFavorite(getActivity(), favoriteName);
        }

        mAdapter =
                new MetadataListAdapter(
                        favoriteItem.getMetadataItems(),
                        this,
                        getActivity());

        lvMetadata.setAdapter(mAdapter);
    }

}

