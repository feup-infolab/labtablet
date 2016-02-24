package pt.up.fe.labtablet.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.DataListAdapter;
import pt.up.fe.labtablet.adapters.MetadataListAdapter;
import pt.up.fe.labtablet.async.AsyncCustomTaskHandler;
import pt.up.fe.labtablet.async.AsyncFileImporter;
import pt.up.fe.labtablet.async.AsyncPackageCreator;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.models.ProgressUpdateItem;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.OnItemClickListener;
import pt.up.fe.labtablet.utils.Utils;

public class FavoriteDetailsActivity extends AppCompatActivity {

    //Buttons to switch between data and metadata views
    private Button bt_meta_view;
    private Button bt_data_view;

    private boolean isMetadataVisible;

    private FavoriteItem currentItem;
    private String favoriteName;

    private RecyclerView itemList;
    private OnItemClickListener itemClickListener;

    private MetadataListAdapter metadataListAdapter;
    private DataListAdapter dataListAdapter;

    private CoordinatorLayout coordinatorLayout;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_view);

        FloatingActionButton bt_new_metadata
                = (FloatingActionButton) findViewById(R.id.bt_new_metadata);


        Button bt_fieldMode = (Button) findViewById(R.id.bt_field_mode);

        bt_meta_view = (Button) findViewById(R.id.tab_metadata);
        bt_data_view = (Button) findViewById(R.id.tab_data);

        if (savedInstanceState != null) {
            currentItem = new Gson().fromJson(savedInstanceState.getString("current_item"), FavoriteItem.class);
            favoriteName = savedInstanceState.getString("favorite_name");
            isMetadataVisible = savedInstanceState.getBoolean("metadata_visible");
        } else {
            Bundle extras = getIntent().getExtras();
            favoriteName = extras.getString("favorite_name");
            currentItem = FavoriteMgr.getFavorite(this, favoriteName);
            isMetadataVisible = true;
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(currentItem.getTitle());
        mToolbar.setSubtitle(currentItem.getDescription());

        itemList = (RecyclerView) findViewById(R.id.lv_favorite_metadata);
        itemList.setLayoutManager(new LinearLayoutManager(this));
        itemList.setItemAnimator(new DefaultItemAnimator());
        itemList.animate();


        itemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent intent = new Intent(FavoriteDetailsActivity.this, ItemPreviewActivity.class);
                if (isMetadataVisible) {
                    intent.putExtra("metadata_item",
                            new Gson().toJson(currentItem.getMetadataItems().get(position)));
                } else {
                    intent.putExtra("data_item",
                            new Gson().toJson(currentItem.getDataItems().get(position)));
                }

                intent.putExtra("position", position);
                startActivityForResult(intent, Utils.ITEM_PREVIEW);
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteDetailsActivity.this);
                builder.setTitle(getResources().getString(R.string.form_really_delete));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //remove entry and associated file (if any)
                        if (isMetadataVisible) {
                            Descriptor deletionItem = currentItem.getMetadataItems().get(position);
                            if (deletionItem.hasFile()) {
                                if (new File(deletionItem.getFilePath()).delete()) {
                                    currentItem.getMetadataItems().remove(position);
                                    metadataListAdapter.notifyItemRemoved(position);
                                } else {
                                    Toast.makeText(FavoriteDetailsActivity.this, "Failed to remove resource ", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                currentItem.getMetadataItems().remove(position);
                                metadataListAdapter.notifyItemRemoved(position);
                            }

                        } else {
                            if ((new File(currentItem.getDataItems().get(position).getLocalPath())).delete()) {
                                currentItem.getDataItems().remove(position);
                                dataListAdapter.notifyItemRemoved(position);

                            } else {
                                Toast.makeText(FavoriteDetailsActivity.this, "Failed to remove resource ", Toast.LENGTH_SHORT).show();
                            }
                        }

                        FavoriteMgr.updateFavoriteEntry(currentItem.getTitle(), currentItem, FavoriteDetailsActivity.this);
                    }
                });
                builder.setCancelable(true);
                builder.show();
            }
        };


        if (isMetadataVisible) {
            loadMetadataView();
        } else {
            loadDataView();
        }


        bt_fieldMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavoriteDetailsActivity.this, FieldModeActivity.class);
                intent.putExtra("favorite_name", favoriteName);
                startActivity(intent);
            }
        });

        bt_new_metadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isMetadataVisible) {
                    Toast.makeText(FavoriteDetailsActivity.this, "Choose the file", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent, Utils.PICK_FILE_INTENT);
                } else {
                    Intent myIntent = new Intent(FavoriteDetailsActivity.this, DescriptorPickerActivity.class);
                    myIntent.putExtra("file_extension", "");
                    myIntent.putExtra("favoriteName", favoriteName);
                    myIntent.putExtra("returnMode", Utils.DESCRIPTOR_DEFINE);
                    startActivityForResult(myIntent, Utils.DESCRIPTOR_DEFINE);
                }
            }
        });

        bt_data_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDataView();
            }
        });
        bt_meta_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMetadataView();
            }
        });
    }


    private void loadMetadataView() {
        bt_data_view.setEnabled(true);
        bt_data_view.setBackgroundColor(getResources().getColor(R.color.primary));
        bt_meta_view.setBackgroundColor(getResources().getColor(R.color.primary_dark));
        bt_meta_view.setEnabled(false);
        isMetadataVisible = true;

        metadataListAdapter =
                new MetadataListAdapter(
                        currentItem.getMetadataItems(),
                        itemClickListener,
                        FavoriteDetailsActivity.this);

        itemList.setAdapter(metadataListAdapter);
    }

    private void loadDataView() {

        bt_data_view.setEnabled(false);
        bt_meta_view.setEnabled(true);
        bt_data_view.setBackgroundColor(getResources().getColor(R.color.primary_dark));
        bt_meta_view.setBackgroundColor(getResources().getColor(R.color.primary));

        isMetadataVisible = false;

        dataListAdapter = new DataListAdapter(
                currentItem.getDataItems(),
                itemClickListener,
                FavoriteDetailsActivity.this);

        itemList.setAdapter(dataListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();



        if (favoriteName != null) {
            currentItem = FavoriteMgr.getFavorite(FavoriteDetailsActivity.this, favoriteName);
            mToolbar.setSubtitle(currentItem.getDescription());
            mToolbar.setTitle(currentItem.getTitle());
        }
        if (isMetadataVisible) {
            loadMetadataView();
        } else {
            loadDataView();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;

        //Add single descriptor
        if (requestCode == Utils.DESCRIPTOR_DEFINE || requestCode == Utils.DESCRIPTOR_GET) {
            if (!data.getExtras().containsKey("descriptor"))
                return;

            String descriptorJson = data.getStringExtra("descriptor");
            Descriptor newDescriptor = new Gson().fromJson(descriptorJson, Descriptor.class);
            currentItem.addMetadataItem(newDescriptor);
            FavoriteMgr.updateFavoriteEntry(currentItem.getTitle(), currentItem, this);

            this.onResume();

        } else if (requestCode == Utils.METADATA_VALIDATION) {
            if (!data.getExtras().containsKey("favorite")) {
                throw new AssertionError("Received no favorite from metadata validation");
            }

            currentItem = new Gson().fromJson(data.getStringExtra("favorite"), FavoriteItem.class);
            this.onResume();

        } else if (requestCode == Utils.PICK_FILE_INTENT) {

            final Dialog dialog = new Dialog(this);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_import_file);
            dialog.setTitle(getResources().getString(R.string.importing_file));

            final EditText importDescription = (EditText) dialog.findViewById(R.id.import_file_description);
            final ProgressBar importProgress = (ProgressBar) dialog.findViewById(R.id.import_file_progress);
            final Button importSubmit = (Button) dialog.findViewById(R.id.import_file_submit);
            final TextView importHeader = (TextView) dialog.findViewById(R.id.import_file_header);

            importSubmit.setEnabled(false);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            dialog.show();

            new AsyncFileImporter(new AsyncTaskHandler<DataItem>() {
                @Override
                public void onSuccess(final DataItem result) {

                    importSubmit.setEnabled(true);
                    importHeader.setText(getString(R.string.file_imported_description));
                    importSubmit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String value = importDescription.getText().toString();

                            if (!value.equals("")) {
                                ArrayList<Descriptor> itemLevelDescriptors = result.getFileLevelMetadata();
                                for (Descriptor desc : itemLevelDescriptors) {
                                    if (desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                                        desc.setValue(value);
                                    }
                                }
                            }

                            currentItem.addDataItem(result);
                            FavoriteMgr.updateFavoriteEntry(favoriteName, currentItem, FavoriteDetailsActivity.this);
                            dialog.dismiss();
                            onResume();
                            FavoriteDetailsActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        }
                    });

                }

                @Override
                public void onFailure(Exception error) {
                    Toast.makeText(FavoriteDetailsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    onResume();
                    dialog.dismiss();
                }

                @Override
                public void onProgressUpdate(int value) {
                    importProgress.setProgress(value);
                    importHeader.setText("" + value + "%");
                }
            }).execute(FavoriteDetailsActivity.this, data, favoriteName);
        } else if (requestCode == Utils.ITEM_PREVIEW) {

            Bundle extras = data.getExtras();

            if (resultCode == Utils.DATA_ITEM_CHANGED) {
                if (extras == null
                        || !extras.containsKey("data_item")
                        || !extras.containsKey("position")) {
                    throw new AssertionError("Received no data from item preview");
                }

                DataItem item = new Gson()
                        .fromJson(data.getStringExtra("data_item"), DataItem.class);

                currentItem.getDataItems().remove(extras.getInt("position"));
                currentItem.addDataItem(item);

            } else if (resultCode == Utils.METADATA_ITEM_CHANGED) {
                if (extras == null
                        || !extras.containsKey("metadata_item")
                        || !extras.containsKey("position")) {
                    throw new AssertionError("Received no data from item preview");
                }

                Descriptor item = new Gson()
                        .fromJson(data.getStringExtra("metadata_item"), Descriptor.class);

                currentItem.getMetadataItems().remove(extras.getInt("position"));
                currentItem.addMetadataItem(item);
            }

            FavoriteMgr.updateFavoriteEntry(currentItem.getTitle(), currentItem, FavoriteDetailsActivity.this);
            onResume();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("favorite_name", favoriteName);
        outState.putString("current_item", new Gson().toJson(currentItem));
        outState.putBoolean("metadata_visible", isMetadataVisible);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_favorite_upload:
                SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
                if (!settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.dendro_confs_not_found_title))
                            .setMessage(getResources().getString(R.string.dendro_confs_not_found_message))
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(getResources().getDrawable(R.drawable.ab_cross))
                            .show();
                    return super.onOptionsItemSelected(item);
                }

                Intent mIntent = new Intent(this, SubmissionValidationActivity.class);
                mIntent.putExtra("favorite_name", favoriteName);
                startActivityForResult(mIntent, Utils.SUBMISSION_VALIDATION);


                break;

            case R.id.action_favorite_delete:
                //remove this favorite
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setTitle(R.string.edit_metadata_item_delete)
                        .setMessage(R.string.form_really_delete_favorite)
                        .setPositiveButton(R.string.form_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FileMgr.removeFavorite(favoriteName, FavoriteDetailsActivity.this);
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                getFragmentManager().popBackStack();
                                transaction.commit();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;

            case R.id.action_favorite_zip:
                final ProgressDialog dialog = ProgressDialog.show(this,
                        getString(R.string.upload_progress_creating_package),
                        getString(R.string.wait_queue_processing), false);

                new AsyncPackageCreator(new AsyncCustomTaskHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(FavoriteDetailsActivity.this, "OK", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        onResume();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Toast.makeText(FavoriteDetailsActivity.this, "FAILED", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onProgressUpdate(ProgressUpdateItem progress) {
                        dialog.setProgress(progress.getProgress());
                        dialog.setMessage(progress.getMessage());
                    }
                }).execute(favoriteName, this);
                break;

            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class dcClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteDetailsActivity.this);
            builder.setTitle("New value");
            final View mView = view;

            // Set up the input
            final EditText input = new EditText(FavoriteDetailsActivity.this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            builder.setView(input);
            builder.setMessage(getResources().getString(R.string.update_name_instructions));

            if (mView.getTag().equals(Utils.TITLE_TAG)) {
                input.setText(currentItem.getTitle());
            } else {
                input.setText(mToolbar.getSubtitle());
            }

            // Set up the buttons
            builder.setPositiveButton(getResources().getString(R.string.form_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (input.getText().toString().equals("")) {
                        Toast.makeText(FavoriteDetailsActivity.this, "Unchanged", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Update favorite's name (and DB entries ofc)
                    //TODO: move this
                    /*
                    if (mView.getTag().equals(Utils.TITLE_TAG)) {
                        if (!currentItem.getTitle().equals(input.getText().toString())) {
                            if (FileMgr.renameFavorite(favoriteName,
                                    input.getText().toString(),
                                    getActivity())) {
                                Toast.makeText(getActivity(), "Successfully updated name", Toast.LENGTH_LONG).show();
                                favoriteName = input.getText().toString();
                                currentItem.setTitle(favoriteName);
                                tv_title.setText(favoriteName);
                            }
                        }
                    }*/
                    onResume();
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

}