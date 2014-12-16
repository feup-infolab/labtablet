package pt.up.fe.labtablet.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.OnItemClickListener;

/**
 * Handles the list of favorites in the device, showing
 * additional info such as their size and creation date
 */
public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.FavoriteItemVH>{
	private final List<FavoriteItem> items;
    private int rowLayout;
    private static OnItemClickListener listener;

	public FavoriteListAdapter(
                               List<FavoriteItem> srcItems,
                               int rowLayout,
                               OnItemClickListener clickListener) {

		this.items = srcItems;
        this.rowLayout = rowLayout;

        listener = clickListener;
	}


    @Override
    public FavoriteItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(rowLayout, parent, false);

        return new FavoriteItemVH(v);
    }

    @Override
    public void onBindViewHolder(FavoriteItemVH holder, int position) {
        FavoriteItem item = items.get(position);
        holder.mFavoriteName.setText(item.getTitle());
        holder.mFavoriteSize.setText(item.getSize());
        holder.mFavoriteDate.setText(item.getDate_modified());
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class FavoriteItemVH  extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView mFavoriteName;
        public TextView mFavoriteDate;
        public TextView mFavoriteSize;

        public FavoriteItemVH(View itemView) {
            super(itemView);
            mFavoriteName = (TextView) itemView.findViewById(R.id.folder_item_title);
            mFavoriteSize = (TextView) itemView.findViewById(R.id.folder_item_size);
            mFavoriteDate = (TextView) itemView.findViewById(R.id.folder_item_date);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
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
