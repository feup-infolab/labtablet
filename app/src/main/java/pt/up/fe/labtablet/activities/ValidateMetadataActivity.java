package pt.up.fe.labtablet.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.MetadataListAdapter;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.async.AsyncQueueProcessor;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.OnItemClickListener;
import pt.up.fe.labtablet.utils.Utils;

public class ValidateMetadataActivity extends Activity implements OnItemClickListener {

    private ProgressDialog mProgressDialog;
    private FavoriteItem fItem;

    private ArrayList<Descriptor> unvalidatedQueue;
    private ArrayList<Descriptor> deletionQueue;
    private ArrayList<Descriptor> conversionQueue;


    private MetadataListAdapter metadataListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_metadata);

        ActionBar mActionBar = getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("ValidateMetadata" + "Couldn't get actionbar.");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, ValidateMetadataActivity.this);
        } else {
            getActionBar().setTitle(getResources().getString(R.string.metadata_validation_title));
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }

        if (savedInstanceState == null) {
            String descriptorsJson = getIntent().getStringExtra("favorite");
            String unvalidatedDescriptors = getIntent().getStringExtra("unvalidated_metadata");

            fItem = new Gson().fromJson(descriptorsJson, FavoriteItem.class);
            unvalidatedQueue = new Gson().fromJson(unvalidatedDescriptors, Utils.ARRAY_DESCRIPTORS);

            deletionQueue = new ArrayList<>();
            conversionQueue = new ArrayList<>();
        } else {
            fItem = new Gson().fromJson(savedInstanceState.getString("favorite"), FavoriteItem.class);
            deletionQueue = new Gson().fromJson(savedInstanceState.getString("deletionQueue"), Utils.ARRAY_DESCRIPTORS);
            conversionQueue = new Gson().fromJson(savedInstanceState.getString("convertionQueue"), Utils.ARRAY_DESCRIPTORS);
            unvalidatedQueue = new Gson().fromJson(savedInstanceState.getString("unvalidated_metadata"), Utils.ARRAY_DESCRIPTORS);
        }

        RecyclerView itemList = (RecyclerView) findViewById(R.id.lv_unvalidate_metadata);
        itemList.setLayoutManager(new LinearLayoutManager(this));
        itemList.setItemAnimator(new DefaultItemAnimator());
        itemList.animate();

        //Create adapter
        metadataListAdapter =
                new MetadataListAdapter(
                        unvalidatedQueue,
                        this,
                        this);

        //Set adapter
        itemList.setAdapter(metadataListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.validate_metadata, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_metadata_save) {
            if (!fItem.getMetadataItems().isEmpty()) {
                metadataListAdapter.notifyDataSetChanged();

                for (Descriptor desc : unvalidatedQueue) {
                    if (desc.getState() == Utils.DESCRIPTOR_STATE_NOT_VALIDATED) {
                        Toast.makeText(getApplication(), getResources().getString(R.string.at_least_undefined_descriptor), Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            }

            mProgressDialog = ProgressDialog.show(ValidateMetadataActivity.this,
                    getResources().getString(R.string.wait_queue_processing_title),
                    getResources().getString(R.string.wait_queue_processing), true);
            mProgressDialog.show();

            for (Descriptor desc : unvalidatedQueue) {
                fItem.addMetadataItem(desc);
            }

            new AsyncQueueProcessor(new AsyncTaskHandler<Void>() {
                @Override
                public void onSuccess(Void result) {
                    mProgressDialog.dismiss();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("favorite", new Gson().toJson(fItem));
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }

                @Override
                public void onFailure(Exception error) {
                    mProgressDialog.dismiss();
                }

                @Override
                public void onProgressUpdate(int value) {
                    mProgressDialog.setProgress(value);
                }
            }).execute(fItem, ValidateMetadataActivity.this, deletionQueue, conversionQueue);

        } else if (item.getItemId() == R.id.action_metadata_cancel) {
            deletionQueue.clear();
            conversionQueue.clear();
            unvalidatedQueue.clear();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("favorite", new Gson().toJson(fItem));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Update descriptor to the picked one
        try {
            int position = data.getIntExtra("position", -1);
            String descriptorJson = data.getStringExtra("metadata_item");
            Descriptor newMetadata = new Gson().fromJson(descriptorJson, Descriptor.class);

            unvalidatedQueue.remove(position);
            unvalidatedQueue.add(newMetadata);

            metadataListAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage(e.getMessage() + " (ValidateMetadataActivity)");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, this);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("favorite", new Gson().toJson(fItem));
        outState.putString("deletionQueue", new Gson().toJson(deletionQueue, Utils.ARRAY_DESCRIPTORS));
        outState.putString("convertionQueue", new Gson().toJson(conversionQueue, Utils.ARRAY_DESCRIPTORS));
        outState.putString("unvalidated_metadata", new Gson().toJson(unvalidatedQueue, Utils.ARRAY_DESCRIPTORS));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        deletionQueue.clear();

        //remove unwanted files
        for (Descriptor desc : fItem.getMetadataItems()) {
            if (desc.hasFile() && desc.getState() == Utils.DESCRIPTOR_STATE_NOT_VALIDATED) {
                new File(desc.getFilePath()).delete();
            }
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra("favorite", new Gson().toJson(fItem));
        setResult(RESULT_CANCELED, returnIntent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, ItemPreviewActivity.class);
        intent.putExtra("metadata_item",
                new Gson().toJson(unvalidatedQueue.get(position)));

        intent.putExtra("position", position);
        startActivityForResult(intent, Utils.ITEM_PREVIEW);
    }

    @Override
    public void onItemLongClick(View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.form_really_delete));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //remove entry and associated file (if any)
                Descriptor deletionItem = unvalidatedQueue.get(position);
                if (deletionItem.hasFile()) {
                    if (new File(deletionItem.getFilePath()).delete()) {
                        unvalidatedQueue.remove(position);
                        metadataListAdapter.notifyItemRemoved(position);
                    } else {
                        Toast.makeText(ValidateMetadataActivity.this
                                , "Failed to remove resource ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    unvalidatedQueue.remove(position);
                    metadataListAdapter.notifyItemRemoved(position);
                }

                FavoriteMgr.updateFavoriteEntry(fItem.getTitle(), fItem, ValidateMetadataActivity.this);
            }
        });

        builder.setCancelable(true);
        builder.show();
    }
}
