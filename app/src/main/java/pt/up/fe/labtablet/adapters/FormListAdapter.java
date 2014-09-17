package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.Form;

/**
 * Created by ricardo on 9/17/14.
 */
public class FormListAdapter extends ArrayAdapter<Form> {

    private final Activity context;
    private ArrayList<Form> items;


    public FormListAdapter(Activity context, ArrayList<Form> srcItems) {
        super(context, R.layout.item_form_list, srcItems);
        this.context = context;
        this.items = srcItems;
    }

    @Override
    public void notifyDataSetChanged() {
        items.clear();
        //items.addAll(FileMgr.getFormItems(formName, context));
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_form_list, parent, false);
            // configure view holder

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mFormTitle = (TextView) rowView.findViewById(R.id.form_name);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final Form item = items.get(position);

        holder.mFormTitle.setText(item.getFormName());

        Animation animation = AnimationUtils.makeInAnimation(context, false);
        rowView.startAnimation(animation);
        return rowView;
    }

    static class ViewHolder {
        public TextView mFormTitle;
    }
}
