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
import pt.up.fe.labtablet.models.FormQuestion;

public class FormItemListAdapter extends ArrayAdapter<FormQuestion> {

    private final Activity context;
    private ArrayList<FormQuestion> items;


    public FormItemListAdapter(Activity context, ArrayList<FormQuestion> srcItems) {
        super(context, R.layout.item_question, srcItems);
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
            rowView = inflater.inflate(R.layout.item_question, parent, false);
            // configure view holder

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mFormItemQuestion = (TextView) rowView.findViewById(R.id.form_item_question);
            viewHolder.mFormItemType = (TextView) rowView.findViewById(R.id.form_item_type);
            viewHolder.mFormItemAllowedValues = (TextView) rowView.findViewById(R.id.form_item_values);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final FormQuestion item = items.get(position);

        holder.mFormItemQuestion.setText(item.getQuestion());
        holder.mFormItemType.setText(item.getType().toString());
        holder.mFormItemAllowedValues.setText(item.getAllowedValues().toString());

        Animation animation = AnimationUtils.makeInAnimation(context, false);
        rowView.startAnimation(animation);
        return rowView;
    }

    static class ViewHolder {
        public TextView mFormItemQuestion;
        public TextView mFormItemAllowedValues;
        public TextView mFormItemType;
    }

}
