package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import java.util.Date;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.AsyncImageLoader;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

public class DataListAdapter extends ArrayAdapter<Descriptor> {

    private final Activity context;
    private final String favoriteName;
    private final ArrayList<Descriptor> items;


    public DataListAdapter(Activity context, ArrayList<Descriptor> srcItems, String favoriteName) {
        super(context, R.layout.item_data_list, srcItems);
        this.context = context;
        this.items = srcItems;
        this.favoriteName = favoriteName;
    }

    @Override
    public void notifyDataSetChanged() {

        String path = Environment.getExternalStorageDirectory().toString() + "/"
                + context.getString(R.string.app_name) + "/"
                + favoriteName;

        File f = new File(path);
        File[] files = f.listFiles();
        items.clear();

        for (File inFile : files) {
            if (inFile.isFile()) {
                Descriptor newItem = new Descriptor();
                newItem.setDescriptor("");
                newItem.setFilePath(inFile.getAbsolutePath());
                newItem.setDateModified(new Date(inFile.lastModified()).toString());
                newItem.setName(inFile.getName());
                newItem.setValue(FileMgr.getMimeType(inFile.getAbsolutePath()));
                items.add(newItem);
            }
        }

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
            viewHolder.mDescriptorDate = (TextView) rowView.findViewById(R.id.metadata_item_date);
            viewHolder.mDescriptorValue = (TextView) rowView.findViewById(R.id.metadata_item_value);
            viewHolder.mDescriptorSize = (TextView) rowView.findViewById(R.id.metadata_item_size);
            viewHolder.mRemoveFile = (ImageButton) rowView.findViewById(R.id.bt_remove_file);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final Descriptor item = items.get(position);

        holder.mDescriptorDate.setText(item.getDateModified());
        holder.mDescriptorValue.setText(item.getValue());
        holder.mDescriptorName.setText(item.getName());
        holder.mDescriptorType.setTag(item.getFilePath());
        holder.mDescriptorSize.setText(item.getSize());


        holder.mRemoveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.remove_file_title))
                        .setMessage(context.getString(R.string.form_really_delete))
                        .setIcon(R.drawable.ic_recycle)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (!(new File(items.get(position).getFilePath()).delete())) {
                                    ChangelogItem item = new ChangelogItem();
                                    item.setMessage("Queue Processor" + "Failed to delete file "
                                            + items.get(position).getFilePath());

                                    item.setTitle(context.getResources().getString(R.string.developer_error));
                                    item.setDate(Utils.getDate());
                                    ChangelogManager.addLog(item, context);
                                }
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        holder.mDescriptorType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(items.get(position).getFilePath());
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

        new AsyncImageLoader(holder.mDescriptorType, context).execute();

        Animation animation = AnimationUtils.makeInAnimation(context, false);
        rowView.startAnimation(animation);

        return rowView;
    }

    static class ViewHolder {
        public TextView mDescriptorName;
        public TextView mDescriptorValue;
        public ImageView mDescriptorType;
        public TextView mDescriptorDate;
        public TextView mDescriptorSize;
        public ImageButton mRemoveFile;
    }
}
