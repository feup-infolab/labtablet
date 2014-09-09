package pt.up.fe.labtablet.fragments;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.DescriptorPickerActivity;
import pt.up.fe.labtablet.activities.FieldModeActivity;
import pt.up.fe.labtablet.activities.SubmissionValidationActivity;
import pt.up.fe.labtablet.activities.ValidateMetadataActivity;
import pt.up.fe.labtablet.adapters.MetadataListAdapter;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

public class FavoriteDetailsFragment extends Fragment {

    ActionBar mActionBar;
    TextView tv_title;
    TextView tv_description;
    Button bt_fieldMode;
    Button bt_new_metadata;
    ImageButton bt_edit_title;
    ImageButton bt_edit_description;
    ListView lv_metadata;
    MetadataListAdapter mAdapter;
    private ArrayList<Descriptor> itemDescriptors;
    private String favoriteName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite_view,
                container, false);

        setHasOptionsMenu(true);

        tv_title = (TextView) rootView.findViewById(R.id.tv_title);
        tv_description = (TextView) rootView.findViewById(R.id.tv_description);
        bt_fieldMode = (Button) rootView.findViewById(R.id.bt_field_mode);
        lv_metadata = (ListView) rootView.findViewById(R.id.lv_favorite_metadata);
        bt_new_metadata = (Button) rootView.findViewById(R.id.bt_new_metadata);
        bt_edit_description = (ImageButton) rootView.findViewById(R.id.favorite_view_edit_description);
        bt_edit_title = (ImageButton) rootView.findViewById(R.id.favorite_view_edit_title);

        bt_edit_title.setTag(Utils.TITLE_TAG);
        bt_edit_description.setTag(Utils.DESCRIPTION_TAG);

        if(savedInstanceState != null) {
            favoriteName = savedInstanceState.getString("favorite_name");
        } else {
            favoriteName = this.getArguments().getString("favorite_name");
        }

        tv_title.setText(favoriteName);

        mActionBar= getActivity().getActionBar();
        mActionBar.setSubtitle(favoriteName);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        itemDescriptors = FileMgr.getDescriptors(favoriteName, getActivity());

        for(Descriptor desc : itemDescriptors) {
            /*if(!desc.getValue().equals("")) {
                definedDescriptors.add(desc);
            }*/
            if (desc.getDescriptor().contains("description")) {
                tv_description.setText(desc.getValue());
            }
            if (desc.getDescriptor().contains("title")) {
                tv_title.setText(desc.getValue());
            }
        }

        lv_metadata.setDividerHeight(0);
        mAdapter = new MetadataListAdapter(getActivity(), itemDescriptors, favoriteName);
        lv_metadata.setAdapter(mAdapter);

        dcClickListener mClickListener = new dcClickListener();
        bt_edit_title.setOnClickListener(mClickListener);
        bt_edit_description.setOnClickListener(mClickListener);

        bt_fieldMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FieldModeActivity.class);
                intent.putExtra("favorite_name", favoriteName);
                startActivity(intent);
            }
        });

        bt_new_metadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), DescriptorPickerActivity.class);
                myIntent.putExtra("file_extension", "");
                myIntent.putExtra("favoriteName", favoriteName);
                myIntent.putExtra("returnMode", Utils.DESCRIPTOR_DEFINE);
                startActivityForResult(myIntent, Utils.DESCRIPTOR_DEFINE);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Should work with simply notifyDatasetChanged() but this is not the case
        itemDescriptors = FileMgr.getDescriptors(favoriteName, getActivity());
        mAdapter = new MetadataListAdapter(getActivity(), itemDescriptors, favoriteName);

        //Update interface title and description
        //IMPRVMT - do this only if they have changed
        for(Descriptor desc : itemDescriptors) {
            if(desc.getTag().equals(Utils.TITLE_TAG)) {
                tv_title.setText(desc.getValue());
            } else if(desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                tv_description.setText(desc.getValue());
            }
        }
        lv_metadata.setAdapter(mAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null)
            return;

        //Add single descriptor
        if(requestCode == Utils.DESCRIPTOR_DEFINE || requestCode == Utils.DESCRIPTOR_GET) {
            if (!data.getExtras().containsKey("descriptor"))
                return;

            String descriptorJson = data.getStringExtra("descriptor");
            Descriptor newDescriptor = new Gson().fromJson(descriptorJson, Descriptor.class);
            itemDescriptors.add(newDescriptor);
            FileMgr.overwriteDescriptors(favoriteName, itemDescriptors, getActivity());
        } else if (requestCode == Utils.METADATA_VALIDATION) {
            if(!data.getExtras().containsKey("descriptors"))
                return;

            String descriptorsJson = data.getStringExtra("descriptors");
            itemDescriptors = new Gson().fromJson(descriptorsJson, Utils.ARRAY_DESCRIPTORS);

            FileMgr.overwriteDescriptors(favoriteName, itemDescriptors, getActivity());
        }
        this.onResume();
    }

    private class dcClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("New value");
            final View mView = view;

            // Set up the input
            final EditText input = new EditText(getActivity());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(getResources().getString(R.string.form_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(input.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Unchanged", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(mView.getTag().equals(Utils.TITLE_TAG)) {
                        if(!favoriteName.equals(input.getText().toString())) {
                            if(FileMgr.renameFavorite(favoriteName,
                                    input.getText().toString(),
                                    getActivity())) {
                                Toast.makeText(getActivity(), "Successfully updated name", Toast.LENGTH_LONG).show();
                                favoriteName = input.getText().toString();
                                tv_title.setText(favoriteName);
                            }
                        }

                    } else if (mView.getTag().equals(Utils.DESCRIPTION_TAG)) {
                        tv_description.setText(input.getText().toString());

                        itemDescriptors = FileMgr.getDescriptors(favoriteName, getActivity());
                        for(Descriptor desc : itemDescriptors) {
                            if (desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                                desc.setValue(input.getText().toString());
                            }
                        }
                        FileMgr.overwriteDescriptors(favoriteName, itemDescriptors, getActivity());
                    }
                    onResume();
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("favorite_name", favoriteName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.favorite_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == R.id.action_favorite_upload) {
            SharedPreferences settings = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
            if (!settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.dendro_confs_not_found_title))
                        .setMessage(getResources().getString(R.string.dendro_confs_not_found_message))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .setIcon(getResources().getDrawable(R.drawable.ic_error))
                        .show();
                return super.onOptionsItemSelected(item);
            }



            Intent mIntent = new Intent(getActivity(), SubmissionValidationActivity.class);
            mIntent.putExtra("favorite_name", favoriteName);

            getActivity().startActivityForResult(mIntent, Utils.SUBMISSION_VALIDATION);
            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
            getActivity().getFragmentManager().popBackStack();
            transaction.remove(FavoriteDetailsFragment.this);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            transaction.commit();
        } else if (item.getItemId() == R.id.action_favorite_delete) {
            //remove this favorite
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .setTitle(R.string.edit_metadata_item_delete)
                    .setMessage(R.string.form_really_delete_favorite)
                    .setPositiveButton(R.string.form_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FileMgr.removeFavorite(favoriteName, getActivity());

                            ChangelogItem log = new ChangelogItem();
                            log.setMessage(getResources().getString(R.string.log_favorite_removed) + favoriteName);
                            log.setDate(Utils.getDate());
                            log.setTitle(getResources().getString(R.string.log_removed));
                            ChangelogManager.addLog(log, getActivity());

                            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                            getActivity().getFragmentManager().popBackStack();
                            transaction.remove(FavoriteDetailsFragment.this);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                            transaction.commit();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();

        } else if (item.getItemId() == R.id.action_favorite_edit) {
            Intent myIntent = new Intent(getActivity(), ValidateMetadataActivity.class);
            myIntent.putExtra("favorite_name", favoriteName);
            myIntent.putExtra("descriptors", new Gson().toJson(itemDescriptors, Utils.ARRAY_DESCRIPTORS));
            startActivityForResult(myIntent, Utils.METADATA_VALIDATION);
        }
        return super.onOptionsItemSelected(item);

    }

}