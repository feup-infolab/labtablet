package pt.up.fe.labtablet.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.fragments.ConfigurationFragment;
import pt.up.fe.labtablet.fragments.DrawerFragment;
import pt.up.fe.labtablet.fragments.HomeFragment;
import pt.up.fe.labtablet.fragments.ListChangelogFragment;
import pt.up.fe.labtablet.fragments.ListFavoritesFragment;
import pt.up.fe.labtablet.fragments.ListFormFragment;
import pt.up.fe.labtablet.fragments.NewFavoriteBaseFragment;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.utils.Utils;

public class MainActivity extends AppCompatActivity implements DrawerFragment.FragmentDrawerListener {

    // used to store app title
    private CharSequence mTitle;


    private Toolbar mToolbar;

    private DrawerFragment drawerFragment;


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

        // enabling action bar app icon and behaving it as toggle button

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerFragment = (DrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
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
                fragment = new NewFavoriteBaseFragment();
                tag = getString(R.string.title_new_favorite);
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
            fragmentTransaction.replace(R.id.frame_container, fragment);
            fragmentTransaction.commit();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            //ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            ft.replace(R.id.frame_container, fragment, tag);
            ft.commit();


            getSupportActionBar().setTitle(tag);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;

        if (mTitle.equals(getString(R.string.app_name)))
            mTitle = "";

        if (getSupportActionBar() == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("MainActivity" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, MainActivity.this);
        } else {
            getSupportActionBar().setTitle(mTitle);
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
}

