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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.async.AsyncProjectListFetcher;
import pt.up.fe.labtablet.async.AsyncRecommendationsLoader;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Dendro.Project;
import pt.up.fe.labtablet.models.Dendro.ProjectListResponse;
import pt.up.fe.labtablet.models.Descriptor;
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
    private SharedPreferences settings;
    private String projectName;
    private ArrayList<Project> availableProjects;
    private ArrayList<Descriptor> recommendations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_new_dataset, container, false);

        favoriteName = (EditText) rootView.findViewById(R.id.et_dataset_name);
        favoriteDescription = (EditText) rootView.findViewById(R.id.et_dataset_description);
        Button bt_submit = (Button) rootView.findViewById(R.id.bt_submit);
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

        favoriteName.requestFocus();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("recommendations")) {
                recommendations = new Gson().fromJson(
                        savedInstanceState.get("recommendations").toString(),
                        Utils.ARRAY_DESCRIPTORS
                );
                bt_load_suggestions.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null, getResources().getDrawable(R.drawable.ic_check), null, null
                );

                bt_load_suggestions.setText(getResources().getString(R.string.successul_imported_recommendations));
            }
        }

        //create preferences entry for that favorite
        settings = getActivity().getSharedPreferences(
                getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

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
                                                null, getResources().getDrawable(R.drawable.ic_check), null, null
                                        );
                                        bt_load_suggestions.setText(getResources().getString(R.string.successul_imported_recommendations));
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

        bt_submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favoriteName.getText().toString().equals("")) {
                    favoriteName.setError("A name must be provided");
                    return;
                }

                //Base configuration already loaded?
                if (!settings.contains(Utils.DESCRIPTORS_CONFIG_ENTRY)) {
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
                    return;
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

                editor = settings.edit();
                if (settings.contains(itemName)) {
                    editor.remove(itemName);
                    Toast.makeText(getActivity(), "Will overwrite...", Toast.LENGTH_LONG).show();
                }

                //Load default configuration
                ArrayList<Descriptor> baseCfg = new Gson().fromJson(settings.getString(Utils.DESCRIPTORS_CONFIG_ENTRY, ""),
                        Utils.ARRAY_DESCRIPTORS);
                ArrayList<Descriptor> folderMetadata = new ArrayList<Descriptor>();
                ArrayList<ChangelogItem> logs = new ArrayList<ChangelogItem>();
                ChangelogItem log;
                String descName;
                for (Descriptor desc : baseCfg) {
                    descName = desc.getName().toLowerCase();
                    log = new ChangelogItem();
                    if (descName.contains("title")) {
                        log.setMessage(ChangelogManager.addedLog(descName, itemName));
                        log.setDate(Utils.getDate());
                        log.setTitle(getResources().getString(R.string.log_added));
                        logs.add(log);
                        desc.setValue(itemName);
                        desc.validate();
                        folderMetadata.add(desc);
                    }
                    if (descName.contains("description")) {
                        log.setMessage(ChangelogManager.addedLog(descName, itemName));
                        log.setDate(Utils.getDate());
                        log.setTitle(getResources().getString(R.string.log_added));
                        logs.add(log);
                        desc.validate();
                        desc.setValue(favoriteDescription.getText().toString());
                        folderMetadata.add(desc);
                    }
                    if (descName.contains("date")) {
                        log.setMessage(ChangelogManager.addedLog(descName, Utils.getDate()));
                        log.setDate(Utils.getDate());
                        log.setTitle(getResources().getString(R.string.log_added));
                        logs.add(log);
                        desc.validate();
                        desc.setValue(Utils.getDate());
                        folderMetadata.add(desc);
                    }

                }
                editor.putString(itemName, new Gson().toJson(folderMetadata));
                if (recommendations != null && recommendations.size() > 0) {
                    editor.putString(itemName + "_dendro", new Gson().toJson(recommendations));
                    log = new ChangelogItem();
                    log.setMessage(getResources().getString(R.string.log_loaded) + ": " + projectName);
                    log.setTitle(getResources().getString(R.string.log_loaded));
                    logs.add(log);
                }

                editor.apply();

                ChangelogManager.addItems(logs, getActivity());
                if (mDialog != null) {
                    mDialog.dismiss();
                }

                FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                FavoriteDetailsFragment favoriteDetail = new FavoriteDetailsFragment();
                Bundle args = new Bundle();
                args.putString("favorite_name", favoriteName.getText().toString());
                favoriteDetail.setArguments(args);
                transaction.replace(R.id.frame_container, favoriteDetail);
                transaction.addToBackStack(null);
                getFragmentManager().popBackStack();
                transaction.commit();
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("recommendations", new Gson().toJson(recommendations));
    }
}