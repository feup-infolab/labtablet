package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.async.AsyncImageLoader;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;


public class ItemPreviewActivity extends Activity {

    private ImageView ivItemPreview;
    private TextView tvItemDescription;
    private TextView itemMimeType;

    private Descriptor metadataItem;
    private Descriptor draftMetadataItem;

    private DataItem dataItem;
    private DataItem draftDataItem;

    private boolean isMetadataVisible;
    private int position;

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
        itemMimeType = (TextView) findViewById(R.id.item_preview_mime);
        tvItemDescription = (TextView) findViewById(R.id.item_preview_value);

        if (!extras.containsKey("position")) {
            Toast.makeText(this, "Unable to load asset", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        position = extras.getInt("position");

        if(extras.containsKey("data_item")) {
            dataItem = new Gson()
                    .fromJson(extras.getString("data_item"), DataItem.class);

            isMetadataVisible = false;
            atatchDataItemView();
        } else if (extras.containsKey("metadata_item")) {
            metadataItem = new Gson()
                    .fromJson(extras.getString("metadata_item"), Descriptor.class);

            isMetadataVisible = true;
            atatchMetadataItemView();
        } else {
            Toast.makeText(this, "Unable to load asset", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (isMetadataVisible && metadataItem.hasFile()) {
            findViewById(R.id.item_preview_value).setVisibility(View.GONE);
        }

        tvItemDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog();
            }
        });

        //Load image thumbnail
        if (isMetadataVisible) {
            if (metadataItem.hasFile() &&
                    Utils.knownImageMimeTypes.contains(FileMgr.getMimeType(metadataItem.getFilePath()))) {
                ivItemPreview.setTag(metadataItem.getFilePath());
                new AsyncImageLoader(ivItemPreview, this, false).execute();
            } else {
                ivItemPreview.setImageDrawable(getDrawable(R.drawable.ic_folder));
            }

        } else {
            ivItemPreview.setTag(dataItem.getLocalPath());
            new AsyncImageLoader(ivItemPreview, this, false).execute();
        }
    }

    private void promptDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText input = new EditText(this);
        input.setText(tvItemDescription.getText().toString());
        input.requestFocus();
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.action_save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (isMetadataVisible){
                    draftMetadataItem = metadataItem;
                    draftMetadataItem.setValue(input.getText().toString());
                    onResume();

                } else {
                    draftDataItem = dataItem;
                    draftDataItem.setDescription(input.getText().toString());
                    onResume();
                }
            }
        });

        alert.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    private void atatchDataItemView() {

        final DataItem currentItem
                = draftDataItem == null ? dataItem : draftDataItem;

        tvItemDescription.setText(currentItem.getDescription());
        itemMimeType.setText(currentItem.getMimeType());
        ivItemPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Utils.openFile(ItemPreviewActivity.this, new File(currentItem.getLocalPath()));
                } catch (IOException e) {
                    Toast.makeText(ItemPreviewActivity.this, "Unable to open associated file", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void atatchMetadataItemView() {

        final Descriptor currentItem
                = draftMetadataItem == null ? metadataItem : draftMetadataItem;

        String mime = metadataItem.hasFile() ? FileMgr.getMimeType(metadataItem.getFilePath()) : "";

        TextView tvItemDescriptor = (TextView) findViewById(R.id.item_preview_descriptor);
        TextView tvItemDescriptorDescription = (TextView) findViewById(R.id.item_preview_descriptor_details);

        itemMimeType.setText(mime);
        tvItemDescription.setText(currentItem.getValue());

        tvItemDescriptor.setText(currentItem.getDescriptor());
        tvItemDescriptorDescription.setText(currentItem.getDescription());

        CardView metadataDescriptorCard = (CardView) findViewById(R.id.item_preview_metadata_card);

        metadataDescriptorCard.setVisibility(View.VISIBLE);
        metadataDescriptorCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickDescriptorIntent = new Intent(ItemPreviewActivity.this, DescriptorPickerActivity.class);
                pickDescriptorIntent.putExtra("file_extension", "");
                pickDescriptorIntent.putExtra("descriptor", new Gson().toJson(currentItem));
                pickDescriptorIntent.putExtra("favoriteName", "");
                pickDescriptorIntent.putExtra("returnMode", Utils.DESCRIPTOR_GET);
                startActivityForResult(pickDescriptorIntent, Utils.DESCRIPTOR_GET);
            }
        });

        ivItemPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentItem.hasFile()) {
                    return;
                }

                try {
                    Utils.openFile(ItemPreviewActivity.this, new File(currentItem.getFilePath()));
                } catch (IOException e) {
                    Toast.makeText(ItemPreviewActivity.this, "Unable to open associated file", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMetadataVisible) {
            atatchMetadataItemView();
        } else {
            atatchDataItemView();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("position", position);

        if (dataItem != null) {
            outState.putString("data_item",
                    new Gson().toJson(dataItem));
        } else if (metadataItem != null) {
            outState.putString("metadata_item",
                    new Gson().toJson(metadataItem));
        }

        if (draftDataItem != null) {
            outState.putString("draft_data_item",
                    new Gson().toJson(draftDataItem));
        }

        if (draftMetadataItem != null) {
            outState.putString("draft_metadata_item",
                    new Gson().toJson(draftMetadataItem));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() != R.id.action_preview_save) {
            finish();
            return true;
        }


        //No updates to metadata descriptor
        if (isMetadataVisible && draftMetadataItem == null) {
            finish();
            return true;
        }

        //No updates to data item
        if (!isMetadataVisible && draftDataItem == null) {
            finish();
            return true;
        }


        //At this point, either the descriptor or data item were changed
        //so the return object will be one of the draft items
        Intent returnIntent = new Intent();
        if (isMetadataVisible) {

            returnIntent.putExtra("metadata_item",
                    new Gson().toJson(draftMetadataItem));
            setResult(Utils.METADATA_ITEM_CHANGED, returnIntent);

        } else {

            returnIntent.putExtra("data_item",
                    new Gson().toJson(dataItem));
            setResult(Utils.DATA_ITEM_CHANGED, returnIntent);
        }

        returnIntent.putExtra("position", position);
        finish();
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;

        //Add single descriptor
        if (requestCode != Utils.DESCRIPTOR_GET) {
            return;
        }

        if (!data.getExtras().containsKey("descriptor"))
            return;

        draftMetadataItem = new Gson().fromJson(data.getStringExtra("descriptor"), Descriptor.class);

        if (metadataItem.hasFile()) {
            draftMetadataItem.setFilePath(metadataItem.getFilePath());
            draftMetadataItem.setDescription(metadataItem.getDescription());
        }

        ((TextView) findViewById(R.id.item_preview_descriptor)).setText(draftMetadataItem.getDescriptor());
        ((TextView) findViewById(R.id.item_preview_descriptor_details)).setText(draftMetadataItem.getDescription());

        Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
    }

}
