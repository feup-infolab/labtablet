package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.UnvalidatedMetadataListAdapter;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

public class ValidateMetadataActivity extends Activity {

    private ArrayList<Descriptor> descriptors;
    private ListView lv_unvalidated_metadata;
    private UnvalidatedMetadataListAdapter mAdapter;
    private String favoriteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_metadata);
        getActionBar().setTitle(getResources().getString(R.string.metadata_validation_title));

        if (savedInstanceState == null) {
            String descriptorsJson = getIntent().getStringExtra("descriptors");
            favoriteName = getIntent().getStringExtra("favorite_name");
            descriptors = new Gson().fromJson(descriptorsJson, Utils.ARRAY_DESCRIPTORS);
        } else {
            descriptors = new Gson().fromJson(savedInstanceState.getString("descriptors"), Utils.ARRAY_DESCRIPTORS);
            favoriteName = savedInstanceState.getString("favorite_name");
        }

        lv_unvalidated_metadata = (ListView) findViewById(R.id.lv_unvalidated_metadata);
        lv_unvalidated_metadata.setDividerHeight(0);


        mAdapter = new UnvalidatedMetadataListAdapter(this,
                descriptors, FileMgr.getAssociations(this), favoriteName);

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
            Intent returnIntent = new Intent();
            returnIntent.putExtra("descriptors", new Gson().toJson(descriptors, Utils.ARRAY_DESCRIPTORS));
            setResult(RESULT_OK, returnIntent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_metadata_cancel) {
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
            for(Descriptor desc : descriptors) {
                if(desc.getValue().equals(newMetadata.getValue())) {
                    desc.setName(newMetadata.getName());
                    desc.setDescription(newMetadata.getDescription());
                    desc.setDescriptor(newMetadata.getDescriptor());
                    desc.setTag(newMetadata.getTag());
                    desc.setState(Utils.DESCRIPTOR_STATE_VALIDATED);
                }
            }
            mAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {
            Toast.makeText(ValidateMetadataActivity.this, "Error", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("favorite_name", favoriteName);
        outState.putString("descriptors", new Gson().toJson(descriptors, Utils.ARRAY_DESCRIPTORS));
        super.onSaveInstanceState(outState);
    }
}
