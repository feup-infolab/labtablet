package pt.up.fe.alpha.labtablet.fragments;

import android.app.Fragment;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.activities.FavoriteDetailsActivity;
import pt.up.fe.alpha.labtablet.activities.ItemPreviewActivity;
import pt.up.fe.alpha.labtablet.activities.MainActivity;
import pt.up.fe.alpha.labtablet.activities.SubmissionValidationActivity;
import pt.up.fe.alpha.labtablet.async.AsyncCustomTaskHandler;
import pt.up.fe.alpha.labtablet.async.AsyncUploader;
import pt.up.fe.alpha.labtablet.database.AppDatabase;
import pt.up.fe.alpha.labtablet.models.Dendro.Sync;
import pt.up.fe.alpha.labtablet.models.ProgressUpdateItem;
import pt.up.fe.alpha.labtablet.utils.Utils;


public class SubmissionStep4 extends Fragment {

    private Button btStartUpload;
    private ProgressBar pbStatus;
    private TextView tv_progress_status;

    private String favoriteName;
    private String projectName;
    private String destInstanceAddress;
    private String destUri;

    private AsyncUploader mUploadTask;

    public SubmissionStep4() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SubmissionStep4 newInstance(String favoriteName, String projectName, String destUri) {
        SubmissionStep4 fragment = new SubmissionStep4();
        Bundle args = new Bundle();
        args.putString("favorite_name", favoriteName);
        args.putString("project_name", projectName);
        args.putString("dest_uri", destUri);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_submission_step4, container, false);
        btStartUpload = (Button) rootView.findViewById(R.id.submission_start_upload);
        pbStatus = (ProgressBar) rootView.findViewById(R.id.pb_submission_loading);
        tv_progress_status = (TextView) rootView.findViewById(R.id.submission_status_message);

        if (savedInstanceState == null) {
            favoriteName = getArguments().getString("favorite_name");
            projectName = getArguments().getString("project_name");
            destUri = SubmissionValidationActivity.getDestUri();
            destInstanceAddress = SubmissionValidationActivity.getDestUri();
        } else {
            favoriteName = savedInstanceState.getString("favorite_name");
            projectName = savedInstanceState.getString("project_name");
            destUri = SubmissionValidationActivity.getDestUri();
            destInstanceAddress = SubmissionValidationActivity.getDestUri();
        }

        btStartUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btStartUpload.setEnabled(false);
                destUri = SubmissionValidationActivity.getDestUri();
                if (destUri.equals("")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.dest_folder_undefined), Toast.LENGTH_LONG).show();
                    return;
                }

                pbStatus.setVisibility(View.VISIBLE);
                btStartUpload.setText(getString(R.string.cancel_process));
                pbStatus.setIndeterminate(false);
                pbStatus.setProgress(0);

                mUploadTask = new AsyncUploader(new AsyncCustomTaskHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (getActivity() == null) {
                            return;
                        }
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("favoriteName", getArguments().getString("favorite_name"));
                        getActivity().setResult(Utils.SUBMISSION_VALIDATION, returnIntent);
                        AppDatabase db = AppDatabase.getDatabase(getActivity().getApplicationContext());
                        Sync syncedFolder = new Sync(favoriteName, destInstanceAddress, destUri, new Date(), false);
                        Boolean resultOfInsert = syncedFolder.insertSync(db);
                        getActivity().finish();
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.uploaded_successfully), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        getActivity().startActivity(intent);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        if (getActivity() == null) {
                            return;
                        }
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Submission", "Failed " + error.getMessage());
                        //OLD CODE
                        /*tv_progress_status.setText(error.getMessage());
                        btStartUpload.setText(getResources().getString(R.string.retry));
                        btStartUpload.setEnabled(true);
                        */
                        //NEW CODE
                        tv_progress_status.setText(error.getMessage());
                        btStartUpload.setText(getResources().getString(R.string.retry));
                        btStartUpload.setEnabled(true);
                        getActivity().finish();
                    }

                    @Override
                    public void onProgressUpdate(ProgressUpdateItem value) {
                        if (getActivity() == null) {
                            return;
                        }
                        FloatingActionButton fab = new FloatingActionButton(getActivity());
                        fab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ab_pulse));
                        fab.show();


                        tv_progress_status.setText(value.getMessage());
                        pbStatus.setProgress(value.getProgress());

                    }

                });
                projectName = SubmissionValidationActivity.getProjectName();
                destUri = SubmissionValidationActivity.getDestUri();
                mUploadTask.execute(favoriteName, projectName, destUri, getActivity());
            }

        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("favorite_name", getArguments().getString("favorite_name"));
        savedInstanceState.putString("project_name", getArguments().getString("project_name"));
        savedInstanceState.putString("dest_uri", getArguments().getString("dest_uri"));
    }
}
