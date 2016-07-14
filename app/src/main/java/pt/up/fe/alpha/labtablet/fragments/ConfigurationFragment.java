package pt.up.fe.alpha.labtablet.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.activities.DescriptorPickerActivity;
import pt.up.fe.alpha.labtablet.api.ChangelogManager;
import pt.up.fe.alpha.labtablet.application.LabTablet;
import pt.up.fe.alpha.labtablet.async.AsyncAuthenticator;
import pt.up.fe.alpha.labtablet.async.AsyncProfileLoader;
import pt.up.fe.alpha.labtablet.async.AsyncTaskHandler;
import pt.up.fe.alpha.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.alpha.labtablet.models.AssociationItem;
import pt.up.fe.alpha.labtablet.models.ChangelogItem;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.alpha.labtablet.models.Descriptor;
import pt.up.fe.alpha.labtablet.models.Dictionary;
import pt.up.fe.alpha.labtablet.models.SeaBioData.Data;
import pt.up.fe.alpha.labtablet.models.SeaBioData.EntityResponse;
import pt.up.fe.alpha.labtablet.utils.Utils;

public class ConfigurationFragment extends Fragment implements AsyncTaskHandler<ArrayList<Descriptor>>, View.OnClickListener {

    private ProgressDialog progress;
    private TextView tv_kml_descriptor;
    private TextView tv_kml_descriptor_description;
    private TextView tv_jpg_descriptor;
    private TextView tv_jpg_descriptor_description;
    private TextView tv_mp3_descriptor;
    private TextView tv_mp3_descriptor_description;
    private TextView tv_sbd_active_campaign;
    private TextView tv_sbd_users;
    private TextView tv_sbd_procedures;

    private EditText et_conf_username;
    private EditText et_conf_password;
    private EditText et_conf_address;

    private Button bt_save_dendro_confs;
    private Button bt_sbd_username;
    private Button btPickCampaign;
    private Button bt_file;
    private Button btLoadUsers;
    private Button btLoadProcedures;

    private SharedPreferences settings;
    private ArrayList<AssociationItem> mItems;
    private String SBDToken;

    private View mRootView;

