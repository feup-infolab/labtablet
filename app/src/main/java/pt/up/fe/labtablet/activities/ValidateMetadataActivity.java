package pt.up.fe.labtablet.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.UnvalidatedMetadataListAdapter;
import pt.up.fe.labtablet.api.AsyncQueueProcessor;
import pt.up.fe.labtablet.api.AsyncTaskHandler;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.DBCon;
import pt.up.fe.labtablet.utils.Utils;

public class ValidateMetadataActivity extends Activity {

    ProgressDialog mProgressDialog;
    private ArrayList<Descriptor> descriptors;
    private ArrayList<Descriptor> deletionQueue;
    private ArrayList<Descriptor> convertionQueue;
    private UnvalidatedMetadataListAdapter mAdapter;
    private String favoriteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_metadata);

        ActionBar mActionBar = getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("ValidateMetadata" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, ValidateMetadataActivity.this);
        } else {
            getActionBar().setTitle(getResources().getString(R.string.metadata_validation_title));
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }

        if (savedInstanceState == null) {
            String descriptorsJson = getIntent().getStringExtra("descriptors");
            favoriteName = getIntent().getStringExtra("favorite_name");
            descriptors = new Gson().fromJson(descriptorsJson, Utils.ARRAY_DESCRIPTORS);
            deletionQueue = new ArrayList<Descriptor>();
            convertionQueue = new ArrayList<Descriptor>();
        } else {
            descriptors = new Gson().fromJson(savedInstanceState.getString("descriptors"), Utils.ARRAY_DESCRIPTORS);
            deletionQueue = new Gson().fromJson(savedInstanceState.getString("deletionQueue"), Utils.ARRAY_DESCRIPTORS);
            convertionQueue = new Gson().fromJson(savedInstanceState.getString("convertionQueue"), Utils.ARRAY_DESCRIPTORS);
            favoriteName = savedInstanceState.getString("favorite_name");
        }

        ListView lv_unvalidated_metadata = (ListView) findViewById(R.id.lv_unvalidated_metadata);
        lv_unvalidated_metadata.setDividerHeight(0);

        UnvalidatedMetadataListAdapter.unvalidatedMetadataInterface mInterface =
                new UnvalidatedMetadataListAdapter.unvalidatedMetadataInterface() {
                    @Override
                    public void onFileDeletion(Descriptor desc) {
                        deletionQueue.add(desc);
                    }

                    @Override
                    public void onDataConvertion(Descriptor desc) {
                        convertionQueue.add(desc);
                    }
                };

        mAdapter = new UnvalidatedMetadataListAdapter(this,
                descriptors, DBCon.getAssociations(this), favoriteName, mInterface);

        lv_unvalidated_metadata.setAdapter(mAdapter);
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
            if (!descriptors.isEmpty()) {
                mAdapter.notifyDataSetChanged();

                for (Descriptor desc : descriptors) {
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

            new AsyncQueueProcessor(new AsyncTaskHandler<Void>() {
                @Override
                public void onSuccess(Void result) {
                    mProgressDialog.dismiss();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("descriptors", new Gson().toJson(descriptors, Utils.ARRAY_DESCRIPTORS));
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }

                @Override
                public void onFailure(Exception error) {
                    mProgressDialog.dismiss();
                }

                @Override
                public void onProgressUpdate(int value) {

                }
            }).execute(favoriteName, ValidateMetadataActivity.this, deletionQueue, convertionQueue);

        } else if (item.getItemId() == R.id.action_metadata_cancel) {
            deletionQueue.clear();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("descriptors", new Gson().toJson(descriptors, Utils.ARRAY_DESCRIPTORS));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            String descriptorJson = data.getStringExtra("descriptor");
            Descriptor newMetadata = new Gson().fromJson(descriptorJson, Descriptor.class);
            for (Descriptor desc : descriptors) {
                if (desc.getValue().equals(newMetadata.getValue())) {
                    desc.setName(newMetadata.getName());
                    desc.setDescription(newMetadata.getDescription());
                    desc.setDescriptor(newMetadata.getDescriptor());
                    desc.setTag(newMetadata.getTag());
                    desc.setState(Utils.DESCRIPTOR_STATE_VALIDATED);
                }
            }
            mAdapter.notifyDataSetChanged();
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
        outState.putString("favorite_name", favoriteName);
        outState.putString("descriptors", new Gson().toJson(descriptors, Utils.ARRAY_DESCRIPTORS));
        outState.putString("deletionQueue", new Gson().toJson(deletionQueue, Utils.ARRAY_DESCRIPTORS));
        outState.putString("convertionQueue", new Gson().toJson(convertionQueue, Utils.ARRAY_DESCRIPTORS));

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        deletionQueue.clear();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("descriptors", new Gson().toJson(descriptors, Utils.ARRAY_DESCRIPTORS));
        setResult(RESULT_CANCELED, returnIntent);
        finish();
        super.onBackPressed();
    }
}
