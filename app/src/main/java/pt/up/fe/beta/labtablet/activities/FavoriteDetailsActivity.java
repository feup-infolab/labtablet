package pt.up.fe.beta.labtablet.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.async.AsyncCustomTaskHandler;
import pt.up.fe.beta.labtablet.async.AsyncFileImporter;
import pt.up.fe.beta.labtablet.async.AsyncPackageCreator;
import pt.up.fe.beta.labtablet.async.AsyncTaskHandler;
import pt.up.fe.beta.labtablet.database.AppDatabase;
import pt.up.fe.beta.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.beta.labtablet.db_handlers.FormMgr;
import pt.up.fe.beta.labtablet.fragments.FavoriteViewFragment;
import pt.up.fe.beta.labtablet.models.DataItem;
import pt.up.fe.beta.labtablet.models.Dendro.Sync;
import pt.up.fe.beta.labtablet.models.Descriptor;
import pt.up.fe.beta.labtablet.models.FavoriteItem;
import pt.up.fe.beta.labtablet.models.Form;
import pt.up.fe.beta.labtablet.models.FormInstance;
import pt.up.fe.beta.labtablet.models.ProgressUpdateItem;
import pt.up.fe.beta.labtablet.models.SeaBioData.FormExportItem;
import pt.up.fe.beta.labtablet.utils.FileMgr;
import pt.up.fe.beta.labtablet.utils.Utils;

