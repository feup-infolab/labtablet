package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.async.AsyncImageLoader;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Adapter to handle data files for each favorite
 */
public class DataListAdapter extends ArrayAdapter<DataItem> {

    private final Activity context;
    private final String favoriteName;
    private final FavoriteItem currentItem;


    public DataListAdapter(Activity context, FavoriteItem item, String favoriteName) {
        super(context, R.layout.item_data_list, item.getDataItems());
        this.context = context;
        this.currentItem = item;
        this.favoriteName = favoriteName;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_data_list, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mDescriptorName = (TextView) rowView.findViewById(R.id.metadata_item_title);
            viewHolder.mDescriptorType = (ImageView) rowView.findViewById(R.id.metadata_item_type);
            viewHolder.mDescriptorValue = (TextView) rowView.findViewById(R.id.metadata_item_value);
            viewHolder.mDescriptorSize = (TextView) rowView.findViewById(R.id.metadata_item_size);
            viewHolder.mRemoveFile = (ImageButton) rowView.findViewById(R.id.bt_remove_file);
            viewHolder.mDescriptorDescription = (TextView) rowView.findViewById(R.id.metadata_item_description);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        final ArrayList<DataItem> items = currentItem.getDataItems();
        final DataItem item = items.get(position);

        holder.mDescriptorValue.setText(item.getMimeType());
        holder.mDescriptorName.setText(new File(item.getLocalPath()).getName());
        holder.mDescriptorType.setTag(item.getLocalPath());
        holder.mDescriptorSize.setText(item.getHumanReadableSize());

        ArrayList<Descriptor> itemMetadata = item.getFileLevelMetadata();
        for (Descriptor desc : itemMetadata) {
            if (desc.getTag().equals(Utils.DESCRIPTION_TAG)) {
                if (desc.getValue().equals("")) {
                    holder.mDescriptorDescription.setVisibility(View.GONE);
                } else {
                    holder.mDescriptorDescription.setVisibility(View.VISIBLE);
                    holder.mDescriptorDescription.setText(desc.getValue());
                }
            }

            if (item.getMimeType() == null) {
                holder.mDescriptorValue.setVisibility(View.GONE);
            } else {
                holder.mDescriptorValue.setVisibility(View.VISIBLE);
            }
        }


        holder.mRemoveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.remove_file_title))
                        .setMessage(context.getString(R.string.form_really_delete))
                        .setIcon(R.drawable.ic_recycle)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                File currentFile = new File(items.get(position).getLocalPath());

                                if (!currentFile.delete()) {
                                    ChangelogItem item = new ChangelogItem();
                                    item.setMessage("Queue Processor" + "Failed to delete file "
                                            + items.get(position).getLocalPath());

                                    item.setTitle(context.getResources().getString(R.string.developer_error));
                                    item.setDate(Utils.getDate());
                                    ChangelogManager.addLog(item, context);
                                    Toast.makeText(context, "Unable to delete file", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                items.remove(item);
                                FavoriteMgr.updateFavoriteEntry(favoriteName, currentItem, context);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        holder.mDescriptorType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(items.get(position).getLocalPath().toLowerCase());
                String mime = FileMgr.getMimeType(file.getAbsolutePath());

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), mime);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.no_apps_available),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (item.getMimeType() != null
                && Utils.knownImageMimeTypes.contains(item.getMimeType())) {
            new AsyncImageLoader(holder.mDescriptorType, context).execute();
        } else {
            holder.mDescriptorType.setImageResource(R.drawable.ic_file);
        }

        Animation animation = AnimationUtils.makeInAnimation(context, false);
        rowView.startAnimation(animation);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_data_list_preview);

                final TextView dataItemDescription = (TextView) dialog.findViewById(R.id.data_item_description);
                final EditText dataItemDescriptionEdit = (EditText) dialog.findViewById(R.id.data_item_description_edit);
                final Button dataItemSubmitChanges = (Button) dialog.findViewById(R.id.data_item_submit_changes);
                final ImageView dataItemPreview = (ImageView) dialog.findViewById(R.id.data_item_preview);
                dataItemPreview.setTag(item.getLocalPath());

                final String itemDescription = item.getDescription();

                dataItemDescription.setText(itemDescription);
                dataItemDescriptionEdit.setText(itemDescription);

                dataItemDescription.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataItemSubmitChanges.setText(context.getString(R.string.action_save));
                        dataItemDescription.setVisibility(View.GONE);
                        dataItemDescriptionEdit.setVisibility(View.VISIBLE);
                    }
                });

                dataItemSubmitChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (dataItemDescription.getVisibility() == View.GONE) {
                            if (!dataItemDescriptionEdit.getText().toString().equals(itemDescription)) {


                                FavoriteItem fItem = FavoriteMgr.getFavorite(context, favoriteName);
                                fItem.getDataItems()
                                        .get(position)
                                        .setDescription(dataItemDescriptionEdit.getText().toString());

                                FavoriteMgr.updateFavoriteEntry(favoriteName, fItem, context);
                                notifyDataSetChanged();
                            }
                        }
                        dialog.dismiss();
                    }
                });

                if (item.getMimeType() != null
                        && Utils.knownImageMimeTypes.contains(item.getMimeType())) {
                    new AsyncImageLoader(dataItemPreview, context).execute();
                } else {
                    dataItemPreview.setImageResource(R.drawable.ic_file_color);
                }

                dialog.setTitle(item.getResourceName());
                dialog.show();

            }
        });
        return rowView;
    }

    static class ViewHolder {
        public TextView mDescriptorName;
        public TextView mDescriptorDescription;
        public TextView mDescriptorValue;
        public ImageView mDescriptorType;
        public TextView mDescriptorSize;
        public ImageButton mRemoveFile;
    }


}
