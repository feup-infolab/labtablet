package pt.up.fe.labtablet.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.SubmissionValidationActivity;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.async.AsyncUploader;
import pt.up.fe.labtablet.utils.Utils;


public class SubmissionStep4 extends Fragment {

    private Button btStartUpload;
    private ProgressBar pbStatus;
    private TextView tv_progress_status;

    private String favoriteName;
    private String projectName;
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
        } else {
            favoriteName = savedInstanceState.getString("favorite_name");
            projectName = savedInstanceState.getString("project_name");
            destUri = SubmissionValidationActivity.getDestUri();
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

                mUploadTask = new AsyncUploader(new AsyncTaskHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (getActivity() == null) {
                            return;
                        }
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("favoriteName", getArguments().getString("favorite_name"));
                        getActivity().setResult(Utils.SUBMISSION_VALIDATION, returnIntent);
                        getActivity().finish();
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.uploaded_successfully), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        if (getActivity() == null) {
                            return;
                        }
                        Log.e("Submission", "Failed " + error.getMessage());
                        tv_progress_status.setText(error.getMessage());
                        btStartUpload.setText(getResources().getString(R.string.retry));
                        btStartUpload.setEnabled(true);
                    }

                    @Override
                    public void onProgressUpdate(int value) {
                        if (getActivity() == null) {
                            return;
                        }


                        if (value > 100) {
                            tv_progress_status.setText(getResources().getString(R.string.uploading));
                            pbStatus.setProgress(value - 100);
                            return;
                        }
                        pbStatus.setProgress(value);
                        switch (value) {
                            case 10:
                                tv_progress_status.setText(getResources().getString(R.string.creating_package));
                                break;
                            case 20:
                                tv_progress_status.setText(getResources().getString(R.string.authenticating));
                                break;
                            case 21:
                                tv_progress_status.setText(getResources().getString(R.string.creating_package));
                                break;
                            case 25:
                                tv_progress_status.setText(getResources().getString(R.string.uploading));
                                break;
                            case 50:
                                tv_progress_status.setText(getResources().getString(R.string.creating_metadata_package));
                                break;
                            case 70:
                                tv_progress_status.setText(getResources().getString(R.string.sending_metadata));
                                break;
                            case 90:
                                tv_progress_status.setText(getResources().getString(R.string.deleting_temp_files));
                                break;
                            case 100:
                                tv_progress_status.setText(getResources().getString(R.string.finished));
                                break;
                            default:
                                break;
                        }
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