public class FavoriteDetailsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private FavoriteItem currentItem;
    private String favoriteName;

    private ViewPager viewPager;
    private TabLayout.Tab activeTab;
    private Toolbar mToolbar;
    private FloatingActionButton fab;

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_view);

        fab = (FloatingActionButton) findViewById(R.id.bt_new_metadata);


        Button bt_fieldMode = (Button) findViewById(R.id.bt_field_mode);


        if (savedInstanceState != null) {
            currentItem = new Gson().fromJson(savedInstanceState.getString("current_item"), FavoriteItem.class);
        } else {
            Bundle extras = getIntent().getExtras();
            favoriteName = extras.getString("favorite_name");
        }

        currentItem = FavoriteMgr.getFavorite(this, favoriteName);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
        activeTab = tabLayout.getTabAt(0);

        getFragmentManager().findFragmentByTag("metadata_view");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(currentItem.getTitle());
        mToolbar.setSubtitle(currentItem.getDescription());

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String rootFolder = Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_name) + File.separator + currentItem.getTitle();
        ((TextView) findViewById(R.id.favorite_stats)).setText(
                getString(R.string.favorite_info,
                        FileMgr.humanReadableByteCount(FileMgr.folderSize(new File(rootFolder)), false),
                        currentItem.getMetadataItems().size()));


        bt_fieldMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavoriteDetailsActivity.this, FieldModeActivity.class);
                intent.putExtra("favorite_name", favoriteName);
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (activeTab == null || activeTab.getText() == null)
                    return;

                String activeTabName = "" + activeTab.getText();
                Intent myIntent;
                switch (activeTabName) {
                    case "metadata":
                        myIntent = new Intent(FavoriteDetailsActivity.this, DescriptorPickerActivity.class);
                        myIntent.putExtra("file_extension", "");
                        myIntent.putExtra("favoriteName", favoriteName);
                        myIntent.putExtra("returnMode", Utils.DESCRIPTOR_DEFINE);
                        startActivityForResult(myIntent, Utils.DESCRIPTOR_DEFINE);
                        break;
                    case "sync":
                        fab.setVisibility(View.INVISIBLE);
                        myIntent = new Intent(FavoriteDetailsActivity.this, DescriptorPickerActivity.class);
                        myIntent.putExtra("file_extension", "");
                        myIntent.putExtra("favoriteName", favoriteName);
                        myIntent.putExtra("returnMode", Utils.DESCRIPTOR_DEFINE);
                        startActivityForResult(myIntent, Utils.DESCRIPTOR_DEFINE);
                        break;
                    case "data":
                        Toast.makeText(FavoriteDetailsActivity.this, "Choose the file", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        startActivityForResult(intent, Utils.PICK_FILE_INTENT);
                        break;
                    case "forms":
                        final ArrayList<Form> forms = FormMgr.getCurrentBaseForms(FavoriteDetailsActivity.this);

                        final CharSequence values[] = new CharSequence[forms.size()];
                        for (int i = 0; i < forms.size(); ++i) {
                            values[i] = forms.get(i).getFormName();
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteDetailsActivity.this);
                        builder.setTitle(getResources().getString(R.string.select_form_solve));
                        builder.setItems(values, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent formIntent = new Intent(FavoriteDetailsActivity.this, FormSolverActivity.class);
                                formIntent.putExtra("form",
                                        new Gson().toJson(new FormInstance(forms.get(which))));
                                startActivityForResult(formIntent, Utils.SOLVE_FORM);
                            }
                        });
                        builder.show();
                        break;
                }
            }
        });


        PackageManager pm = getPackageManager();
        // Check whether NFC is available on device
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            // NFC is not available on the device.
            Toast.makeText(this, "The device does not has NFC hardware.",
                    Toast.LENGTH_SHORT).show();
        }
        // Check whether device is running Android 4.1 or higher
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            // Android Beam feature is not supported.
            Toast.makeText(this, "Android Beam is not supported.",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            // NFC and Android Beam file transfer is supported.
            Toast.makeText(this, "Android Beam is supported on your device.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.favorite_view_menu, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (favoriteName != null) {
            currentItem = FavoriteMgr.getFavorite(FavoriteDetailsActivity.this, favoriteName);
            mToolbar.setSubtitle(currentItem.getDescription());
            mToolbar.setTitle(currentItem.getTitle());

            for (int i = 0 ; i < viewPager.getAdapter().getCount(); ++i) {
                Fragment f = getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + i);

                if (f instanceof FavoriteViewFragment) {
                    ((FavoriteViewFragment) f).notifyItemsChanged(currentItem);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //TODO NELSON THIS IS VERY UGLY CODE BUT SEEMS TO BE UPDATING THE SYNC LIST
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        if (data == null)
            return;

        Bundle extras = data.getExtras();
        switch (requestCode) {
            case Utils.DESCRIPTOR_DEFINE:
            case Utils.DESCRIPTOR_GET:
                if (!data.getExtras().containsKey("descriptor"))
                    return;

                String descriptorJson = data.getStringExtra("descriptor");
                Descriptor newDescriptor = new Gson().fromJson(descriptorJson, Descriptor.class);
                currentItem.addMetadataItem(newDescriptor);
                FavoriteMgr.updateFavoriteEntry(currentItem.getTitle(), currentItem, this);
                this.onResume();
                break;

            case Utils.METADATA_VALIDATION:
                if (!data.getExtras().containsKey("favorite")) {
                    throw new AssertionError("Received no favorite from metadata validation");
                }

                currentItem = new Gson().fromJson(data.getStringExtra("favorite"), FavoriteItem.class);
                this.onResume();
                break;

            case Utils.PICK_FILE_INTENT:

                final Dialog dialog = new Dialog(this);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_import_file);
                dialog.setTitle(getResources().getString(R.string.importing_file));

                final EditText importDescription = (EditText) dialog.findViewById(R.id.import_file_description);
                final ProgressBar importProgress = (ProgressBar) dialog.findViewById(R.id.import_file_progress);
                final Button importSubmit = (Button) dialog.findViewById(R.id.import_file_submit);
                final TextView importHeader = (TextView) dialog.findViewById(R.id.import_file_header);

                importSubmit.setEnabled(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                }

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
                break;


            case Utils.ITEM_PREVIEW:
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
                break;

            case Utils.SOLVE_FORM:

                if (extras == null
                        || !extras.containsKey("form")) {
                    throw new AssertionError("Received no data from form activity");
                }

                FormInstance f = new Gson().fromJson(extras.getString("form"), FormInstance.class);
                currentItem.addFormInstance(f);
                FavoriteMgr.updateFavoriteEntry(currentItem.getTitle(), currentItem, this);
                onResume();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("favorite_name", favoriteName);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_favorite_upload:
                dispatchDendroUpload();
                /*final String options[] = {"SeaBioData", "Dendro"};
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(FavoriteDetailsActivity.this);
                builder.setTitle(getString(R.string.dialog_pick_context_title));
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (options[which]) {
                            case "SeaBioData":
                                try {
                                    dispatchSBDExport();
                                } catch (IOException e) {
                                    Log.e("SBD", e.getMessage());
                                }
                                break;

                            case "Dendro":
                                dispatchDendroUpload();

                        }
                    }
                });
                builder.show();
                */
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
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;

            /*case R.id.action_favorite_zip:
                final ProgressDialog dialog = ProgressDialog.show(this,
                        getString(R.string.upload_progress_creating_package),
                        getString(R.string.wait_queue_processing), false);

                new AsyncPackageCreator(new AsyncCustomTaskHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(FavoriteDetailsActivity.this, "OK", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        sendFile();
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
                break;*/
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Generates the necessary files for the forms upload to the SeaBioData repository
     */
    private void dispatchSBDExport() throws IOException {


        SharedPreferences settings = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        if (!settings.contains(Utils.TAG_SBD_USERNAME)) {
            Toast.makeText(this, getString(R.string.sbd_username_missing_export), Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, ArrayList<FormInstance>> groupedInstances = new HashMap<>();
        for (FormInstance fi : currentItem.getLinkedForms()) {
            if (groupedInstances.containsKey(fi.getParent())) {
                groupedInstances.get(fi.getParent()).add(fi);
                continue;
            }

            ArrayList<FormInstance> newInstances = new ArrayList<>();
            newInstances.add(fi);
            groupedInstances.put(fi.getParent(), newInstances);
        }


        for (String key : groupedInstances.keySet()) {
            FormExportItem exportItem = new FormExportItem(groupedInstances.get(key), settings.getString(Utils.TAG_SBD_USERNAME, ""));
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + key + "_" + new Date().toString() + ".json");

            if(file.createNewFile()) {
                OutputStream fo = new FileOutputStream(file);
                fo.write(new Gson().toJson(exportItem).getBytes());
                fo.close();
            }
        }

        Toast.makeText(getApplicationContext(), getString(R.string.sbd_dorms_exported_successfully), Toast.LENGTH_SHORT).show();
    }


    /**
     * Checks the dendro setup entries and launches the upload preparation
     * activity if they are correct
     */
    private void dispatchDendroUpload() {
        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        if (!settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
            new AlertDialog.Builder(FavoriteDetailsActivity.this)
                    .setTitle(getResources().getString(R.string.dendro_confs_not_found_title))
                    .setMessage(getResources().getString(R.string.dendro_confs_not_found_message))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(getResources().getDrawable(R.drawable.ab_cross))
                    .show();
        }

        Intent mIntent = new Intent(this, SubmissionValidationActivity.class);
        mIntent.putExtra("favorite_name", favoriteName);
        startActivityForResult(mIntent, Utils.SUBMISSION_VALIDATION);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        activeTab = tab;
        viewPager.setCurrentItem(tab.getPosition());

        if (tab.getText() == null)
            return;

        String activeTabName = "" + activeTab.getText();
        fab.setVisibility(View.VISIBLE);
        switch (activeTabName) {
            case "metadata":

                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_border_color_white_24dp));
                break;
            case "sync":
                fab.setVisibility(View.INVISIBLE);
                //fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_border_color_white_24dp));
                break;
            case "data":
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_attachment_white_24dp));
                break;
            case "forms":
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_assignment_white_24dp));
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    /**
     * Handles form instance removal from the Form tab
     * @param form form that is to be removed
     */
    public void notifyFormInstanceRemoved(FormInstance form, int position) {
        FavoriteItem item =  FavoriteMgr.getFavorite(this, favoriteName);

        item.getLinkedForms().remove(form);
        FavoriteMgr.updateFavoriteEntry(favoriteName, item, this);
        Toast.makeText(this, "Instance removed", Toast.LENGTH_SHORT).show();
        onResume();
    }

    /**
     * Handles data item removal from the fragment
     * @param items updated items for the active favorite
     */
    public void notifyDataItemRemoved(ArrayList<DataItem> items) {
        FavoriteItem item =  FavoriteMgr.getFavorite(this, favoriteName);
        item.setDataItems(items);
        FavoriteMgr.updateFavoriteEntry(favoriteName, item, this);
        Toast.makeText(this, "Instance removed", Toast.LENGTH_SHORT).show();
        onResume();
    }

    /**
     * Handles metadata item removal from the fragment
     * @param items updated items for the active favorite
     */
    public void notifyMetadataItemRemoved(ArrayList<Descriptor> items) {
        FavoriteItem item =  FavoriteMgr.getFavorite(this, favoriteName);
        item.setMetadataItems(items);
        FavoriteMgr.updateFavoriteEntry(favoriteName, item, this);
        Toast.makeText(this, "Instance removed", Toast.LENGTH_SHORT).show();
        onResume();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        currentItem = FavoriteMgr.getFavorite(this, currentItem.getTitle());
        //currentItem.setSyncItems(CreateFakeSyncItems());
        currentItem.setSyncItems(getSyncsWithDendro(currentItem.getTitle()));
        adapter.addFragment(FavoriteViewFragment.newInstance("metadata", currentItem.getMetadataItems()), "metadata");
        adapter.addFragment(FavoriteViewFragment.newInstance("data", currentItem.getDataItems()), "data");
        adapter.addFragment(FavoriteViewFragment.newInstance("forms", currentItem.getLinkedForms()), "forms");
        adapter.addFragment(FavoriteViewFragment.newInstance("sync", currentItem.getSyncItems()), "sync");
        viewPager.setAdapter(adapter);
    }

    private ArrayList<Sync> CreateFakeSyncItems(){
        ArrayList<Sync> res = new ArrayList<Sync>();

        String title = "Base Data";
        String dendroInstanceAddress = "http://dendro-prd.fe.up.pt:3007";
        String folderUri = "/r/folder/bccadc68-f8bc-419d-b924-f0b55b06e876";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 6);
        Date dateRepresentation = cal.getTime();

        boolean syncOK  = true;

        Sync d = new Sync(
                title,
                dendroInstanceAddress,
                folderUri,
                dateRepresentation,
                true
        );

        res.add(d);
        return res;
    }

    private ArrayList<Sync> getSyncsWithDendro(String labtabletProjectTitle)
    {
        ArrayList<Sync> res = new ArrayList<Sync>();
        //List<Sync> syncs = Sync.getAllSync(AppDatabase.getDatabase(getApplicationContext()));
        List<Sync> syncs = Sync.getAllWithTitleSync(AppDatabase.getDatabase(getApplicationContext()), labtabletProjectTitle);

        for (int i = 0; i < syncs.size(); ++i)
        {
            Sync sync =  syncs.get(i);
            res.add(sync);
        }
        return res;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void sendFile() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Check whether NFC is enabled on device
        if(!nfcAdapter.isEnabled()){
            // NFC is disabled, show the settings UI
            // to enable NFC
            Toast.makeText(this, "Please enable NFC.",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        // Check whether Android Beam feature is enabled on device
        else if(!nfcAdapter.isNdefPushEnabled()) {
            // Android Beam is disabled, show the settings UI
            // to enable Android Beam
            Toast.makeText(this, "Please enable Android Beam.",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
        }
        else {
            // NFC and Android Beam both are enabled

            // File to be transferred
            // For the sake of this tutorial I've placed an image
            // named 'wallpaper.png' in the 'Pictures' directory
            String fileName = currentItem.getTitle() + ".zip";

            // Retrieve the path to the user's public pictures directory
            File fileDirectory = Environment
                    .getExternalStorageDirectory();

            // Create a new file using the specified directory and name
            File fileToTransfer = new File(fileDirectory, fileName);
            fileToTransfer.setReadable(true, false);

            nfcAdapter.setBeamPushUris(
                    new Uri[]{Uri.fromFile(fileToTransfer)}, this);
        }
    }
}