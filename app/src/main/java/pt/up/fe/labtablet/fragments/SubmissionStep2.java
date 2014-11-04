package pt.up.fe.labtablet.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.ValidateMetadataActivity;
import pt.up.fe.labtablet.adapters.MetadataListAdapter;
import pt.up.fe.labtablet.api.SubmissionStepHandler;
import pt.up.fe.labtablet.db.FavoriteMgr;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.db.DBCon;
import pt.up.fe.labtablet.utils.Utils;


public class SubmissionStep2 extends Fragment {

    private ListView lvMetadata;
    private MetadataListAdapter mAdapter;
    private ArrayList<Descriptor> itemDescriptors;
    private String favoriteName;
    private static SubmissionStepHandler mHandler;

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

        itemDescriptors = FavoriteMgr.getDescriptors(favoriteName, getActivity());

        lvMetadata = (ListView) rootView.findViewById(R.id.submission_validation_metadata_list);
        lvMetadata.setDividerHeight(0);
        mAdapter = new MetadataListAdapter(getActivity(), itemDescriptors, favoriteName);
        lvMetadata.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode != Utils.METADATA_VALIDATION)
            return;

        if(data == null)
            return;

        //deal with the received descriptors
        if(!data.getExtras().containsKey("descriptors")) {
            Toast.makeText(getActivity(), "No descriptors received", Toast.LENGTH_SHORT).show();
        } else {
            String descriptorsJson = data.getStringExtra("descriptors");
            itemDescriptors = new Gson().fromJson(descriptorsJson, Utils.ARRAY_DESCRIPTORS);
            mAdapter = new MetadataListAdapter(getActivity(), itemDescriptors, favoriteName);
            lvMetadata.setAdapter(mAdapter);
            FavoriteMgr.overwriteDescriptors(favoriteName, itemDescriptors, getActivity());
            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
        }
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

        if (item.getItemId() == R.id.dendro_metadata_edit) {
            Intent intent = new Intent(getActivity(), ValidateMetadataActivity.class);
            intent.putExtra("descriptors", new Gson().toJson(itemDescriptors, Utils.ARRAY_DESCRIPTORS));
            intent.putExtra("favorite_name", getArguments().getString("favorite_name"));
            startActivityForResult(intent, Utils.METADATA_VALIDATION);
        } else if (item.getItemId() == R.id.action_dendro_metadata_confirm) {
            mHandler.nextStep(2);
        }
        return super.onOptionsItemSelected(item);

    }
}
