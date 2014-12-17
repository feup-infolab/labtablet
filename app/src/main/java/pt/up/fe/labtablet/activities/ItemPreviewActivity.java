package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.async.AsyncImageLoader;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Created by ricardo on 17-12-2014.
 */
public class ItemPreviewActivity extends Activity {

    private ImageView ivItemPreview;
    private TextView tvItemDescription;
    private TextView tvItemDescriptor;
    private TextView tvItemDescriptorDescription;

    private Descriptor metadataItem;
    private DataItem dataItem;

    private boolean isMetadataVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_preview);
        if (getActionBar() != null) {
            getActionBar().setTitle("Inspect record");
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }


        Bundle extras;

        if (savedInstanceState != null) {
            extras = savedInstanceState;
        } else {
            //Load item from parameters
            Intent intent = getIntent();
            extras = intent.getExtras();
        }

        ivItemPreview = (ImageView) findViewById(R.id.item_preview_image);
        tvItemDescription = (TextView) findViewById(R.id.item_preview_value);

        if(extras.containsKey("data_item")) {
            dataItem = new Gson()
                    .fromJson(extras.getString("data_item"), DataItem.class);

            isMetadataVisible = false;
            attatchDataItemView();
        } else if (extras.containsKey("metadata_item")) {
            metadataItem = new Gson()
                    .fromJson(extras.getString("metadata_item"), Descriptor.class);

            isMetadataVisible = true;
            attatchMetadataItemView();
        } else {
            Toast.makeText(this, "Unable to load asset", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        if (isMetadataVisible) {
            if (metadataItem.hasFile()) {
                ivItemPreview.setTag(metadataItem.getFilePath());
                new AsyncImageLoader(ivItemPreview, this, false).execute();
            }
        } else {
            ivItemPreview.setTag(dataItem.getLocalPath());
            new AsyncImageLoader(ivItemPreview, this, false).execute();
        }
    }

    private void attatchDataItemView() {
        tvItemDescription.setText(dataItem.getDescription());
    }

    private void attatchMetadataItemView() {

        (findViewById(R.id.item_preview_metadata_card)).setVisibility(View.VISIBLE);

        tvItemDescriptor = (TextView) findViewById(R.id.item_preview_descriptor);
        tvItemDescriptorDescription = (TextView) findViewById(R.id.item_preview_descriptor_details);

        tvItemDescription.setText(metadataItem.getValue());
        tvItemDescriptor.setText(metadataItem.getDescriptor());
        tvItemDescriptorDescription.setText(metadataItem.getDescription());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMetadataVisible) {
            attatchMetadataItemView();
        } else {
            attatchDataItemView();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (dataItem != null) {
            outState.putString("data_item",
                    new Gson().toJson(dataItem));
        } else if (metadataItem != null) {
            outState.putString("metadata_item",
                    new Gson().toJson(metadataItem));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == R.id.action_preview_save) {

            Intent returnIntent = new Intent();
            if (isMetadataVisible) {
                returnIntent.putExtra("metadata_item",
                        new Gson().toJson(metadataItem));
                setResult(Utils.METADATA_ITEM_CHANGED, returnIntent);
            } else {
                returnIntent.putExtra("data_item",
                        new Gson().toJson(dataItem));
                setResult(Utils.DATA_ITEM_CHANGED, returnIntent);
            }

            finish();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }
}
