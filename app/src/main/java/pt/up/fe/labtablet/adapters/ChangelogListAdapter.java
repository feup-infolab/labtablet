package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.ChangelogItem;

public class ChangelogListAdapter  extends ArrayAdapter<ChangelogItem> {
 
	private final Activity context;
	private final ArrayList<ChangelogItem> logs;
    
	static class ViewHolder {  
		public TextView log; 
		public ImageView type;
		public TextView date;
        public TextView description;
	}

	public ChangelogListAdapter(Activity context, ArrayList<ChangelogItem> srcLogs) {
		super(context, R.layout.item_changelog_list, srcLogs);
		this.context = context;
		this.logs = srcLogs;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.item_changelog_list, null);
			// configure view holder
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.log = (TextView) rowView.findViewById(R.id.changelog_item_title);
			viewHolder.type = (ImageView) rowView.findViewById(R.id.changelog_item_type);
			viewHolder.date = (TextView) rowView.findViewById(R.id.changelog_item_date);
            viewHolder.description = (TextView) rowView.findViewById(R.id.changelog_item_description);
			rowView.setTag(viewHolder); 
		}

		// fill data
		ViewHolder holder = (ViewHolder) rowView.getTag();
		holder.log.setText(logs.get(position).getTitle());
		holder.date.setText(logs.get(position).getDate());
        holder.description.setText(logs.get(position).getMessage());

        if (logs.get(position).getTitle().equals(context.getResources().getString(R.string.log_added))) {
            holder.type.setImageResource(R.drawable.ab_plus);
        } else if (logs.get(position).getTitle().equals(context.getResources().getString(R.string.log_removed))) {
            holder.type.setImageResource(R.drawable.ab_cross);
        } else if (logs.get(position).getTitle().equals(context.getResources().getString(R.string.log_loaded))) {
            holder.type.setImageResource(R.drawable.ab_save);
        } else if (logs.get(position).getTitle().equals(context.getResources().getString(R.string.log_favorite_removed))) {
            holder.type.setImageResource(R.drawable.ab_cross);
        }

		return rowView;
	}
}
