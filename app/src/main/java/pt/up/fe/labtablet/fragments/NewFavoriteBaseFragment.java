package pt.up.fe.labtablet.fragments;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.application.LabTablet;
import pt.up.fe.labtablet.async.AsyncProjectListFetcher;
import pt.up.fe.labtablet.async.AsyncRecommendationsLoader;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Dendro.Project;
import pt.up.fe.labtablet.models.Dendro.ProjectListResponse;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Fragment to create a new favorite in the application
 */
public class NewFavoriteBaseFragment extends Fragment {

    private EditText favoriteName;
    private EditText favoriteDescription;

    private Button bt_load_suggestions;
    private ProgressDialog mDialog;
    private SharedPreferences.Editor editor;
    private String projectName;
    private ArrayList<Project> availableProjects;
    private ArrayList<Descriptor> recommendations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_new_dataset, container, false);

        favoriteName = (EditText) rootView.findViewById(R.id.et_dataset_name);
        favoriteDescription = (EditText) rootView.findViewById(R.id.et_dataset_description);
        bt_load_suggestions = (Button) rootView.findViewById(R.id.new_favorite_proj_load);

        ActionBar mActionBar = getActivity().getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("NewFavorite" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, getActivity());
        } else {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setHasOptionsMenu(true);
        favoriteName.requestFocus();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("recommendations")) {
                recommendations = new Gson().fromJson(
                        savedInstanceState.get("recommendations").toString(),
                        Utils.ARRAY_DESCRIPTORS
                );

                bt_load_suggestions.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_check), null, null, null
                );

                bt_load_suggestions.setText(getResources().getString(R.string.successul_imported_recommendations));
                bt_load_suggestions.setEnabled(false);
            }
        }


        bt_load_suggestions.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                mDialog = ProgressDialog.show(getActivity(), "",
                        getResources().getString(R.string.loading), true);


                new AsyncProjectListFetcher(new AsyncTaskHandler<ProjectListResponse>() {
                    @Override
                    public void onSuccess(ProjectListResponse result) {
                        if (getActivity() == null) {
                            return;
                        }
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }

                        availableProjects = result.getProjects();
                        CharSequence values[] = new CharSequence[result.getProjects().size()];
                        for (int i = 0; i < result.getProjects().size(); ++i) {
                            values[i] = result.getProjects().get(i).getDcterms().getTitle();
                        }

                        if (getActivity() == null) {
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getResources().getString(R.string.select_project_above));
                        builder.setItems(values, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                projectName = availableProjects.get(which).getDdr().getHandle();
                                new AsyncRecommendationsLoader(new AsyncTaskHandler<ArrayList<Descriptor>>() {
                                    @Override
                                    public void onSuccess(ArrayList<Descriptor> result) {
                                        if (getActivity() == null) {
                                            return;
                                        }
                                        bt_load_suggestions.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                                getResources().getDrawable(R.drawable.ic_check), null, null, null
                                        );

                                        bt_load_suggestions.setText(getResources().getString(R.string.successul_imported_recommendations));
                                        bt_load_suggestions.setEnabled(false);
                                        recommendations = result;

                                        if (mDialog != null) {
                                            mDialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception error) {
                                        if (getActivity() == null) {
                                            return;
                                        }
                                        bt_load_suggestions.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                                null, getResources().getDrawable(R.drawable.ic_error), null, null
                                        );

                                        if (error != null) {
                                            bt_load_suggestions.setText(error.getMessage());
                                        }

                                        if (mDialog != null) {
                                            mDialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onProgressUpdate(int value) {

                                    }
                                }).execute(getActivity(), projectName);
                            }
                        });

                        builder.show();

                    }

                    @Override
                    public void onFailure(Exception error) {
                        if (getActivity() == null) {
                            return;
                        }
                        bt_load_suggestions.setText(error.getMessage());
                        bt_load_suggestions.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                null, getResources().getDrawable(R.drawable.ic_error), null, null
                        );
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                    }

                    @Override
                    public void onProgressUpdate(int value) {

                    }
                }).execute(getActivity());
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.new_favorite, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() != R.id.action_create_favorite) {
                return super.onOptionsItemSelected(item);
        }

        if (favoriteName.getText().toString().equals("")) {
            favoriteName.setError("A name must be provided");
            return false;
        }

        SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        //Base configuration already loaded?
        if (!settings.contains(Utils.BASE_DESCRIPTORS_ENTRY)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Application Profile not loaded")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setMessage(getResources().getString(R.string.no_profile))
                    .setIcon(R.drawable.ic_whats_hot)
                    .show();
            return false;
        }

        String itemName = favoriteName.getText().toString();

        final File favoriteFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + getResources().getString(R.string.app_name) + "/"
                + favoriteName.getText());

        if (!favoriteFolder.exists()) {
            Log.i("Make dir", "" + favoriteFolder.mkdir());
            Toast.makeText(getActivity(), getResources().getString(R.string.created_folder), Toast.LENGTH_SHORT).show();
            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(favoriteFolder)));
        }

        //Register favorite
        FavoriteItem newFavorite = new FavoriteItem(favoriteName.getText().toString());

        //Load default configuration
        ArrayList<Descriptor> baseCfg = FavoriteMgr.getBaseDescriptors(getActivity());
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
                desc.setValue(favoriteDescription.getText().toString());
                folderMetadata.add(desc);
            }
        }

        if (recommendations != null && recommendations.size() > 0) {
            newFavorite.setMetadataRecommendations(recommendations);
        }

        newFavorite.setMetadataItems(folderMetadata);
        FavoriteMgr.registerFavorite(getActivity(), newFavorite);

        if (mDialog != null) {
            mDialog.dismiss();
        }

        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        FavoriteDetailsFragment favoriteDetail = new FavoriteDetailsFragment();
        Bundle args = new Bundle();
        args.putString("favorite_name", newFavorite.getTitle());
        favoriteDetail.setArguments(args);
        transaction.replace(R.id.frame_container, favoriteDetail);
        //getFragmentManager().popBackStack();
        transaction.commit();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("recommendations", new Gson().toJson(recommendations));
    }
}