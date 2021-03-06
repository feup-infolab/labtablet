package pt.up.fe.beta.labtablet.adapters;

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

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.models.Dendro.DendroFolderItem;

/**
 * Adapter to handle displaying the folders from the repository
 */
public class DendroFolderAdapter extends ArrayAdapter<DendroFolderItem> {
    private final Activity context;
    private final List<DendroFolderItem> items;

    public DendroFolderAdapter(Activity context, List<DendroFolderItem> srcItems) {
        super(context, R.layout.item_dendro_folder, srcItems);
        this.context = context;
        this.items = srcItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_dendro_folder, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mFolderName = (TextView) rowView.findViewById(R.id.folder_item_title);
            viewHolder.mFolderDate = (TextView) rowView.findViewById(R.id.folder_item_date);
            viewHolder.mFolderUri = (TextView) rowView.findViewById(R.id.folder_item_size);
            viewHolder.mFolderType = (ImageView) rowView.findViewById(R.id.folder_item_type);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final DendroFolderItem item = items.get(position);
        holder.mFolderName.setText((String)item.getNie().getTitle());
        holder.mFolderDate.setText((String)item.getDcterms().getModified());
        holder.mFolderUri.setText(item.getUri());

        //if (item.getDdr().getFileExtension().equals("folder")) {
        if (item.getRdf().getType().toString().contains("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#Folder")) {
            holder.mFolderType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_folder));
        } else {
            holder.mFolderType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file));
        }

        Animation animation = AnimationUtils.makeInChildBottomAnimation(context);
        rowView.startAnimation(animation);
        return rowView;
    }

    class ViewHolder {
        public TextView mFolderName;
        public TextView mFolderDate;
        public TextView mFolderUri;
        public ImageView mFolderType;
    }
}
