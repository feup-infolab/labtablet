package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.FavoriteItem;

/**
 * 
 * @author ricardo 
 * To manage info within the home page 
 * 
 */
public class FavoriteListAdapter extends ArrayAdapter<FavoriteItem>{
	private final Activity context;
	private final List<FavoriteItem> items;

	static class ViewHolder {
		public TextView mFavoriteName;
        public TextView mFavoriteDate;
		public ImageView mFavoriteType;
        public TextView mFavoriteSize;
	}

	public FavoriteListAdapter(Activity context, List<FavoriteItem> srcItems) {
		super(context, R.layout.item_favorite_list, srcItems);
		this.context = context;
		this.items = srcItems;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.item_favorite_list, null);
			// configure view holder
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.mFavoriteName = (TextView) rowView.findViewById(R.id.folder_item_title);
			viewHolder.mFavoriteType = (ImageView) rowView.findViewById(R.id.folder_item_type);
            viewHolder.mFavoriteSize = (TextView) rowView.findViewById(R.id.folder_item_size);
            viewHolder.mFavoriteDate = (TextView) rowView.findViewById(R.id.folder_item_date);
			rowView.setTag(viewHolder);
		}

		// fill data
		ViewHolder holder = (ViewHolder) rowView.getTag();
		FavoriteItem item = items.get(position);

		holder.mFavoriteName.setText(item.getTitle());
        holder.mFavoriteSize.setText(item.getSize());
        holder.mFavoriteDate.setText(item.getDate_modified());

        Animation animation = AnimationUtils.makeInAnimation(getContext(), false);
        rowView.startAnimation(animation);

		return rowView;
	}
}
