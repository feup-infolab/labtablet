package pt.up.fe.labtablet.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.async.AsyncImageLoader;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.OnItemClickListener;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Adapter to handle data files for each favorite
 */
public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.DataListVH> {

    private final Context context;

    private final ArrayList<DataItem> items;

    private int rowLayout;
    private static OnItemClickListener listener;


    public DataListAdapter( ArrayList<DataItem> items,
            int rowLayout,
            OnItemClickListener clickListener,
            Context context
            ) {

        this.context = context;
        this.items = items;
        this.rowLayout = rowLayout;

        listener = clickListener;


    }


    @Override
    public DataListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new DataListVH(v);
    }

    @Override
    public void onBindViewHolder(DataListVH holder, int position) {
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

        if (item.getMimeType() != null
                && Utils.knownImageMimeTypes.contains(item.getMimeType())) {
            new AsyncImageLoader(holder.mDescriptorType, context).execute();
        } else {
            holder.mDescriptorType.setImageResource(R.drawable.ic_file);
        }


    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class DataListVH extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        public TextView mDescriptorName;
        public TextView mDescriptorDescription;
        public TextView mDescriptorValue;
        public ImageView mDescriptorType;
        public TextView mDescriptorSize;

        public DataListVH(View rowView) {
            super(rowView);

            mDescriptorName = (TextView) rowView.findViewById(R.id.metadata_item_title);
            mDescriptorType = (ImageView) rowView.findViewById(R.id.metadata_item_type);
            mDescriptorValue = (TextView) rowView.findViewById(R.id.metadata_item_value);
            mDescriptorSize = (TextView) rowView.findViewById(R.id.metadata_item_size);
            mDescriptorDescription = (TextView) rowView.findViewById(R.id.metadata_item_description);

            rowView.setOnClickListener(this);
            rowView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(view, getPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            listener.onItemLongClick(view, getPosition());
            return true;
        }
    }
}
