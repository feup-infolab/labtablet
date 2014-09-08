package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
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

import java.io.File;
import java.util.List;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.DescriptorPickerActivity;
import pt.up.fe.labtablet.models.AssociationItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Created by ricardo on 07-04-2014.
 */
public class UnvalidatedMetadataListAdapter  extends ArrayAdapter<Descriptor> {
    private final Activity context;
    private final List<Descriptor> items;
    private final Context mContext;
    private final List<AssociationItem> associations;
    private final String favoriteName;

    static class ViewHolder {
        public TextView mItemValue;
        public TextView mExtension;
        public TextView mDescriptorUri;
        public TextView mDescriptorName;
        public ImageButton bt_remove;
        public ImageView mMetadataPreview;
        public Button bt_edit;
        public Button bt_edit_value;
    }

    public UnvalidatedMetadataListAdapter(Activity context, List<Descriptor> srcItems, List<AssociationItem> associations, String favoriteName) {
        super(context, R.layout.item_unvalidated_metadata, srcItems);
        this.context = context;
        this.items = srcItems;
        this.mContext = context;
        this.associations = associations;
        this.favoriteName = favoriteName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_unvalidated_metadata, null);
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


            rowView.setTag(viewHolder);
        }

        // fill data
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.bt_remove.setTag(Integer.valueOf(position));
        holder.bt_edit.setTag(Integer.valueOf(position));
        holder.bt_edit_value.setTag(Integer.valueOf(position));

        final Descriptor item = items.get(position);
        holder.mExtension.setText(item.getTag());
        holder.mItemValue.setText(item.getValue());
        holder.mDescriptorUri.setText(item.getDescriptor());
        holder.mDescriptorName.setText(item.getName());
        holder.mMetadataPreview.setTag(item.getFilePath());
        holder.bt_edit_value.setTag(position);

        new LoadImage(holder.mMetadataPreview).execute();

        for(AssociationItem association : associations) {
            Descriptor chosenOne = association.getDescriptor();

            //there is no context associated
            if(chosenOne.getTag() == null) {
                break;
            }
            if(chosenOne.getTag().equals(item.getTag())) {
                holder.mDescriptorName.setText(chosenOne.getName());
                holder.mDescriptorUri.setText(chosenOne.getDescriptor());
                item.setState(Utils.DESCRIPTOR_STATE_VALIDATED);
                item.setDescriptor(chosenOne.getDescriptor());
                item.setName(chosenOne.getName());
            }
        }

        if(item.getTag().equals(Utils.TITLE_TAG) || item.getTag().equals(Utils.DESCRIPTION_TAG)) {
            holder.bt_edit.setEnabled(false);
            holder.bt_remove.setEnabled(false);
        } else if ( item.hasFile() ){
            holder.bt_edit_value.setEnabled(false);
            holder.bt_edit.setEnabled(true);
            holder.bt_remove.setEnabled(true);
        } else {
            holder.bt_edit_value.setEnabled(true);
            holder.bt_edit.setEnabled(true);
            holder.bt_remove.setEnabled(true);
        }

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

                                if(items.get(pos).hasFile()) {
                                    Log.e("FILE_DELETION", "" + new File((items.get(pos).getFilePath())).delete());
                                }

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
                input.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                input.setText(item.getValue());
                input.setTag(item.getValue());
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton(mContext.getResources().getString(R.string.form_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(input.getText().toString().equals("")) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.unchanged), Toast.LENGTH_SHORT).show();
                            return;
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

    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private ImageView imv;
        private String path;

        public LoadImage(ImageView imv) {
            this.imv = imv;
            this.path = imv.getTag().toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            File file = new File(path);

            if(file.exists()){
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }

            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            if (!imv.getTag().toString().equals(path)) {
               /* The path is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
                return;
            }

            if(result != null && imv != null){
                imv.setVisibility(View.VISIBLE);
                imv.setImageBitmap(result);
            } else {
                imv.setImageResource(R.drawable.ic_metadata);
            }
        }

    }
}
