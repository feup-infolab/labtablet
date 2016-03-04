package pt.up.fe.alpha.labtablet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.models.Descriptor;


public class DescriptorsListAdapter extends ArrayAdapter<Descriptor> {

    private final Activity context;
    private final List<Descriptor> items;

    public DescriptorsListAdapter(Activity context, List<Descriptor> srcItems) {
        super(context, R.layout.item_descriptor_list, srcItems);
        this.context = context;
        this.items = srcItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_descriptor_list, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mDescriptorName = (TextView) rowView.findViewById(R.id.item_title);
            viewHolder.mDescriptor = (TextView) rowView.findViewById(R.id.item_descriptor);
            viewHolder.mDescriptorDescription = (TextView) rowView.findViewById(R.id.item_description);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Descriptor item = items.get(position);

        holder.mDescriptorName.setText(item.getName());
        holder.mDescriptor.setText(item.getDescriptor());
        holder.mDescriptorDescription.setText(item.getDescription());
        return rowView;
    }

    static class ViewHolder {
        public TextView mDescriptorName;
        public TextView mDescriptor;
        public TextView mDescriptorDescription;
    }
}
