package pt.up.fe.labtablet.fragments;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.SubmissionValidationActivity;
import pt.up.fe.labtablet.api.AsyncDendroDirectoryFetcher;
import pt.up.fe.labtablet.api.AsyncProjectListFetcher;
import pt.up.fe.labtablet.api.AsyncTaskHandler;
import pt.up.fe.labtablet.api.SubmissionStepHandler;
import pt.up.fe.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.labtablet.models.Dendro.DendroFolderItem;
import pt.up.fe.labtablet.models.Dendro.Project;
import pt.up.fe.labtablet.models.Dendro.ProjectListResponse;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;


public class SubmissionStep3 extends Fragment {

    private ListView dendroDirList;
    private DendroFolderAdapter mAdapter;

    private ProgressBar progressBar;
    private Button selectFolder;
    private String path;
    private static String projectName;
    private TextView tv_instructions;
    private TextView tv_empty;

    static SubmissionStepHandler mHandler;

    private AsyncProjectListFetcher mProjectFetcher;
    private AsyncDendroDirectoryFetcher mDirectoryFetcher;
    private ArrayList<DendroFolderItem> folders;
    private ArrayList<Project> availableProjects;

    MenuItem actionRefresh;
    MenuItem actionUp;




    public static SubmissionStep3 newInstance(String projectName, SubmissionStepHandler handler) {
        SubmissionStep3 fragment = new SubmissionStep3();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        mHandler = handler;
        return fragment;
    }

    public SubmissionStep3() {
        this.path = "/data";
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
        progressBar = (ProgressBar) rootView.findViewById(R.id.dendro_folders_progress);
        tv_instructions = (TextView) rootView.findViewById(R.id.step3_instructions);
        tv_empty = (TextView) rootView.findViewById(R.id.step3_empty);
        progressBar.setVisibility(View.GONE);
        tv_instructions.setVisibility(View.VISIBLE);
        tv_empty.setVisibility(View.GONE);

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
                if(item.getDdr().getFileExtension().equals(Utils.DENDRO_FOLDER_EXTENSION)) {
                    path += "/" + item.getNie().getTitle();
                    //items.get((Integer) view.getTag()).getNie().getTitle();
                    selectFolder.setEnabled(true);
                    refreshFoldersList();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.this_is_a_file), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Step3", "Received intent, but didn't launch anything. Request: " + requestCode + " Result: " + resultCode);
    }

    public void refreshFoldersList() {
        progressBar.setVisibility(View.VISIBLE);
        initDirectoryFetcher();
        mDirectoryFetcher.execute(projectName + path, getActivity());

    }


    public class DendroFolderAdapter extends ArrayAdapter<DendroFolderItem> {
        private final Activity context;
        private final List<DendroFolderItem> items;

        class ViewHolder {
            public TextView mFolderName;
            public TextView mFolderDate;
            public TextView mFolderUri;
            public ImageView mFolderType;
        }

        public DendroFolderAdapter(Activity context, List<DendroFolderItem> srcItems) {
            super(context, R.layout.item_dendro_folder, srcItems);
            this.context = context;
            this.items = srcItems;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.item_dendro_folder, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.mFolderName = (TextView) rowView.findViewById(R.id.folder_item_title);
                viewHolder.mFolderDate = (TextView) rowView.findViewById(R.id.folder_item_date);
                viewHolder.mFolderUri = (TextView) rowView.findViewById(R.id.folder_item_size);
                viewHolder.mFolderType = (ImageView) rowView.findViewById(R.id.folder_item_type);
                rowView.setTag(viewHolder);
            }

            // fill data
            ViewHolder holder = (ViewHolder) rowView.getTag();
            final DendroFolderItem item = items.get(position);
            holder.mFolderName.setText(item.getNie().getTitle());
            holder.mFolderDate.setText(item.getDcterms().getModified());
            holder.mFolderUri.setText(item.getUri());

            if (item.getDdr().getFileExtension().equals("folder")) {
                holder.mFolderType.setImageDrawable(getResources().getDrawable(R.drawable.ic_folder));
            } else {
                holder.mFolderType.setImageDrawable(getResources().getDrawable(R.drawable.ic_file));
            }
            return rowView;
        }
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
            initDialog();
            mProjectFetcher.execute(getActivity());
        } else if (item.getItemId() == R.id.action_dendro_refresh) {
            if(!path.equals("/data")) {
                initDirectoryFetcher();
                mDirectoryFetcher.execute(projectName + path, getActivity());
            }
        } else if (item.getItemId() == R.id.action_dendro_go_up) {
            if(path.equals("/data")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.already_in_root_folder), Toast.LENGTH_LONG).show();
            } else {
                //remove a level from the path
                path = path.substring(0, path.lastIndexOf('/'));
                refreshFoldersList();
            }

        }
        return super.onOptionsItemSelected(item);

    }


    public void initDirectoryFetcher() {
        mDirectoryFetcher = new AsyncDendroDirectoryFetcher(new AsyncTaskHandler<ArrayList<DendroFolderItem>>() {
            @Override
            public void onSuccess(ArrayList<DendroFolderItem> result) {
                if (getActivity() == null) {
                    return;
                }
                folders = result;
                mAdapter = new DendroFolderAdapter(getActivity(), folders);
                if(folders.size() == 0) {
                    tv_empty.setVisibility(View.VISIBLE);
                } else {
                    tv_empty.setVisibility(View.GONE);
                }
                dendroDirList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                actionUp.setVisible(true);
                actionRefresh.setVisible(true);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Exception error) {
                if (getActivity() == null) {
                    return;
                }
                if(getActivity() == null) {
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

    public void initDialog() {
        progressBar.setVisibility(View.VISIBLE);
        mProjectFetcher = new AsyncProjectListFetcher(new AsyncTaskHandler<ProjectListResponse>() {
            @Override
            public void onSuccess(ProjectListResponse result) {
                if (getActivity() == null) {
                    return;
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
                        SubmissionValidationActivity.setProjectName(projectName);
                        refreshFoldersList();
                    }
                });

                tv_instructions.setVisibility(View.GONE);
                tv_empty.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                builder.show();

            }

            @Override
            public void onFailure(Exception error) {
                if (getActivity() == null) {
                    return;
                }
                tv_instructions.setVisibility(View.GONE);
                tv_empty.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onProgressUpdate(int value) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
