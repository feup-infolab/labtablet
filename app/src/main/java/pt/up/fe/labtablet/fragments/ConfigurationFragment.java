package pt.up.fe.labtablet.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.DescriptorPickerActivity;
import pt.up.fe.labtablet.api.AsyncAuthenticator;
import pt.up.fe.labtablet.api.AsyncProfileLoader;
import pt.up.fe.labtablet.api.AsyncTaskHandler;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.AssociationItem;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.Utils;

public class ConfigurationFragment extends Fragment {

    private ProgressDialog progress;
    private TextView tv_kml_descriptor;
    private TextView tv_kml_descriptor_description;
    private TextView tv_jpg_descriptor;
    private TextView tv_jpg_descriptor_description;
    private TextView tv_mp3_descriptor;
    private TextView tv_mp3_descriptor_description;
    private View.OnClickListener mClickListener;
    private Button bt_clear_association;
    private Button bt_gps_edit;
    private Button bt_jpg_edit;
    private Button bt_mp3_edit;
    private Button bt_file;

    private EditText et_conf_username;
    private EditText et_conf_password;
    private EditText et_conf_address;
    private Button bt_save_dendro_confs;

    private SharedPreferences.Editor editor;
    private SharedPreferences settings;
    private ArrayList<AssociationItem> mItems;

    public ConfigurationFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        editor = settings.edit();

        //configure views and buttons
        setupLayout(rootView);
        loadAssociations();

        bt_clear_association.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSharedPreferences(getResources()
                        .getString(R.string.app_name),Context.MODE_PRIVATE).edit()
                        .remove("associations")
                        .apply();

                loadAssociations();
                Toast.makeText(getActivity(), "Configuration successfully removed", Toast.LENGTH_SHORT).show();
            }
        });

        bt_file = (Button) rootView.findViewById(R.id.bt_file_path);
        bt_file.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, Utils.PROFILE_PICK);
            }
        });

        progress.dismiss();
        return rootView;
    }

    private void setupLayout(View rootView) {
        bt_clear_association = (Button) rootView.findViewById(R.id.bt_clear_associations);
        bt_gps_edit = (Button) rootView.findViewById(R.id.bt_kml_edit);
        bt_jpg_edit = (Button) rootView.findViewById(R.id.bt_jpg_edit);
        bt_mp3_edit = (Button) rootView.findViewById(R.id.bt_mp3_edit);
        bt_file = (Button) rootView.findViewById(R.id.bt_file_path);
        bt_save_dendro_confs = (Button) rootView.findViewById(R.id.dendro_configurations_save);

        et_conf_username = (EditText) rootView.findViewById(R.id.dendro_configurations_username);
        et_conf_address = (EditText) rootView.findViewById(R.id.dendro_configurations_address);
        et_conf_password = (EditText) rootView.findViewById(R.id.dendro_configurations_password);

        tv_jpg_descriptor = (TextView) rootView.findViewById(R.id.jpg_extension_descriptor);
        tv_jpg_descriptor_description = (TextView) rootView.findViewById(R.id.jpg_extension_description);

        tv_kml_descriptor = (TextView) rootView.findViewById(R.id.kml_extension_descriptor);
        tv_kml_descriptor_description = (TextView) rootView.findViewById(R.id.kml_extension_description);

        tv_mp3_descriptor = (TextView) rootView.findViewById(R.id.mp3_extension_descriptor);
        tv_mp3_descriptor_description = (TextView) rootView.findViewById(R.id.mp3_extension_description);

        mClickListener = new View.OnClickListener() {

            String extension;

            @Override
            public void onClick(View view) {
                if(!settings.contains(Utils.DESCRIPTORS_CONFIG_ENTRY)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Application Profile not loaded")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .setMessage("Please select an application profile before proceeding.")
                            .setIcon(R.drawable.ic_whats_hot)
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

        if (settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
            DendroConfiguration conf = new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);

            et_conf_address.setText(conf.getAddress());
            et_conf_password.setText(conf.getPassword());
            et_conf_username.setText(conf.getUsername());

            if(conf.validated) {
                bt_save_dendro_confs.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null, getResources().getDrawable(R.drawable.ic_check), null, null);
            }
        }

        bt_save_dendro_confs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                null, getResources().getDrawable(R.drawable.ic_error), null, null);
                    }

                    @Override
                    public void onProgressUpdate(int value) {

                    }
                }).execute(getActivity());
            }
        });
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

            new AsyncProfileLoader(new AsyncTaskHandler<ArrayList<Descriptor>>() {
                @Override
                public void onSuccess(ArrayList<Descriptor> result) {
                    //save default descriptors to the preferences
                    Type type = new TypeToken<ArrayList<Descriptor>>() {}.getType();
                    SharedPreferences.Editor editor = settings.edit();

                    if(settings.contains(Utils.DESCRIPTORS_CONFIG_ENTRY)) {
                        editor
                                .remove(Utils.DESCRIPTORS_CONFIG_ENTRY);
                    }
                    editor.putString(Utils.DESCRIPTORS_CONFIG_ENTRY, new Gson().toJson(result,type));
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
            }).execute(new File(data.getData().getPath()));
        }
    }

    private void loadAssociations() {

        //They don't exist, create new ones
        if (!settings.contains("associations")) {
            editor.putString("associations", new Gson().toJson(createBaseAssociations(), Utils.ARRAY_ASSOCIATION_ITEM));
            //mItems = new ArrayList<AssociationItem>();
            //editor.putString("associations", new Gson().toJson(mItems, Utils.ARRAY_ASSOCIATION_ITEM));
            editor.commit();
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
        mItems = new ArrayList<AssociationItem>();

        Descriptor genericDesc = new Descriptor();

        AssociationItem gps = new AssociationItem();
        gps.setFileExtension(Utils.GEO_TAGS);
        gps.setExtensionDescription(getResources().getString(R.string.gps_description));
        gps.setDescriptor(genericDesc);

        AssociationItem image = new AssociationItem();
        image.setFileExtension(Utils.PICTURE_TAGS);
        image.setExtensionDescription(getResources().getString(R.string.jpg_description));
        image.setDescriptor(genericDesc);

        AssociationItem audio = new AssociationItem();
        audio.setFileExtension(Utils.AUDIO_TAGS);
        audio.setExtensionDescription(getResources().getString(R.string.mp3_description));
        audio.setDescriptor(genericDesc);

        mItems.add(gps);
        mItems.add(image);
        mItems.add(audio);

        return mItems;
    }



}