    public ConfigurationFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_configuration, container, false);

        progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading");
        progress.setMessage("Please stand by, while the descriptors are loaded...");
        progress.show();


        try {
            settings = getActivity().getSharedPreferences(getResources()
                    .getString(R.string.app_name),Context.MODE_PRIVATE);
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "Error loading preferences", Toast.LENGTH_LONG).show();
            Log.e("PREF", e.getMessage());
            return rootView;
        }


        mRootView = rootView;

        //configure views and buttons
        setupLayout();
        loadAssociations();


        bt_file = (Button) rootView.findViewById(R.id.bt_file_path);
        bt_file.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, Utils.PROFILE_PICK);
            }
        });

        bt_file.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                try {
                    InputStream stream = getActivity().getAssets().open("base_profile.json");
                    new AsyncProfileLoader(ConfigurationFragment.this).execute(stream);
                } catch (IOException e) {
                    Log.e("IO", "READING ASSETS " +  e.toString());
                }
                return true;
            }
        });

        progress.dismiss();
        return rootView;
    }

    /**
     * Sets up the layout assigning buttons and other elements to their IDs
     */
    private void setupLayout() {
        Button bt_gps_edit = (Button) mRootView.findViewById(R.id.bt_kml_edit);
        Button bt_jpg_edit = (Button) mRootView.findViewById(R.id.bt_jpg_edit);
        Button bt_mp3_edit = (Button) mRootView.findViewById(R.id.bt_mp3_edit);
        bt_sbd_username = (Button) mRootView.findViewById(R.id.bt_sbd_authenticate_user);

        bt_file = (Button) mRootView.findViewById(R.id.bt_file_path);
        bt_save_dendro_confs = (Button) mRootView.findViewById(R.id.dendro_configurations_save);
        btPickCampaign = (Button) mRootView.findViewById(R.id.bt_sbd_pick_campaign);
        btLoadUsers = (Button) mRootView.findViewById(R.id.bt_sbd_get_users);
        btLoadProcedures = (Button) mRootView.findViewById(R.id.bt_sbd_get_procedures);

        et_conf_username = (EditText) mRootView.findViewById(R.id.dendro_configurations_username);
        et_conf_address = (EditText) mRootView.findViewById(R.id.dendro_configurations_address);
        et_conf_password = (EditText) mRootView.findViewById(R.id.dendro_configurations_password);

        tv_sbd_active_campaign = (TextView) mRootView.findViewById(R.id.sbd_active_campaign);
        tv_jpg_descriptor = (TextView) mRootView.findViewById(R.id.jpg_extension_descriptor);
        tv_jpg_descriptor_description = (TextView) mRootView.findViewById(R.id.jpg_extension_description);
        tv_sbd_procedures = (TextView) mRootView.findViewById(R.id.tv_sbd_procedures);

        tv_kml_descriptor = (TextView) mRootView.findViewById(R.id.kml_extension_descriptor);
        tv_kml_descriptor_description = (TextView) mRootView.findViewById(R.id.kml_extension_description);

        tv_mp3_descriptor = (TextView) mRootView.findViewById(R.id.mp3_extension_descriptor);
        tv_mp3_descriptor_description = (TextView) mRootView.findViewById(R.id.mp3_extension_description);
        tv_sbd_users = (TextView) mRootView.findViewById(R.id.tv_sbd_users);

        checkAvailableSettings();

        mRootView.findViewById(R.id.bt_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://docs.google.com/forms/d/1JSUS-yidh6SQUyV4SmubDdPr9WIqay0PCbWDutpFkIc/viewform";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        View.OnClickListener mClickListener = new View.OnClickListener() {

            String extension;

            @Override
            public void onClick(View view) {
                if(!settings.contains(Utils.BASE_DESCRIPTORS_ENTRY)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Application Profile not loaded")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .setMessage("Please select an application profile before proceeding.")
                            .setIcon(R.drawable.ic_warning)
                            .show();
                    return;
                }

                int id = view.getId();
                switch (id) {
                    case R.id.bt_kml_edit:
                        extension = Utils.GEO_TAGS;
                        break;
                    case R.id.bt_jpg_edit:
                        extension = Utils.PICTURE_TAGS;
                        break;
                    case R.id.bt_mp3_edit:
                        extension = Utils.AUDIO_TAGS;
                        break;
                }

                //Launch activity to select the descriptor
                Intent myIntent = new Intent(getActivity(), DescriptorPickerActivity.class);
                myIntent.putExtra("file_extension", extension);
                myIntent.putExtra("favoriteName", "");
                myIntent.putExtra("returnMode", Utils.DESCRIPTOR_ASSOCIATE);
                startActivityForResult(myIntent, Utils.DESCRIPTOR_ASSOCIATE);
            }
        };

        bt_mp3_edit.setOnClickListener(mClickListener);
        bt_gps_edit.setOnClickListener(mClickListener);
        bt_jpg_edit.setOnClickListener(mClickListener);

        bt_save_dendro_confs.setOnClickListener(this);
        bt_sbd_username.setOnClickListener(this);
        btPickCampaign.setOnClickListener(this);
        btLoadUsers.setOnClickListener(this);
        btLoadProcedures.setOnClickListener(this);
    }

    /**
     * Checks the shared preferences for existing settings and updates the layout accordingly
     */
    private void checkAvailableSettings() {
        if (settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
            DendroConfiguration conf = new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);

            et_conf_address.setText(conf.getAddress());
            et_conf_password.setText(conf.getPassword());
            et_conf_username.setText(conf.getUsername());

            if(conf.isValidated()) {
                bt_save_dendro_confs.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null, getResources().getDrawable(R.drawable.ic_check), null, null);
            }
        }

        //check for saved settings
        if (settings.contains(Utils.TAG_SBD_USERNAME)) {
            bt_sbd_username.setText(getString(R.string.update));
        }

        if (settings.contains(Utils.SBD_USERS)) {
            btLoadUsers.setText(getString(R.string.update));
            ArrayList<Data> items = new Gson().fromJson(settings.getString(Utils.SBD_USERS, ""), Utils.ARRAY_SBD_DATA);
            String users = "";
            for (Data item : items)
                users += item.getName() + "\n";

            tv_sbd_users.setText(users.substring(0, users.length()-1));
        }

        if (settings.contains(Utils.SBD_ACTIVE_CAMPAIGN)) {
            Data campaign = new Gson().fromJson(settings.getString(Utils.SBD_ACTIVE_CAMPAIGN,""), Data.class);
            tv_sbd_active_campaign.setText(campaign.getName());
            tv_sbd_active_campaign.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows a dialog to pick a campaign and sets it as the active campaign for the application
     * @param response the response from the campaigns listing
     */
    private void showCampaignPickDialog(String response) {
        //Show dialog to pick campaign
        EntityResponse responseObj = new Gson().fromJson(response, EntityResponse.class);

        final ArrayList<Data> dataItems = responseObj.getData();
        final ArrayList<String> campaigns = new ArrayList<>();
        for (Data data : dataItems) {
            campaigns.add(data.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.sbd_select_campaign));
        builder.setItems(campaigns.toArray(new String[campaigns.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settings.edit().putString(Utils.SBD_ACTIVE_CAMPAIGN, new Gson().toJson(dataItems.get(which))).apply();
                tv_sbd_active_campaign.setText(campaigns.get(which));
                tv_sbd_active_campaign.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();

        Log.e("RS", responseObj.getData().get(0).getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAssociations();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.DESCRIPTOR_ASSOCIATE) {

            Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();

        } else if (requestCode == Utils.PROFILE_PICK) {

            if(data == null)
                return;

            progress = new ProgressDialog(getActivity());
            progress.setTitle("Loading");
            progress.setMessage("Please wait while the profile is loaded.");
            progress.show();

            try {
                File profile = new File(data.getData().getPath());
                new AsyncProfileLoader(ConfigurationFragment.this)
                        .execute(new FileInputStream(profile));
            } catch (FileNotFoundException e) {
                Log.e("IO", "FILE NOT FOUND " + e.toString());
            }

        }
    }

    /**
     * Gets associations that are registered by the user and applies them to the interface if they exist
     */
    private void loadAssociations() {

        //They don't exist, create new ones
        if (!settings.contains("associations")) {
            settings.edit().putString("associations", new Gson().toJson(createBaseAssociations(), Utils.ARRAY_ASSOCIATION_ITEM)).apply();
        }
        String associationsJson  = settings.getString("associations", "");
        mItems = new Gson().fromJson(associationsJson, Utils.ARRAY_ASSOCIATION_ITEM);


        //Update Layout
        for(AssociationItem item : mItems) {
            if(item.getFileExtension().equals(Utils.GEO_TAGS)) {
                tv_kml_descriptor.setText(item.getDescriptor().getName());
                tv_kml_descriptor_description.setText(item.getDescriptor().getDescription());
            }
            if(item.getFileExtension().equals(Utils.PICTURE_TAGS)) {
                tv_jpg_descriptor.setText(item.getDescriptor().getName());
                tv_jpg_descriptor_description.setText(item.getDescriptor().getDescription());
            }
            if(item.getFileExtension().equals(Utils.AUDIO_TAGS)) {
                tv_mp3_descriptor.setText(item.getDescriptor().getName());
                tv_mp3_descriptor_description.setText(item.getDescriptor().getDescription());
            }
        }
    }

    private ArrayList<AssociationItem> createBaseAssociations() {
        mItems = new ArrayList<>();

        Descriptor genericDesc = new Descriptor();

        AssociationItem gps = new AssociationItem();
        gps.setFileExtension(Utils.GEO_TAGS);
        gps.setDescriptor(genericDesc);

        AssociationItem image = new AssociationItem();
        image.setFileExtension(Utils.PICTURE_TAGS);
        image.setDescriptor(genericDesc);

        AssociationItem audio = new AssociationItem();
        audio.setFileExtension(Utils.AUDIO_TAGS);
        audio.setDescriptor(genericDesc);

        mItems.add(gps);
        mItems.add(image);
        mItems.add(audio);

        return mItems;
    }

    @Override
    public void onSuccess(ArrayList<Descriptor> result) {
        //save default descriptors to the preferences
        SharedPreferences.Editor editor = settings.edit();

        if(settings.contains(Utils.BASE_DESCRIPTORS_ENTRY)) {
            editor.remove(Utils.BASE_DESCRIPTORS_ENTRY);
        }
        editor.putString(Utils.BASE_DESCRIPTORS_ENTRY, new Gson().toJson(
                result, Utils.ARRAY_DESCRIPTORS));
        editor.apply();
        bt_file.setText(getResources().getString(R.string.edit));
        progress.dismiss();

        ChangelogItem log = new ChangelogItem();
        log.setMessage("Application profile");
        log.setDate(Utils.getDate());
        log.setTitle(getResources().getString(R.string.log_loaded));
        ChangelogManager.addLog(log, getActivity());
        Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(Exception error) {
        Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProgressUpdate(int value) {

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.bt_sbd_get_procedures:
                dispatchProceduresLoader();
                break;
            case R.id.bt_sbd_get_users:
                dispatchUsersLoader();
                break;
            case R.id.bt_sbd_pick_campaign:
                dispatchCampaignLoader();
                break;
            case R.id.bt_sbd_authenticate_user:
                dispatchUserAuthenticator();
                break;
            case R.id.dendro_configurations_save:
                saveDendroConfig();
                break;
        }
    }

    /**
     * Authenticates the user and saves the configurations
     */
    private void saveDendroConfig() {
        if(et_conf_password.getText().toString().equals("")) {
            et_conf_password.setError(getResources().getString(R.string.required));
            return;
        }
        if(et_conf_username.getText().toString().equals("")) {
            et_conf_username.setError(getResources().getString(R.string.required));
            return;
        }
        if(et_conf_address.getText().toString().equals("")) {
            et_conf_address.setError(getResources().getString(R.string.required));
            return;
        }

        SharedPreferences.Editor editor = settings.edit();
        DendroConfiguration conf;
        if (settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
            conf = new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);
        } else {
            conf = new DendroConfiguration();
        }

        conf.setAddress(et_conf_address.getText().toString());
        conf.setUsername(et_conf_username.getText().toString());
        conf.setPassword(et_conf_password.getText().toString());

        editor.putString(Utils.DENDRO_CONFS_ENTRY, new Gson().toJson(conf, DendroConfiguration.class));
        editor.apply();

        bt_save_dendro_confs.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, getResources().getDrawable(R.drawable.ic_wait), null, null);

        new AsyncAuthenticator(new AsyncTaskHandler<String>() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() == null) {
                    return;
                }

                SharedPreferences.Editor editor = settings.edit();
                DendroConfiguration conf;
                if (settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
                    conf = new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);
                } else {
                    conf = new DendroConfiguration();
                }

                conf.setAddress(et_conf_address.getText().toString());
                conf.setUsername(et_conf_username.getText().toString());
                conf.setPassword(et_conf_password.getText().toString());
                conf.setValidated(true);

                editor.putString(Utils.DENDRO_CONFS_ENTRY, new Gson().toJson(conf, DendroConfiguration.class));
                editor.apply();

                bt_save_dendro_confs.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null, getResources().getDrawable(R.drawable.ic_check), null, null);
                Toast.makeText(getActivity(), getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception error) {
                if (getActivity() == null) {
                    return;
                }

                Log.e("AUTH", "" + error.getMessage());
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                bt_save_dendro_confs.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null, getResources().getDrawable(R.drawable.ab_cross), null, null);
            }

            @Override
            public void onProgressUpdate(int value) {

            }
        }).execute(getActivity());
    }

    /**
     * Authenticates the provided credentials
     */
    private void dispatchUserAuthenticator() {
        //Check if user input is valid
        final EditText etUsername = (EditText) mRootView.findViewById(R.id.seabio_configurations_username);
        final EditText etPassword = (EditText) mRootView.findViewById(R.id.seabio_configurations_password);
        final EditText etURL = (EditText) mRootView.findViewById(R.id.seabio_configurations_address);

        if (etUsername.getText().toString().isEmpty()) {
            etUsername.setError(getString(R.string.required));
            return;
        }

        if (etPassword.getText().toString().isEmpty()) {
            etPassword.setError(getString(R.string.required));
            return;
        }

        if (etURL.getText().toString().isEmpty()) {
            etURL.setError(getString(R.string.required));
            return;
        }

        final String baseQuery = etURL.getText().toString();
        bt_sbd_username.setText(getString(R.string.loading));

        //Async request for token
        final StringRequest req = new StringRequest(Request.Method.POST, baseQuery + "/signin?",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObj = new JSONObject(response);
                            if (!responseObj.get("status").equals("success")) {
                                bt_sbd_username.setText(getString(R.string.seabio_authenticate_error));
                                bt_sbd_username.setError("");
                                return;
                            }

                            String username = ((JSONObject)responseObj.get("data")).get("name").toString();
                            SBDToken =  ((JSONObject)responseObj.get("data")).get("token").toString();

                            SharedPreferences.Editor editor = settings.edit();
                            if (settings.contains(Utils.TAG_SBD_USERNAME)) {
                                editor.remove(Utils.TAG_SBD_USERNAME);
                            }
                            editor.putString(Utils.TAG_SBD_USERNAME, username);
                            editor.putString(Utils.TAG_SBD_PASSWORD, etPassword.getText().toString());
                            editor.putString(Utils.TAG_SBD_URI, baseQuery);
                            editor.apply();

                            bt_sbd_username.setText(getString(android.R.string.ok));
                            bt_sbd_username.setEnabled(false);

                        } catch (JSONException e) {
                            bt_sbd_username.setText(getString(R.string.seabio_json_error));
                            bt_sbd_username.setError("");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        bt_sbd_username.setText(getString(R.string.seabio_authenticate_error));
                        bt_sbd_username.setError("");
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("username", etUsername.getText().toString());
                params.put("password",etPassword.getText().toString());
                return params;
            }
        };

        // add the request object to the queue to be executed
        LabTablet.getInstance().addToRequestQueue(req);
    }

    /**
     * Deploys a request to load the campaigns and shows a dialog to pick one of them as
     * an active campaign
     */
    private void dispatchCampaignLoader() {
        btPickCampaign.setText(getString(R.string.loading));

        if (!settings.contains(Utils.TAG_SBD_URI)) {
            Toast.makeText(getActivity(), "Please specify the credentials above and try again.", Toast.LENGTH_SHORT).show();
            btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
            return;
        }

        String uri = settings.getString(Utils.TAG_SBD_URI, "");

        if (null == SBDToken || uri.isEmpty() || SBDToken.isEmpty()) {
            Toast.makeText(getActivity(), "Please specify the credentials above and try again.", Toast.LENGTH_SHORT).show();
            btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
            return;
        }

        StringRequest entryRequest = new StringRequest(uri + "/api/campaigns"  + "?token=" + SBDToken, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                showCampaignPickDialog(response);
                btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                btPickCampaign.setText(getString(R.string.seabio_authenticate_error));
                btPickCampaign.setError("");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("token", SBDToken);
                return params;
            }
        };

        LabTablet.getInstance().addToRequestQueue(entryRequest);
    }

    /**
     * Deploys an async task to load the procedures from the server
     */
    private void dispatchProceduresLoader() {
        btLoadProcedures.setText(getString(R.string.loading));

        if (!settings.contains(Utils.TAG_SBD_PROCEDURES)) {
            Toast.makeText(getActivity(), getString(R.string.authenticate_first), Toast.LENGTH_SHORT).show();
            btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
            return;
        }

        String uri = settings.getString(Utils.TAG_SBD_URI, "");

        if (null == SBDToken || uri.isEmpty() || SBDToken.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.authenticate_first), Toast.LENGTH_SHORT).show();
            btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
            return;
        }

        StringRequest entryRequest = new StringRequest(uri + "/api/users"  + "?token=" + SBDToken, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //Save results

                EntityResponse responseObj = new Gson().fromJson(response, EntityResponse.class);

                final ArrayList<Data> dataItems = responseObj.getData();
                String users = "";
                for (Data data : dataItems) {
                    users += data.getName() + "\n";
                }

                if (users.length() > 0)
                    users = users.substring(0, users.length()-1);

                settings.edit().putString(Utils.TAG_SBD_PROCEDURES, new Gson().toJson(dataItems)).apply();
                tv_sbd_users.setText(users);
                btLoadUsers.setText(getString(R.string.update));
                btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
                Toast.makeText(getActivity(), getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                btPickCampaign.setText(getString(R.string.seabio_authenticate_error));
                btPickCampaign.setError("");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("token", SBDToken);
                return params;
            }
        };

        LabTablet.getInstance().addToRequestQueue(entryRequest);
    }

    /**
     * Launches a request to load the users list from the server
     */
    private void dispatchUsersLoader() {
        btLoadUsers.setText(getString(R.string.loading));

        if (!settings.contains(Utils.TAG_SBD_URI)) {
            Toast.makeText(getActivity(), "Please specify the credentials above and try again.", Toast.LENGTH_SHORT).show();
            btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
            return;
        }

        String uri = settings.getString(Utils.TAG_SBD_URI, "");

        if (null == SBDToken || uri.isEmpty() || SBDToken.isEmpty()) {
            Toast.makeText(getActivity(), "Please specify the credentials above and try again.", Toast.LENGTH_SHORT).show();
            btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
            return;
        }

        StringRequest entryRequest = new StringRequest(uri + "/api/users"  + "?token=" + SBDToken, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //Save results

                EntityResponse responseObj = new Gson().fromJson(response, EntityResponse.class);

                final ArrayList<Data> dataItems = responseObj.getData();
                String users = "";
                for (Data data : dataItems) {
                    users += data.getName() + "\n";
                }

                if (users.length() > 0)
                    users = users.substring(0, users.length()-1);

                settings.edit().putString(Utils.SBD_USERS, new Gson().toJson(dataItems)).apply();
                tv_sbd_users.setText(users);
                btLoadUsers.setText(getString(R.string.update));
                btPickCampaign.setText(getString(R.string.sbd_pick_campaign));
                Toast.makeText(getActivity(), getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                btPickCampaign.setText(getString(R.string.seabio_authenticate_error));
                btPickCampaign.setError("");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("token", SBDToken);
                return params;
            }
        };

        LabTablet.getInstance().addToRequestQueue(entryRequest);
    }
}
