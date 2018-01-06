package pt.up.fe.alpha.labtablet.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.async.AsyncImageLoader;
import pt.up.fe.alpha.labtablet.models.Dendro.Sync;
import pt.up.fe.alpha.labtablet.utils.FileMgr;
import pt.up.fe.alpha.labtablet.utils.OnItemClickListener;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Handles each Dendro synchronization event and shows additional info
 * such as its target folder title and synchronization date
 */
public class DendroSyncListAdapter extends RecyclerView.Adapter<DendroSyncListAdapter.SyncListVH> {

    private final Context context;

    private final ArrayList<Sync> items;
    private static OnItemClickListener listener;


    public DendroSyncListAdapter(ArrayList<Sync> srcItems,
                                 OnItemClickListener clickListener,
                                 Context context) {

        this.context = context;
        this.items = srcItems;
        listener = clickListener;
    }

    @Override
    public SyncListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_sync_list, parent, false);
        return new SyncListVH(v);
    }

    @Override
    public void onBindViewHolder(SyncListVH holder, int position) {
        final Sync item = items.get(position);
        
        Date date = item.getExportDate();
        holder.mExportDate.setText(date.toLocaleString());
        holder.mFolderTitle.setText(item.getFolderTitle());
        holder.mDendroTargetFolderUri.setText(item.getDendroFolderUri());
        holder.mDendroTargetAddress.setText(item.getDendroInstanceUri());
        holder.mSyncOK.setTag(item.isOk());

        if(item.isOk())
            holder.mSyncOK.setImageResource(R.drawable.ic_check);
        else
            holder.mSyncOK.setImageResource(R.drawable.ic_warning);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class SyncListVH extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public final TextView mFolderTitle;
        public final TextView mDendroTargetFolderUri;
        public final TextView mDendroTargetAddress;
        public final ImageView mSyncOK;
        public final TextView mExportDate;
        private final LinearLayout mItemDeleteView;

        public SyncListVH(View rowView) {
            super(rowView);
            mFolderTitle = (TextView) rowView.findViewById(R.id.sync_item_title);
            mDendroTargetFolderUri = (TextView) rowView.findViewById(R.id.sync_item_uri);
            mDendroTargetAddress = (TextView) rowView.findViewById(R.id.sync_item_address);
            mSyncOK = (ImageView) rowView.findViewById(R.id.sync_item_ok);
            mExportDate = (TextView) rowView.findViewById(R.id.sync_item_date);
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
