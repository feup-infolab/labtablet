package pt.up.fe.labtablet.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.DescriptorsListAdapter;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.models.AssociationItem;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.Utils;

public class DescriptorPickerActivity extends Activity implements ActionBar.OnNavigationListener {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private DescriptorsListAdapter mAdapter;
    private SharedPreferences settings;
    private ArrayList<Descriptor> mDescriptors;
    private ArrayList<Descriptor> displayedDescriptors;
    private String extension;
    private String favoriteName;
    private Integer returnMode;
    private Bundle mBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descriptor_picker);
        mBundle = getIntent().getExtras();
        returnMode = mBundle.getInt("returnMode");
        favoriteName = mBundle.getString("favoriteName");

        settings = getSharedPreferences(
                getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        if (!settings.contains(Utils.BASE_DESCRIPTORS_ENTRY)) {
            Toast.makeText(getApplication(),
                    getResources().getString(R.string.base_configuration_not_found),
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!mBundle.containsKey("file_extension")) {
            extension = "";
        } else {
            extension = mBundle.getString("file_extension");
        }

        // Set up the action bar to show a dropdown list.
        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("DescriptorPicker" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, DescriptorPickerActivity.this);
        } else {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            actionBar.setDisplayHomeAsUpEnabled(true);
            // Set up the dropdown list navigation in the action bar.
            actionBar.setListNavigationCallbacks(
                    // Specify a SpinnerAdapter to populate the dropdown list.
                    new ArrayAdapter<>(
                            actionBar.getThemedContext(),
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            new String[]{
                                    getResources().getString(R.string.recommended),
                                    getResources().getString(R.string.from_dendro),
                                    getResources().getString(R.string.all)
                            }),
                    this);
        }

        ListView lv_descriptors;
        lv_descriptors = (ListView) findViewById(R.id.lv_descriptors);

        displayedDescriptors = new ArrayList<>();

        mDescriptors = FavoriteMgr.getBaseDescriptors(this);

        //set list adapter
        mAdapter = new DescriptorsListAdapter(this, displayedDescriptors);
        lv_descriptors.setAdapter(mAdapter);

        lv_descriptors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                final Descriptor selectedDescriptor = displayedDescriptors.get(position);

                //save empty descriptor as an association
                if (returnMode == Utils.DESCRIPTOR_GET) {
                    String descriptorJson = mBundle.getString("descriptor");
                    Descriptor emptyDescriptor = new Gson().fromJson(descriptorJson, Descriptor.class);
                    selectedDescriptor.setValue(emptyDescriptor.getValue());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("descriptor", new Gson().toJson(selectedDescriptor, Descriptor.class));
                    setResult(RESULT_OK, returnIntent);
                    finish();
                } else if (returnMode == Utils.DESCRIPTOR_ASSOCIATE) {
                    String associationsJson = settings.getString(Utils.ASSOCIATIONS_CONFIG_ENTRY, "");
                    ArrayList<AssociationItem> mAssociations =
                            new Gson().fromJson(associationsJson, Utils.ARRAY_ASSOCIATION_ITEM);

                    for (AssociationItem item : mAssociations) {
                        if (item.getFileExtension().contains(extension)) {
                            item.setDescriptor(selectedDescriptor);
                        }
                    }
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove(Utils.ASSOCIATIONS_CONFIG_ENTRY);
                    editor.putString(Utils.ASSOCIATIONS_CONFIG_ENTRY, new Gson().toJson(mAssociations));
                    editor.apply();
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
                //select descriptor and set its value
                else if (returnMode == Utils.DESCRIPTOR_DEFINE) {
                    selectedDescriptor.setState(Utils.DESCRIPTOR_STATE_VALIDATED);
                    AlertDialog.Builder builder = new AlertDialog.Builder(DescriptorPickerActivity.this);
                    builder.setTitle(selectedDescriptor.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(DescriptorPickerActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            selectedDescriptor.getAllowed_values()
                                    .toArray(new String[selectedDescriptor.getAllowed_values().size()]));

                    final MultiAutoCompleteTextView autoCompleteTextView = new MultiAutoCompleteTextView(DescriptorPickerActivity.this);
                    autoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    autoCompleteTextView.setAdapter(adapter);
                    autoCompleteTextView.setThreshold(1);

                    autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {

                        @Override
                        public boolean onTouch(View v, MotionEvent event)
                        {
                            autoCompleteTextView.showDropDown();
                            return false;
                        }
                    });

                    // Set up the input
                    builder.setView(autoCompleteTextView);

                    // Set up the buttons
                    builder.setPositiveButton(getResources().getString(R.string.form_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (autoCompleteTextView.getText().toString().equals("")) {
                                autoCompleteTextView.setError("The value shall not be empty.");
                            } else {
                                selectedDescriptor.setValue(autoCompleteTextView.getText().toString());
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("descriptor", new Gson().toJson(selectedDescriptor, Descriptor.class));
                                setResult(RESULT_OK, returnIntent);
                                dialog.dismiss();
                                finish();
                            }
                        }
                    });

                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Toast.makeText(getApplication(), getResources().getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                    builder.show();
                }
            }
        });
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            if (getActionBar() == null) {
                ChangelogItem item = new ChangelogItem();
                item.setMessage("DescriptorPicker" + "Couldn't get actionbar");
                item.setTitle(getResources().getString(R.string.developer_error));
                item.setDate(Utils.getDate());
                ChangelogManager.addLog(item, DescriptorPickerActivity.this);
                return;
            }
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
        mDescriptors = FavoriteMgr.getBaseDescriptors(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        if (getActionBar() == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("DescriptorPicker" + "Couldn't get actionbar");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, DescriptorPickerActivity.this);
            return;
        }

        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.descriptor_picker, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        switch (position) {
            case 0: //recommended
                displayedDescriptors = new ArrayList<>();
                for (Descriptor d : mDescriptors) {
                    if (d.getTag().contains(extension)) {
                        displayedDescriptors.add(d);
                    }
                }

                if (displayedDescriptors.size() == 0) {
                    Toast.makeText(this, getResources().getString(R.string.no_dendro_recommended_descriptors), Toast.LENGTH_SHORT).show();
                    if (getActionBar() == null) {
                        ChangelogItem item = new ChangelogItem();
                        item.setMessage("DescriptorPicker" + "Couldn't get actionbar; recommended descriptors");
                        item.setTitle(getResources().getString(R.string.developer_error));
                        item.setDate(Utils.getDate());
                        ChangelogManager.addLog(item, DescriptorPickerActivity.this);
                        break;
                    }
                    getActionBar().setSelectedNavigationItem(2);
                } else {
                    mAdapter.clear();
                    mAdapter.addAll(displayedDescriptors);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case 1: //Dendro
                if (!settings.contains(favoriteName + "_dendro")) {
                    Toast.makeText(this, getResources().getString(R.string.no_recommended_descriptors), Toast.LENGTH_SHORT).show();
                    ActionBar mActionBar = getActionBar();
                    if (mActionBar == null) {
                        ChangelogItem item = new ChangelogItem();
                        item.setMessage("DescriptorPickerActivity" + "Couldn't get actionbar. Compatibility mode layout");
                        item.setTitle(getResources().getString(R.string.developer_error));
                        item.setDate(Utils.getDate());
                        ChangelogManager.addLog(item, DescriptorPickerActivity.this);
                    } else {
                        getActionBar().setSelectedNavigationItem(2);
                    }

                    break;
                }

                displayedDescriptors = new Gson().fromJson(
                        settings.getString(favoriteName + "_dendro", ""), Utils.ARRAY_DESCRIPTORS);
                mAdapter.clear();
                mAdapter.addAll(displayedDescriptors);
                mAdapter.notifyDataSetChanged();
                break;
            case 2: //ALL
                displayedDescriptors = mDescriptors;
                mAdapter.clear();
                mAdapter.addAll(displayedDescriptors);
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }
}
