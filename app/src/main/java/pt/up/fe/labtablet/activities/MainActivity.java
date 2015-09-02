package pt.up.fe.labtablet.activities;


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

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.fragments.ConfigurationFragment;
import pt.up.fe.labtablet.fragments.DrawerFragment;
import pt.up.fe.labtablet.fragments.FavoriteDetailsFragment;
import pt.up.fe.labtablet.fragments.HomeFragment;
import pt.up.fe.labtablet.fragments.ListChangelogFragment;
import pt.up.fe.labtablet.fragments.ListFavoritesFragment;
import pt.up.fe.labtablet.fragments.ListFormFragment;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.Utils;

public class MainActivity extends AppCompatActivity implements DrawerFragment.FragmentDrawerListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create base folder
        final File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                getResources().getString(R.string.app_name));
        if (!path.exists()) {
            Log.i("CREATEDIR", "" + path.mkdirs());
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(path)));
        }

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerFragment drawerFragment = (DrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        displayView(0);
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
    private void displayView(int position) {
        // remove the main content by replacing fragments
        Fragment fragment = null;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("");
        }

        String tag = "";
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                tag = getString(R.string.title_home);
                break;
            case 1:
                promptProjectCreation();
                break;
            case 2:
                fragment = new ListFavoritesFragment();
                tag = getString(R.string.title_list);
                break;
            case 3:
                fragment = new ListFormFragment();
                tag = getString(R.string.title_list_forms);
                break;
            case 4:
                fragment = new ListChangelogFragment();
                tag = getString(R.string.title_changelog);
                break;
            case 5:
                fragment = new ConfigurationFragment();
                tag = getString(R.string.title_configurations);
                break;
            default:
                break;
        }

        if (fragment != null) {
            //the favorite creation view should never be added to the back stack

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
            fragmentTransaction.replace(R.id.frame_container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_container, fragment, tag);
            ft.commit();

            getSupportActionBar().setTitle(tag);
        }
    }

    @Override
    public void setTitle(CharSequence title) {

        if (title.equals(getString(R.string.app_name)))
            title = "";

        if (getSupportActionBar() == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("MainActivity" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, MainActivity.this);
        } else {
            getSupportActionBar().setTitle(title);
        }

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
                    .setTitle("Application Profile not loaded")
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
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

                FragmentTransaction transaction = MainActivity.this.getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                FavoriteDetailsFragment favoriteDetail = new FavoriteDetailsFragment();
                Bundle args = new Bundle();
                args.putString("favorite_name", newFavorite.getTitle());
                favoriteDetail.setArguments(args);
                transaction.replace(R.id.frame_container, favoriteDetail);
                transaction.addToBackStack(null);
                transaction.commit();
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

}

