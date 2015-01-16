package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.DescriptorPickerActivity;
import pt.up.fe.labtablet.async.AsyncImageLoader;
import pt.up.fe.labtablet.models.AssociationItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Handles the metadata records that are still to be validated
 * eg to be associated with a descriptor
 */
public class UnvalidatedMetadataListAdapter extends ArrayAdapter<Descriptor> {
    private final Activity context;
    private final List<Descriptor> items;
    private final Context mContext;
    private final List<AssociationItem> associations;
    private final String favoriteName;
    private final unvalidatedMetadataInterface mInterface;

    public UnvalidatedMetadataListAdapter(Activity context, List<Descriptor> srcItems, List<AssociationItem> associations, String favoriteName, unvalidatedMetadataInterface actInterface) {
        super(context, R.layout.item_unvalidated_metadata, srcItems);
        this.context = context;
        this.items = srcItems;
        this.mContext = context;
        this.associations = associations;
        this.favoriteName = favoriteName;
        this.mInterface = actInterface;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // let us all reuse these amazing views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_unvalidated_metadata, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mItemValue = (TextView) rowView.findViewById(R.id.unvalidated_meta_value);
            viewHolder.mDescriptorUri = (TextView) rowView.findViewById(R.id.unvalidated_meta_descriptor_uri);
            viewHolder.mExtension = (TextView) rowView.findViewById(R.id.unvalidated_meta_extension);
            viewHolder.mDescriptorName = (TextView) rowView.findViewById(R.id.unvalidated_meta_descriptor_name);
            viewHolder.bt_remove = (ImageButton) rowView.findViewById(R.id.bt_delete_unvalidated_metadata);
            viewHolder.bt_edit = (Button) rowView.findViewById(R.id.bt_edit_unvalidated_metadata);
            viewHolder.mMetadataPreview = (ImageView) rowView.findViewById(R.id.iv_metadata_preview);
            viewHolder.bt_edit_value = (Button) rowView.findViewById(R.id.bt_edit_unvalidated_metadata_value);
            viewHolder.bt_make_it_data = (ImageButton) rowView.findViewById(R.id.bt_make_it_data);

            rowView.setTag(viewHolder);
        }

        // put some data on them
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.bt_remove.setTag(position);
        holder.bt_edit.setTag(position);
        holder.bt_edit_value.setTag(position);
        holder.bt_make_it_data.setTag(position);

        final Descriptor item = items.get(position);
        holder.mExtension.setText(item.getTag());
        holder.mItemValue.setText(item.getValue());
        holder.mDescriptorUri.setText(item.getDescriptor());
        holder.mDescriptorName.setText(item.getName());
        holder.mMetadataPreview.setTag(item.getFilePath());
        holder.bt_edit_value.setTag(position);

        if (item.hasFile()
                && Utils.knownImageMimeTypes.contains(FileMgr.getMimeType(item.getFilePath()))) {
            new AsyncImageLoader(holder.mMetadataPreview, context, true).execute();
        } else {
            holder.mMetadataPreview.setImageResource(R.drawable.ic_file);
        }

        for (AssociationItem association : associations) {
            Descriptor chosenOne = association.getDescriptor();

            //there is no context associated, reject take off
            if (chosenOne.getTag() == null) {
                break;
            }
            if (chosenOne.getTag().equals(item.getTag())) {
                holder.mDescriptorName.setText(chosenOne.getName());
                holder.mDescriptorUri.setText(chosenOne.getDescriptor());
                item.setState(Utils.DESCRIPTOR_STATE_VALIDATED);
                item.setDescriptor(chosenOne.getDescriptor());
                item.setName(chosenOne.getName());
            }
        }

        // ------ Special descriptors handling ------
        //Can't be edited or removed, descriptor can't change
        if (item.getTag().equals(Utils.TITLE_TAG)) {
            holder.bt_edit.setEnabled(false);
            holder.bt_edit_value.setEnabled(false);
        }
        //Can be edited, but not removed nor change its descriptor
        else if (item.getTag().equals(Utils.DESCRIPTION_TAG)) {
            holder.bt_edit.setEnabled(false);
            holder.bt_edit_value.setEnabled(true);
        }
        //Can edit both value and descriptor

        //------- File handling -----
        //Can't change its value
        if (item.hasFile()) {
            holder.bt_edit_value.setEnabled(false);
            holder.bt_edit.setEnabled(true);
            holder.bt_remove.setVisibility(View.VISIBLE);
            holder.bt_make_it_data.setVisibility(View.VISIBLE);

        //can change everything or be removed
        } else {
            holder.bt_remove.setVisibility(View.VISIBLE);
            holder.bt_make_it_data.setVisibility(View.INVISIBLE);
        }

        holder.bt_make_it_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new AlertDialog.Builder(mContext)
                        .setIcon(R.drawable.ab_box)
                        .setTitle(R.string.form_convert_data_title)
                        .setMessage(R.string.form_convert_data)
                        .setPositiveButton(R.string.form_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int pos = (Integer) view.getTag();
                                if (items.get(pos).hasFile()) {
                                    //Add file for deletion
                                    mInterface.onDataMigration(items.get(pos));
                                }
                                //remove the descriptor
                                items.remove(pos);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        holder.bt_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new AlertDialog.Builder(mContext)
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setTitle(R.string.edit_metadata_item_delete)
                        .setMessage(R.string.form_really_delete)
                        .setPositiveButton(R.string.form_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int pos = (Integer) view.getTag();
                                if (items.get(pos).hasFile()) {
                                    //Add file for deletion
                                    mInterface.onFileDeletion(items.get(pos));
                                }
                                //remove the descriptor
                                items.remove(pos);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        holder.bt_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = (Integer) view.getTag();
                Intent myIntent = new Intent(mContext, DescriptorPickerActivity.class);
                myIntent.putExtra("file_extension", items.get(pos).getTag());
                myIntent.putExtra("descriptor", new Gson().toJson(items.get(pos), Descriptor.class));
                myIntent.putExtra("returnMode", Utils.DESCRIPTOR_GET);
                myIntent.putExtra("favoriteName", favoriteName);
                ((Activity) mContext).startActivityForResult(myIntent, 2);
            }
        });

        holder.bt_edit_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int pos = (Integer) view.getTag();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(items.get(pos).getName());

                final EditText input = new EditText(mContext);
                final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setLayoutParams(lparams);

                input.setText(item.getValue());
                input.setTag(item.getValue());
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton(mContext.getResources().getString(R.string.form_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().equals("")) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.unchanged), Toast.LENGTH_SHORT).show();
                        } else {
                            item.setValue(input.getText().toString());
                            notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton(mContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });
        return rowView;
    }

    /**
     * Queue to process items as soon as the changes are applied
     */
    public interface unvalidatedMetadataInterface {
        //called whenever a file is selected for removal
        public void onFileDeletion(Descriptor desc);

        //When a metadata record is migrated to the data folder
        public void onDataMigration(Descriptor desc);
    }

    static class ViewHolder {
        public TextView mItemValue;
        public TextView mExtension;
        public TextView mDescriptorUri;
        public TextView mDescriptorName;
        public ImageButton bt_remove;
        public ImageView mMetadataPreview;
        public Button bt_edit;
        public Button bt_edit_value;
        public ImageButton bt_make_it_data;
    }


}
