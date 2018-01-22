package pt.up.fe.alpha.labtablet.activities;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.async.AsyncFavoriteSetup;
import pt.up.fe.alpha.labtablet.async.AsyncTaskHandler;
import pt.up.fe.alpha.labtablet.database.AppDatabase;
import pt.up.fe.alpha.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.alpha.labtablet.fragments.ConfigurationFragment;
import pt.up.fe.alpha.labtablet.fragments.DrawerFragment;
import pt.up.fe.alpha.labtablet.fragments.HomeFragment;
import pt.up.fe.alpha.labtablet.fragments.ListChangelogFragment;
import pt.up.fe.alpha.labtablet.fragments.ListFavoritesFragment;
import pt.up.fe.alpha.labtablet.fragments.ListFormFragment;
import pt.up.fe.alpha.labtablet.models.Descriptor;
import pt.up.fe.alpha.labtablet.models.FavoriteItem;
import pt.up.fe.alpha.labtablet.utils.Utils;

public class MainActivity extends AppCompatActivity implements DrawerFragment.FragmentDrawerListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase.getDatabase(getApplicationContext());

        //create base folder
        final File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                getResources().getString(R.string.app_name));
        if (!path.exists()) {
            Log.i("CREATEDIR", "" + path.mkdirs());
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(path)));
        }

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        DrawerFragment drawerFragment = (DrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        displayView(0);

        //Incoming file/favorite?
        if (getIntent().getData() != null) {
            //Show dialog to import
            dispatchImportDialog(getIntent().getData());
        }
    }

    /**
     * Shows a dialog to ask whether the user wants to proceed importing the favorite or not
     * This should be called when the app is selected to open a zip file
     * @param data the uri from the intent-filter pointing to the file
     */
    private void dispatchImportDialog(final Uri data) {

        File importFile = new File(data.getPath());
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(importFile.getName())
                .setMessage(getString(R.string.import_favorite))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int which) {
                        //extract file to app folder
                        new AsyncFavoriteSetup(new AsyncTaskHandler<String>() {
                            @Override
                            public void onSuccess(String result) {
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Exception error) {
                                Toast.makeText(MainActivity.this, getString(R.string.unable_import_favorite), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onProgressUpdate(int value) {

                            }
                        }).execute(MainActivity.this, data.getPath());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.ic_import_contacts_white_48dp)
                ;

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    /**
     * Displaying fragment view for selected nav drawer list item
     */
    private void displayView(final int position) {
        Fragment fragment = null;
        String tag = "";

        switch (position) {
            case 0:
                fragment = new HomeFragment();
                tag = getString(R.string.title_home);
                break;
            case 1:
                fragment = new ListFavoritesFragment();
                tag = getString(R.string.title_list);
                break;
            case 2:
                fragment = new ListFormFragment();
                tag = getString(R.string.title_list_forms);
                break;
            case 3:
                fragment = new ListChangelogFragment();
                tag = getString(R.string.title_changelog);
                break;
            case 4:
                fragment = new ConfigurationFragment();
                tag = getString(R.string.title_configurations);
                break;
            default:
                break;
        }

        setToolbarVisibile(!tag.equals(getString(R.string.title_home)));

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment);
            fragmentTransaction.commit();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_container, fragment, tag);
            ft.commit();

            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(tag);
        }
    }


    @Override
    public void setTitle(CharSequence title) {

        if (title.equals(getString(R.string.app_name)))
            title = "";

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SUBMISSION_VALIDATION) {

            if (data == null)
                return;

            Toast.makeText(this, getString(R.string.uploaded_successfully), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows a dialog for the user to input the title and optional description when creating a new favorite/project
     */
    public void promptProjectCreation() {

        SharedPreferences settings = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        //Base configuration already loaded?
        if (!settings.contains(Utils.BASE_DESCRIPTORS_ENTRY)) {
            new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.no_profile))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setToolbarVisibile(false);
                        }
                    })
                    .setMessage(getResources().getString(R.string.no_profile))
                    .setIcon(R.drawable.ic_warning)
                    .show();
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_project, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);


        final EditText etDescription = (EditText) dialogView.findViewById(R.id.new_project_description);
        final EditText etTitle = (EditText) dialogView.findViewById(R.id.new_project_title);

        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = etTitle.getText().toString();
                if (itemName.equals("")) {
                    etTitle.setError(getString(R.string.empty_name));
                    return;
                }

                final File favoriteFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/" + getResources().getString(R.string.app_name) + "/"
                        + itemName);

                if (!favoriteFolder.exists()) {
                    Log.i("Make dir", "" + favoriteFolder.mkdir());
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.created_folder), Toast.LENGTH_SHORT).show();
                    MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(favoriteFolder)));
                }

                //Register favorite
                FavoriteItem newFavorite = new FavoriteItem(itemName);

                //Load default configuration
                ArrayList<Descriptor> baseCfg = FavoriteMgr.getBaseDescriptors(MainActivity.this);
                ArrayList<Descriptor> folderMetadata = new ArrayList<>();

                newFavorite.setTitle(itemName);

                for (Descriptor desc : baseCfg) {
                    String descName = desc.getName().toLowerCase();
                    if (descName.contains("date")) {
                        desc.validate();
                        desc.setValue(Utils.getDate());
                        folderMetadata.add(desc);
                    } else if (desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                        desc.validate();
                        String description = etDescription.getText().toString();
                        if (description.equals("")) {
                            description = getString(R.string.empty_description);
                        }
                        desc.setValue(description);
                        folderMetadata.add(desc);
                    }
                }

                newFavorite.setMetadataItems(folderMetadata);
                FavoriteMgr.registerFavorite(MainActivity.this, newFavorite);

                Intent intent = new Intent(MainActivity.this, FavoriteDetailsActivity.class);
                intent.putExtra("favorite_name", newFavorite.getTitle());
                startActivity(intent);
            }
        });

        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    public void setToolbarVisibile(boolean visibility) {
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if (bar == null)
            return;

        if (visibility)
            bar.show();
        else
            bar.hide();
    }

    //as in https://developer.android.com/training/beam-files/receive-files.html
    public String handleFileUri(Uri beamUri) {
        // Get the path part of the URI
        String fileName = beamUri.getPath();
        // Create a File object for this filename
        File copiedFile = new File(fileName);
        // Get a string containing the file's parent directory
        return copiedFile.getParent();
    }
}

