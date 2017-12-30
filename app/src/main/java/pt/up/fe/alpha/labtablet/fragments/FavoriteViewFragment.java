package pt.up.fe.alpha.labtablet.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.activities.FavoriteDetailsActivity;
import pt.up.fe.alpha.labtablet.activities.FormSolverActivity;
import pt.up.fe.alpha.labtablet.activities.ItemPreviewActivity;
import pt.up.fe.alpha.labtablet.adapters.DataListAdapter;
import pt.up.fe.alpha.labtablet.adapters.FormListAdapter;
import pt.up.fe.alpha.labtablet.adapters.MetadataListAdapter;
import pt.up.fe.alpha.labtablet.models.DataItem;
import pt.up.fe.alpha.labtablet.models.Descriptor;
import pt.up.fe.alpha.labtablet.models.FavoriteItem;
import pt.up.fe.alpha.labtablet.models.FormInstance;
import pt.up.fe.alpha.labtablet.utils.OnItemClickListener;
import pt.up.fe.alpha.labtablet.utils.Utils;

public class FavoriteViewFragment extends Fragment implements OnItemClickListener {


    private RecyclerView itemList;
    private String mCurrentTag;
    private ArrayList<DataItem> dataItems;
    private ArrayList<Descriptor> metadataItems;
    private ArrayList<Descriptor> syncItems;
    private HashMap<String, ArrayList<FormInstance>> groupedForms;
    private View rootView;

    private AlertDialog alertDialog;

    private DataListAdapter dataListAdapter;
    private MetadataListAdapter metadataListAdapter;
    private MetadataListAdapter syncListAdapter;

    public FavoriteViewFragment() {
        // Required empty public constructor
    }

    public void notifyItemsChanged(FavoriteItem item) {

        switch (mCurrentTag) {
            case "sync":
                syncItems = item.getSyncItems();
                bindSyncView(syncItems);
                break;
            case "metadata":
                metadataItems = item.getMetadataItems();
                bindMetaDataView(metadataItems);
                break;
            case "data":
                dataItems = item.getDataItems();
                bindDataView(dataItems);
                break;
            case "forms":
                groupedForms = groupFormInstances(item.getLinkedForms());
                bindFormsView(groupedForms);
                break;
        }
    }

    private ArrayList<Descriptor> CreateFakeSyncItems(){
        ArrayList<Descriptor> res = new ArrayList<Descriptor>();

        String name = "Project1", descriptor = "ola2", value = "2017/12/29", tag = "ola4";

        Descriptor d = new Descriptor(name, descriptor, value, tag);

        res.add(d);
        return res;
    }

    public static FavoriteViewFragment newInstance(String tag, Object item) {
        FavoriteViewFragment f = new FavoriteViewFragment();
        Bundle args = new Bundle();
        args.putString("items", new Gson().toJson(item));
        args.putString("current_tag", tag);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_generic_list, container, false);

        Bundle args = getArguments();
        if (!args.containsKey("current_tag")) {
            Log.e("FView", "tag was not provided");
            return rootView;
        }

        itemList = (RecyclerView) rootView.findViewById(R.id.list);

        mCurrentTag = args.getString("current_tag");
        assert mCurrentTag != null;
        switch (mCurrentTag) {
            case "data":
                dataItems = new Gson().fromJson(args.getString("items"), new TypeToken<ArrayList<DataItem>>(){}.getType());
                bindDataView(dataItems);
                break;

            case "metadata":
                metadataItems = new Gson().fromJson(args.getString("items"), new TypeToken<ArrayList<Descriptor>>(){}.getType());
                bindMetaDataView(metadataItems);
                break;

            case "sync":
                syncItems = new Gson().fromJson(args.getString("items"), new TypeToken<ArrayList<Descriptor>>(){}.getType());
                bindSyncView(syncItems);
                break;

            case "forms":
                ArrayList<FormInstance> instances = new Gson().fromJson(args.getString("items"), new TypeToken<ArrayList<FormInstance>>(){}.getType());
                groupedForms = groupFormInstances(instances);
                bindFormsView(groupedForms);
                break;
            default:
                Toast.makeText(getActivity(), "NO VIEW ATTACHED", Toast.LENGTH_SHORT).show();
                break;
        }
        itemList.setItemAnimator(new DefaultItemAnimator());
        itemList.animate();

