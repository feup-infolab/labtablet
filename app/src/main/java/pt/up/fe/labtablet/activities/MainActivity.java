package pt.up.fe.labtablet.activities;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.NavDrawerListAdapter;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.fragments.ConfigurationFragment;
import pt.up.fe.labtablet.fragments.HomeFragment;
import pt.up.fe.labtablet.fragments.ListChangelogFragment;
import pt.up.fe.labtablet.fragments.ListFavoritesFragment;
import pt.up.fe.labtablet.fragments.ListFormFragment;
import pt.up.fe.labtablet.fragments.NewFavoriteBaseFragment;
import pt.up.fe.labtablet.fragments.SearchFragment;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.NavDrawerItem;
import pt.up.fe.labtablet.utils.Utils;

public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;


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

        mTitle = mDrawerTitle = getTitle();

        TypedArray navMenuIcons;
        ArrayList<NavDrawerItem> navDrawerItems;
        NavDrawerListAdapter adapter;
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);


        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        navDrawerItems = new ArrayList<NavDrawerItem>();

        //HOME, SEARCH, NEW PROJECT ,MY FAVORITES, UPDATES, CONFIGURATIONS
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1), true, "10+"));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        ActionBar mActionBar = getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("MainActivity" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, MainActivity.this);
        } else {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            displayView(0);
        }
        mDrawerLayout.openDrawer(mDrawerList);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // remove the main content by replacing fragments
        Fragment fragment = null;
        String tag = "";
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                tag = "HOME";
                break;
            case 1:
                fragment = new SearchFragment();
                tag = "SEARCH";
                break;
            case 2:
                fragment = new NewFavoriteBaseFragment();
                tag = "NEWFAV";
                break;
            case 3:
                fragment = new ListFavoritesFragment();
                tag = "LISTFAV";
                break;
            case 4:
                fragment = new ListFormFragment();
                tag = "LISTFORM";
                break;
            case 5:
                fragment = new ListChangelogFragment();
                tag = "LOG";
                break;
            case 6:
                fragment = new ConfigurationFragment();
                tag = "CONF";
                break;
            default:
                break;
        }

        if (fragment != null) {
            //the favorite creation view should never be added to the back stack

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment, tag)
                    .addToBackStack(tag)
                    .commit();

            // remove selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        ActionBar mActionBar = getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("MainActivity" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, MainActivity.this);
        } else {
            mActionBar.setTitle(mTitle);
        }

    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("", "REQ:" + requestCode + " RES:" + resultCode);
        if (requestCode == Utils.SUBMISSION_VALIDATION) {

            if (data == null)
                return;

            Toast.makeText(this, getString(R.string.uploaded_successfully), Toast.LENGTH_SHORT).show();
            //Remove favorite is now disabled
            /*
            FileMgr.removeFavorite(data.getStringExtra("favoriteName"), this);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
            ListFavoritesFragment favoriteList = new ListFavoritesFragment();
            transaction.replace(R.id.frame_container, favoriteList);
            transaction.commit();
            */
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {

        Fragment displayedFragment = getFragmentManager().findFragmentByTag("HOME");

        if (displayedFragment.isVisible()) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(R.string.exit)
                    .setMessage(R.string.really_exit)
                    .setPositiveButton(R.string.form_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
            transaction.replace(R.id.frame_container, displayedFragment);
            transaction.addToBackStack("HOME");
            transaction.commit();
        }
    }


    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }


}

