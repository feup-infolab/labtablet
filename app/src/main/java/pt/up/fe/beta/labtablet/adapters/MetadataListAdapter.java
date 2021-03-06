package pt.up.fe.beta.labtablet.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.async.AsyncImageLoader;
import pt.up.fe.beta.labtablet.models.Descriptor;
import pt.up.fe.beta.labtablet.utils.FileMgr;
import pt.up.fe.beta.labtablet.utils.OnItemClickListener;
import pt.up.fe.beta.labtablet.utils.Utils;

/**
 * Handles each metadata item and shows additional info
 * such as its value and associated descriptor
 */
public class MetadataListAdapter extends RecyclerView.Adapter<MetadataListAdapter.MetadataListVH> {

    private final Context context;

    private final ArrayList<Descriptor> items;
    private static OnItemClickListener listener;


    public MetadataListAdapter(ArrayList<Descriptor> srcItems,
                               OnItemClickListener clickListener,
                               Context context) {

        this.context = context;
        this.items = srcItems;
        listener = clickListener;
    }

    @Override
    public MetadataListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_metadata_list, parent, false);
        return new MetadataListVH(v);
    }

    @Override
    public void onBindViewHolder(MetadataListVH holder, int position) {
        final Descriptor item = items.get(position);

        holder.mDescriptorValue.setText(item.getValue());
        holder.mDescriptorName.setText(item.getName());
        holder.mDescriptorType.setTag(item.getFilePath());

        if (item.hasFile()
                && Utils.knownImageMimeTypes.contains(FileMgr.getMimeType(item.getFilePath()))) {
            holder.mDescriptorType.setVisibility(View.VISIBLE);
            new AsyncImageLoader(holder.mDescriptorType, context, true).execute();
        } else {
            holder.mDescriptorType.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class MetadataListVH extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public final TextView mDescriptorName;
        public final TextView mDescriptorValue;
        public final ImageView mDescriptorType;
        private final LinearLayout mItemDeleteView;

        public MetadataListVH(View rowView) {
            super(rowView);
            mDescriptorName = (TextView) rowView.findViewById(R.id.metadata_item_title);
            mDescriptorType = (ImageView) rowView.findViewById(R.id.metadata_item_type);
            mDescriptorValue = (TextView) rowView.findViewById(R.id.metadata_item_value);
            mItemDeleteView = (LinearLayout) rowView.findViewById(R.id.item_metadata_list_expanded_view);

            rowView.setOnClickListener(this);
            rowView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(view, getPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            mItemDeleteView.animate();
            mItemDeleteView.setVisibility(View.VISIBLE);
            Button actionDelete = (Button) view.findViewById(R.id.action_metadata_item_delete);
            Button actionCancel = (Button) view.findViewById(R.id.action_metadata_item_delete_cancel);

            actionCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemDeleteView.setVisibility(View.GONE);
                }
            });

            actionDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onDeleteRequested(view, getPosition());
                }
            });
            return true;
        }
    }

}