        return rootView;
    }

    /**
     * Attaches the view to display the collected files for a particular favorite
     * @param items data items
     */
    private void bindDataView(ArrayList<DataItem> items) {
        if (dataItems.isEmpty()) {
            rootView.findViewById(R.id.list_state).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.list).setVisibility(View.INVISIBLE);
            return;
        }
        rootView.findViewById(R.id.list_state).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);

        dataListAdapter = new DataListAdapter(items, this, getActivity());
        itemList.setAdapter(dataListAdapter);
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    /**
     * Attaches the view for the existing descriptors
     * @param items favorite's descriptors
     */
    private void bindMetaDataView(ArrayList<Descriptor> items) {
        if (metadataItems.isEmpty()) {
            rootView.findViewById(R.id.list_state).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.list).setVisibility(View.INVISIBLE);
            return;
        }
        rootView.findViewById(R.id.list_state).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);

        metadataListAdapter = new MetadataListAdapter(items, this, getActivity());
        itemList.setAdapter(metadataListAdapter);
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void bindSyncView(ArrayList<Descriptor> items) {
        if (syncItems.isEmpty()) {
            rootView.findViewById(R.id.list_state).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.list).setVisibility(View.INVISIBLE);
            return;
        }
        rootView.findViewById(R.id.list_state).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);

        syncListAdapter = new MetadataListAdapter(items, this, getActivity());
        itemList.setAdapter(syncListAdapter);
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    /**
     * Attaches a list of form instances for a particular favorite (if any) or an appropriate view otherwise
     * @param items existing form items
     */
    private void bindFormsView(HashMap<String, ArrayList<FormInstance>> items) {

        if (items.isEmpty()) {
            rootView.findViewById(R.id.list_state).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.list).setVisibility(View.INVISIBLE);
            return;
        }
        rootView.findViewById(R.id.list_state).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);

        FormListAdapter mAdapter = new FormListAdapter(items, this, getActivity());
        itemList.setAdapter(mAdapter);
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    private HashMap<String, ArrayList<FormInstance>> groupFormInstances(ArrayList<FormInstance> items) {
        HashMap<String, ArrayList<FormInstance>> groupedInstances = new HashMap<>();
        for (FormInstance fi : items) {
            if (groupedInstances.containsKey(fi.getParent())) {
                groupedInstances.get(fi.getParent()).add(fi);
                continue;
            }

            ArrayList<FormInstance> newInstances = new ArrayList<>();
            newInstances.add(fi);
            groupedInstances.put(fi.getParent(), newInstances);
        }
        return groupedInstances;
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getActivity(), ItemPreviewActivity.class);

        switch (mCurrentTag) {
            case "sync":
                final String options[] = {"Mockdata", "B2Share"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Synchronize");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
                builder.show();
                break;
            case "metadata":
                intent.putExtra("metadata_item",
                        new Gson().toJson(metadataItems.get(position)));
                break;
            case "data":
                intent.putExtra("data_item",
                        new Gson().toJson(dataItems.get(position)));
                break;
            case "forms":
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_form_instances_list, null);
                dialogBuilder.setView(dialogView);

                RecyclerView instancesList = (RecyclerView) dialogView.findViewById(R.id.form_instances_list);
                final String baseFormName = (new ArrayList<>(groupedForms.keySet())).get(position);

                instancesList.setAdapter(new FormInstancesListAdapter(groupedForms.get(baseFormName), new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        //dispatch form solver activity with this form
                        Intent i = new Intent(getActivity(), FormSolverActivity.class);
                        i.putExtra("form", new Gson().toJson(groupedForms.get(baseFormName).get(position)));
                        i.putExtra("form_name", groupedForms.get(baseFormName).get(position).getParent());
                        getActivity().startActivityForResult(i, Utils.SOLVE_FORM);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }

                    @Override
                    public void onDeleteRequested(View view, int position) {
                        Toast.makeText(getActivity(), "DELETE FORM NOT IMPLEMENTED YET", Toast.LENGTH_SHORT).show();
                    }
                }));

                dialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                instancesList.setLayoutManager(new LinearLayoutManager(getActivity()));
                alertDialog = dialogBuilder.create();
                alertDialog.show();
                return;
        }

        intent.putExtra("position", position);
        getActivity().startActivityForResult(intent, Utils.ITEM_PREVIEW);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Log.e("LCLICK","" + position);
    }

    @Override
    public void onDeleteRequested(View view, int position) {
        switch (mCurrentTag) {
            case "metadata":
                if (position <= metadataItems.size()) {
                    metadataItems.remove(position);
                    metadataListAdapter.notifyItemRemoved(position);
                    ((FavoriteDetailsActivity) getActivity()).notifyMetadataItemRemoved(metadataItems);
                }
                break;
            case "sync":
                if (position <= syncItems.size()) {
                    syncItems.remove(position);
                    syncListAdapter.notifyItemRemoved(position);
                    ((FavoriteDetailsActivity) getActivity()).notifyMetadataItemRemoved(syncItems);
                }
                break;
            case "data":
                if (position <= dataItems.size()) {
                    dataItems.remove(position);
                    dataListAdapter.notifyItemRemoved(position);
                    ((FavoriteDetailsActivity) getActivity()).notifyDataItemRemoved(dataItems);
                }
                break;
        }
    }

    private class FormInstancesListAdapter extends RecyclerView.Adapter<FormInstancesListAdapter.FormInstanceVH> {
        private final ArrayList<FormInstance> instances;
        private OnItemClickListener listener;


        public FormInstancesListAdapter(ArrayList<FormInstance> srcItems,
                                        OnItemClickListener clickListener) {

            this.instances = srcItems;
            listener = clickListener;
        }

        @Override
        public FormInstanceVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_form_instance, parent, false);
            return new FormInstanceVH(v);
        }

        @Override
        public void onBindViewHolder(FormInstanceVH holder, final int position) {

            holder.instanceTimestamp.setText(instances.get(position).getInstanceTimestamp());
            holder.instanceDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((FavoriteDetailsActivity) getActivity()).notifyFormInstanceRemoved(instances.get(position), position);
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return instances == null ? 0 : instances.size();
        }

        /**
         * ViewHolder for an instance item, timestamp would be the selection criteria for the user's
         * interaction as an instance may not have a name
         */
        class FormInstanceVH extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            public final TextView instanceTimestamp;
            public final ImageButton instanceDelete;

            public FormInstanceVH(View rowView) {
                super(rowView);
                instanceTimestamp = (TextView) rowView.findViewById(R.id.instance_timestamp);
                instanceDelete = (ImageButton) rowView.findViewById(R.id.instance_delete);

                rowView.setOnClickListener(this);
                rowView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View view) {
                listener.onItemClick(view, getPosition());
            }

            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        }
    }
}
