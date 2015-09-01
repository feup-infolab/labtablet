package pt.up.fe.labtablet.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.HomeTipsAdapter;
import pt.up.fe.labtablet.models.HomeTip;

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
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator +getResources().getString(R.string.app_name));

        Typeface fancyFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/LobsterTwo-Regular.ttf");
        ((TextView)(rootView.findViewById(R.id.tv_home))).setTypeface(fancyFont);



/*
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

*/
        rootView.findViewById(R.id.home_launch_configurations).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                transaction.replace(R.id.frame_container, new ConfigurationFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        rootView.findViewById(R.id.home_create_project).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 FragmentTransaction transaction = getFragmentManager().beginTransaction();
                 transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                 transaction.replace(R.id.frame_container, new NewFavoriteBaseFragment());
                 transaction.addToBackStack("nopes");
                 transaction.commit();
             }
         });

        rootView.findViewById(R.id.home_launch_projects).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 FragmentTransaction transaction = getFragmentManager().beginTransaction();
                 transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                 transaction.replace(R.id.frame_container, new ListFavoritesFragment());
                 transaction.addToBackStack("nopes");
                 transaction.commit();
             }
         });


        final com.github.clans.fab.FloatingActionButton actionButton = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.home_action_button);
        actionButton.show(true);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "2015 Infolab", Toast.LENGTH_SHORT).show();
            }
        });

        ArrayList<HomeTip> items = new ArrayList<>();

        HomeTip tip = new HomeTip();
        tip.setTitle("Describing data");
        tip.setBody("When exploring research data, having access to information regarding their production circumstances is of utmost importance." +
                " While basic datasets are easily understood without such information, the same doesn't happen if we consider modern research workflows." +
                " To overcome this barrier, it is advisable to provide information that can help third parties fully understand how these datasets were produced," +
                " which research institutions were involved along with other relevant aspects. \n\n With an improved data description, we can achieve higher reuse rates," +
                "higher chances of data being preserved on the long run, and create mechanisms that help validating published research.");
        tip.setResourceID("describing_data");
        items.add(tip);

        tip = new HomeTip();
        tip.setTitle("LabTablet");
        tip.setBody("In some cases, researchers are already describing their data in a systematic approach, even without noticing it. They often resort to paper-based" +
                "laboratory notebooks to do so which, as you can understand, can be a frail support for such an important asset. LabTablet is designed to quickly gather " +
                "important insights about research data. It is similar to an electronic laboratory notebook, offering diverse digital support for all kinds of descriptions." +
                " Text-based notes can be a good starting point, but LabTablet can use the integrated sensors to automatically record the available information.");
        tip.setResourceID("labtablet");
        items.add(tip);

        tip = new HomeTip();
        tip.setTitle("Taking advantage of built-in sensors");
        tip.setBody("<p>LabTablet can read the device's built-in sensors, provided you find them useful." +
                "The available sources are entirely dependant on the platform, but it is quite common to find devices with:</p>\n" +
                "  &#8226; GPS<br/>\n" +
                "  &#8226; Temperature<br/>\n" +
                "  &#8226; Luminosity<br/>\n" +
                "  &#8226; Magnetic field<br/>\n" +
                "  &#8226; Other proprietary sensors<br/>" +
                " <p>For quick memos, for instance, you can make short voice recordings that you can later " +
                "translate into other formats. This feature can also be used if you look forward to record an interview or any other event that you feel should be kept." +
                "With this in mind, LabTablet lets you record from these sources:</p>" +
                "   &#8226; Voice<br/>" +
                "   &#8226; Video<br/>" +
                "   &#8226; Depictions or photos<br/>" +
                "   &#8226; Sketches<br/>" +
                "<p>As soon as you finish the field session, the correspondent files are stored locally in appropriate formats.</p>");
        tip.setResourceID("sensors");
        items.add(tip);

        tip = new HomeTip();
        tip.setTitle("Tracking");
        tip.setBody("In some research fields, such as the ecological domain, researchers may want to represent their route or geographical coverage during an species sightseeing. For this purpose, you can turn on the" +
                " GPS tracking, that will record your position throughout the session. This is later exported to a file that can be easily viewed in known platforms such as Google or Bing Maps.");
        tip.setResourceID("maps");
        items.add(tip);

        HomeTipsAdapter adapter = new HomeTipsAdapter(getActivity(), items);

        RecyclerView itemList = (RecyclerView) rootView.findViewById(R.id.lv_home_tips);

        itemList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        itemList.setItemAnimator(new DefaultItemAnimator());
        itemList.setAdapter(adapter);
        itemList.animate();

        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            boolean hideToolBar = false;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayout header = (LinearLayout)(rootView.findViewById(R.id.home_header));
                if (hideToolBar) {
                    header.animate().translationY(-header.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                    actionButton.hide(true);
                    header.setVisibility(View.GONE);
                } else {
                    header.setVisibility(View.VISIBLE);
                    actionButton.show(true);
                    header.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 20) {
                    hideToolBar = true;

                } else if (dy < -5) {
                    hideToolBar = false;
                }
            }
        };

        itemList.setOnScrollListener(onScrollListener);
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