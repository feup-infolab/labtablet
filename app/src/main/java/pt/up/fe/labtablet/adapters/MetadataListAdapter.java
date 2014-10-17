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

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.async.AsyncImageLoader;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.DBCon;

/**
 * Handles each metadata item and shows additional info
 * such as its value and associated descriptor
 */
public class MetadataListAdapter extends ArrayAdapter<Descriptor> {

    private final Activity context;
    private final String favoriteName;
    private final ArrayList<Descriptor> items;


    public MetadataListAdapter(Activity context, ArrayList<Descriptor> srcItems, String favoriteName) {
        super(context, R.layout.item_metadata_list, srcItems);
        this.context = context;
        this.items = srcItems;
        this.favoriteName = favoriteName;
    }

    @Override
    public void notifyDataSetChanged() {
        items.clear();
        items.addAll(DBCon.getDescriptors(favoriteName, context));
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_metadata_list, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mDescriptorName = (TextView) rowView.findViewById(R.id.metadata_item_title);
            viewHolder.mDescriptorType = (ImageView) rowView.findViewById(R.id.metadata_item_type);
            viewHolder.mDescriptorDate = (TextView) rowView.findViewById(R.id.metadata_item_date);
            viewHolder.mDescriptorValue = (TextView) rowView.findViewById(R.id.metadata_item_value);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final Descriptor item = items.get(position);

        holder.mDescriptorDate.setText(item.getDateModified());
        holder.mDescriptorValue.setText(item.getValue());
        holder.mDescriptorName.setText(item.getName());
        holder.mDescriptorType.setTag(item.getFilePath());


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
    }

}
