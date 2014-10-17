package pt.up.fe.labtablet.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.SubmissionValidationActivity;
import pt.up.fe.labtablet.adapters.DendroFolderAdapter;
import pt.up.fe.labtablet.api.SubmissionStepHandler;
import pt.up.fe.labtablet.async.AsyncDendroDirectoryFetcher;
import pt.up.fe.labtablet.async.AsyncProjectListFetcher;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.labtablet.models.Dendro.DendroFolderItem;
import pt.up.fe.labtablet.models.Dendro.Project;
import pt.up.fe.labtablet.models.Dendro.ProjectListResponse;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;


public class SubmissionStep3 extends Fragment {

    private static SubmissionStepHandler mHandler;
    private static String projectName;
    private MenuItem actionRefresh;
    private MenuItem actionUp;
    private ListView dendroDirList;
    private DendroFolderAdapter mAdapter;
    private TextView tv_empty;
    private Button selectFolder;
    private Button btInstructions;

    private AsyncProjectListFetcher mProjectFetcher;
    private AsyncDendroDirectoryFetcher mDirectoryFetcher;
    private ArrayList<DendroFolderItem> folders;
    private ArrayList<Project> availableProjects;

    private String path;

    public SubmissionStep3() {
        this.path = "/data";
    }

    public static SubmissionStep3 newInstance(SubmissionStepHandler handler) {
        SubmissionStep3 fragment = new SubmissionStep3();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        mHandler = handler;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_submission_step3, container, false);

        if (savedInstanceState == null) {
            projectName = getArguments().getString("project_name");
        } else {
            projectName = savedInstanceState.getString("project_name");
            path = savedInstanceState.getString("path");
        }
        setHasOptionsMenu(true);

        dendroDirList = (ListView) rootView.findViewById(R.id.dendro_folders_list);
        selectFolder = (Button) rootView.findViewById(R.id.dendro_folders_select);
        btInstructions = (Button) rootView.findViewById(R.id.step3_instructions);
        tv_empty = (TextView) rootView.findViewById(R.id.step3_empty);
        btInstructions.setVisibility(View.VISIBLE);
        tv_empty.setVisibility(View.GONE);

        btInstructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchProjects();
            }
        });

        selectFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if the level is root, then the button should be disabled
                if (path.equals("/data")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.root_impossible_to_select), Toast.LENGTH_LONG).show();
                    return;
                }

                DendroConfiguration conf = FileMgr.getDendroConf(getActivity());
                String selection = conf.getAddress() + "/project/" + projectName + path;
                Toast.makeText(getActivity(), getResources().getString(R.string.selected_folder), Toast.LENGTH_SHORT).show();
                SubmissionValidationActivity.setDestUri(selection);
                mHandler.nextStep(3);
            }
        });

        dendroDirList.setDividerHeight(0);
        dendroDirList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                DendroFolderItem item = folders.get(position);
                if (item.getDdr().getFileExtension().equals(Utils.DENDRO_FOLDER_EXTENSION)) {
                    path += "/" + item.getNie().getTitle();

                    //items.get((Integer) view.getTag()).getNie().getTitle();
                    selectFolder.setVisibility(View.VISIBLE);
                    selectFolder.setEnabled(true);
                    refreshFoldersList();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.this_is_a_file), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void onSearchProjects() {
        initDialog();
        mProjectFetcher.execute(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Step3", "Received intent, but didn't launch anything. Request: " + requestCode + " Result: " + resultCode);
    }

    private void refreshFoldersList() {
        initDirectoryFetcher();
        mDirectoryFetcher.execute(projectName + path, getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("favorite_name", getArguments().getString("favorite_name"));
        savedInstanceState.putString("project_name", projectName);
        savedInstanceState.putString("path", path);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.submission_step3, menu);
        actionRefresh = menu.findItem(R.id.action_dendro_refresh);
        actionUp = menu.findItem(R.id.action_dendro_go_up);
        actionRefresh.setVisible(false);
        actionUp.setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_dendro_search) {
            onSearchProjects();
        } else if (item.getItemId() == R.id.action_dendro_refresh) {
            if (!path.equals("/data")) {
                initDirectoryFetcher();
                mDirectoryFetcher.execute(projectName + path, getActivity());
            }
        } else if (item.getItemId() == R.id.action_dendro_go_up) {
            if (path.equals("/data")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.already_in_root_folder), Toast.LENGTH_LONG).show();
            } else {
                //remove a level from the path
                path = path.substring(0, path.lastIndexOf('/'));
                refreshFoldersList();
            }

        }
        return super.onOptionsItemSelected(item);

    }

    private void initDirectoryFetcher() {
        mDirectoryFetcher = new AsyncDendroDirectoryFetcher(new AsyncTaskHandler<ArrayList<DendroFolderItem>>() {
            @Override
            public void onSuccess(ArrayList<DendroFolderItem> result) {
                if (getActivity() == null) {
                    return;
                }
                folders = result;
                mAdapter = new DendroFolderAdapter(getActivity(), folders);
                if (folders.size() == 0) {
                    tv_empty.setVisibility(View.VISIBLE);
                } else {
                    tv_empty.setVisibility(View.GONE);
                }
                dendroDirList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                actionUp.setVisible(true);
                actionRefresh.setVisible(true);
                dendroDirList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception error) {
                if (getActivity() == null) {
                    return;
                }

                Toast.makeText(getActivity(), "Unable to load folder", Toast.LENGTH_SHORT).show();
                path = "/data";
                //refreshFoldersList();
                Log.e("refreshFolderList", error.getMessage());
            }

            @Override
            public void onProgressUpdate(int value) {
            }
        });
    }

    private void initDialog() {
        btInstructions.setText("Loading. Please stand by...");
        btInstructions.setEnabled(false);
        btInstructions.setVisibility(View.VISIBLE);
        btInstructions.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_wait, 0, 0);

        mProjectFetcher = new AsyncProjectListFetcher(new AsyncTaskHandler<ProjectListResponse>() {
            @Override
            public void onSuccess(ProjectListResponse result) {
                if (getActivity() == null) {
                    return;
                }
                availableProjects = result.getProjects();
                (getActivity().findViewById(R.id.dendro_folders_buttons)).setVisibility(View.VISIBLE);
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
                        SubmissionValidationActivity.setProjectName(projectName);
                        refreshFoldersList();
                    }
                });

                btInstructions.setEnabled(true);
                btInstructions.setVisibility(View.GONE);
                tv_empty.setVisibility(View.GONE);
                builder.show();

            }

            @Override
            public void onFailure(Exception error) {
                if (getActivity() == null) {
                    return;
                }

                btInstructions.setText(getString(R.string.unable_load_projects));
                btInstructions.setEnabled(true);
                btInstructions.setVisibility(View.VISIBLE);
                btInstructions.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ab_cross, 0, 0);
                tv_empty.setVisibility(View.GONE);
            }

            @Override
            public void onProgressUpdate(int value) {

            }
        });
    }
}
