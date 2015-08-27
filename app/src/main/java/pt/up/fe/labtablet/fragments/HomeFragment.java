package pt.up.fe.labtablet.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.FormQuestionCreatorActivity;
import pt.up.fe.labtablet.async.AsyncGenericChecker;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.utils.Utils;

public class HomeFragment extends Fragment {

    private TextView tvMetadataQuality;
    private ImageView ivMetadataQuality;
    private ProgressBar pbMetadataLoading;
    private RelativeLayout rlMetadataQuality;

    private boolean projects;

    public HomeFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        Button btConfigurations = (Button) rootView.findViewById(R.id.home_bt_configurations);
        Button btNewProject = (Button) rootView.findViewById(R.id.home_bt_create_project);
        Button btMyProjects = (Button) rootView.findViewById(R.id.bt_home_list_projects);
        TextView tvProjectCount = (TextView) rootView.findViewById(R.id.tv_project_state);
        TextView tvProfileState = (TextView) rootView.findViewById(R.id.tv_profile_state);
        tvMetadataQuality = (TextView) rootView.findViewById(R.id.tv_metadata_state);
        ivMetadataQuality = (ImageView) rootView.findViewById(R.id.iv_metadata_state);
        pbMetadataLoading = (ProgressBar) rootView.findViewById(R.id.pb_generic_progress);
        rlMetadataQuality = (RelativeLayout) rootView.findViewById(R.id.rl_metadata_quality);

        final Drawable yes = getResources().getDrawable(R.drawable.ic_check);
        final Drawable no = getResources().getDrawable(R.drawable.ic_error);
        final Drawable meh = getResources().getDrawable(R.drawable.ic_warning);

        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator +getResources().getString(R.string.app_name));

        if( file.exists() ) {
            if(file.listFiles().length > 0) {
                tvProjectCount.setText(String.format(getResources().getString(R.string.you_have_x_projects), file.listFiles().length));
                tvProjectCount.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        yes, null, null);
                projects = true;
            } else {
                tvProjectCount.setText(getResources().getString(R.string.no_projects));
                tvProjectCount.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        no, null, null);
                projects = false;
            }
        }

        Typeface fancyFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/LobsterTwo-Regular.ttf");
        ((TextView)(rootView.findViewById(R.id.tv_home))).setTypeface(fancyFont);


        SharedPreferences settings = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        if (settings.contains(Utils.BASE_DESCRIPTORS_ENTRY)) {
            tvProfileState.setText(getResources().getString(R.string.profile_already_loaded));
            tvProfileState.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                    yes, null, null);
        } else {
            tvProfileState.setText(getResources().getString(R.string.no_profile));
            tvProfileState.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                    no, null, null);
            btConfigurations.setText(getResources().getString(R.string.open_configurations));
        }

        if (!projects) {
            rlMetadataQuality.setVisibility(View.GONE);
        } else {
            new AsyncGenericChecker(new AsyncTaskHandler<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    if (getActivity() == null) {
                        return;
                    }
                    rlMetadataQuality.setVisibility(View.VISIBLE);
                    tvMetadataQuality.setVisibility(View.VISIBLE);
                    ivMetadataQuality.setVisibility(View.VISIBLE);
                    pbMetadataLoading.setVisibility(View.GONE);
                    if(result > 0) {
                        tvMetadataQuality.setText(String.format(getResources().getString(R.string.metadata_meh), result));
                        ivMetadataQuality.setImageDrawable(meh);
                    } else {
                        tvMetadataQuality.setText(getResources().getString(R.string.metadata_good));
                        ivMetadataQuality.setImageDrawable(yes);
                    }
                }

                @Override
                public void onFailure(Exception error) {
                    if (getActivity() == null) {
                        return;
                    }
                    pbMetadataLoading.setVisibility(View.GONE);
                    Log.e("Generic", error.getMessage());
                }

                @Override
                public void onProgressUpdate(int value) {

                }
            }).execute(getActivity(), "");
        }

        btConfigurations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                //transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                transaction.replace(R.id.frame_container, new ConfigurationFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        btConfigurations.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                saveSharedPreferences();
                Toast.makeText(getActivity(), "Exported to device root", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        btNewProject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });

        btNewProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                //transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                transaction.replace(R.id.frame_container, new NewFavoriteBaseFragment());
                transaction.addToBackStack("nopes");
                transaction.commit();
            }
        });

        btMyProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                //transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                transaction.replace(R.id.frame_container, new ListFavoritesFragment());
                transaction.addToBackStack("nopes");
                transaction.commit();
            }
        });

        com.github.clans.fab.FloatingActionButton actionButton = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_action_button);
        actionButton.show(true);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "2015 Infolab", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void saveSharedPreferences()
    {
        // create some junk data to populate the shared preferences
        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        // BEGIN EXAMPLE

        File myPath = new File(Environment.getExternalStorageDirectory().toString());
        File myFile = new File(myPath, "MySharedPreferences");

        try {
            FileWriter fw = new FileWriter(myFile);
            PrintWriter pw = new PrintWriter(fw);

            Map<String,?> prefsMap = prefs.getAll();

            for(Map.Entry<String,?> entry : prefsMap.entrySet()) {
                pw.println(entry.getKey() + ": " + entry.getValue().toString());
            }

            pw.close();
            fw.close();

        } catch (Exception e) {
            Log.e(getClass().getName(), e.toString());
        }
    }
}