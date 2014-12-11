package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.fragments.FormViewFragment;
import pt.up.fe.labtablet.models.Form;

/**
 * Adapter for the list of available forms
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
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_form_list, parent, false);
            // configure view holder

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mFormTitle = (TextView) rowView.findViewById(R.id.form_name);
            viewHolder.mFormItemDescription = (TextView) rowView.findViewById(R.id.form_item_description);
            viewHolder.mFormItemDuration = (TextView) rowView.findViewById(R.id.form_item_duration);
            viewHolder.mFormItemQuestions = (TextView) rowView.findViewById(R.id.form_item_number_questions);
            rowView.setTag(viewHolder);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentTransaction transaction = context.getFragmentManager().beginTransaction();
                    //transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                    FormViewFragment formDetail = new FormViewFragment();
                    Bundle args = new Bundle();
                    args.putString("form", new Gson().toJson(items.get(position)));
                    formDetail.setArguments(args);
                    transaction.replace(R.id.frame_container, formDetail);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final Form item = items.get(position);

        holder.mFormItemQuestions.setText(item.getFormQuestions().size() + " Questions");
        holder.mFormItemDuration.setText(item.getDuration() + " min");
        if (item.getFormDescription().equals("")) {
            holder.mFormItemDescription.setVisibility(View.GONE);
        } else {
            holder.mFormItemDescription.setVisibility(View.VISIBLE);
            holder.mFormItemDescription.setText(item.getFormDescription());
        }

        holder.mFormTitle.setText(item.getFormName());
        Animation animation = AnimationUtils.makeInAnimation(context, false);
        rowView.startAnimation(animation);
        return rowView;
    }

    static class ViewHolder {
        public TextView mFormTitle;
        public TextView mFormItemDescription;
        public TextView mFormItemQuestions;
        public TextView mFormItemDuration;
    }
}