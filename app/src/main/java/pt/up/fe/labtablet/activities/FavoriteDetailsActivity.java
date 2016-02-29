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
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
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
import java.util.ArrayList;
import java.util.List;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.async.AsyncCustomTaskHandler;
import pt.up.fe.labtablet.async.AsyncFileImporter;
import pt.up.fe.labtablet.async.AsyncPackageCreator;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.fragments.FavoriteViewFragment;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.models.ProgressUpdateItem;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

public class FavoriteDetailsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener{

    private FavoriteItem currentItem;
    private String favoriteName;

    private ViewPager viewPager;
    private TabLayout.Tab activeTab;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_view);

        FloatingActionButton bt_new_metadata
                = (FloatingActionButton) findViewById(R.id.bt_new_metadata);


        Button bt_fieldMode = (Button) findViewById(R.id.bt_field_mode);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);


        if (savedInstanceState != null) {
            currentItem = new Gson().fromJson(savedInstanceState.getString("current_item"), FavoriteItem.class);
        } else {
            Bundle extras = getIntent().getExtras();
            favoriteName = extras.getString("favorite_name");
        }

        currentItem = FavoriteMgr.getFavorite(this, favoriteName);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
        activeTab = tabLayout.getTabAt(0);

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

        bt_new_metadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (activeTab == null || activeTab.getText() == null)
                    return;

                if (activeTab.getText().equals("metadata")) {
                    Intent myIntent = new Intent(FavoriteDetailsActivity.this, DescriptorPickerActivity.class);
                    myIntent.putExtra("file_extension", "");
                    myIntent.putExtra("favoriteName", favoriteName);
                    myIntent.putExtra("returnMode", Utils.DESCRIPTOR_DEFINE);
                    startActivityForResult(myIntent, Utils.DESCRIPTOR_DEFINE);
                } else {
                    Toast.makeText(FavoriteDetailsActivity.this, "Choose the file", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent, Utils.PICK_FILE_INTENT);
                }
            }
        });
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
            setupViewPager(viewPager);
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
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        activeTab = tab;
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

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

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(FavoriteViewFragment.newInstance("metadata", currentItem.getMetadataItems()), "metadata");
        adapter.addFragment(FavoriteViewFragment.newInstance("data", currentItem.getDataItems()), "data");
        viewPager.setAdapter(adapter);
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
}