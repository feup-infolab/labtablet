package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.async.AsyncImageLoader;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.DBCon;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Adapter to handle data files for each favorite
 */
public class DataListAdapter extends ArrayAdapter<DataItem> {

    private final Activity context;
    private final String favoriteName;
    private final ArrayList<DataItem> items;


    public DataListAdapter(Activity context, ArrayList<DataItem> srcItems, String favoriteName) {
        super(context, R.layout.item_data_list, srcItems);
        this.context = context;
        this.items = srcItems;
        this.favoriteName = favoriteName;
    }

    @Override
    public void notifyDataSetChanged() {
        //items.clear();
       // items.addAll(DBCon.getDataDescriptionItems(context, favoriteName));
        super.notifyDataSetChanged();
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
        final DataItem item = items.get(position);

        holder.mDescriptorValue.setText(item.getMimeType());
        holder.mDescriptorName.setText(new File(item.getLocalFilePath()).getName());
        holder.mDescriptorType.setTag(item.getLocalFilePath());
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
                                if (!(new File(items.get(position).getLocalFilePath()).delete())) {
                                    ChangelogItem item = new ChangelogItem();
                                    item.setMessage("Queue Processor" + "Failed to delete file "
                                            + items.get(position).getLocalFilePath());

                                    item.setTitle(context.getResources().getString(R.string.developer_error));
                                    item.setDate(Utils.getDate());
                                    ChangelogManager.addLog(item, context);
                                    Toast.makeText(context, "Unable to delete file", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                items.remove(item);
                                DBCon.overwriteDataItems(context, items, favoriteName);

                                notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        holder.mDescriptorType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(items.get(position).getLocalFilePath());
                String mime = FileMgr.getMimeType(file.getAbsolutePath());

                if (mime == null) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.could_not_get_extension),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
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
