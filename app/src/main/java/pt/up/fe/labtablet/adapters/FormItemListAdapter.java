package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.FormEnumType;
import pt.up.fe.labtablet.models.FormQuestion;

/**
 * Adapter to manage info about each form, in a similar way as it is done
 * with favorites
 */
public class FormItemListAdapter extends ArrayAdapter<FormQuestion> {

    private final Activity context;
    private final ArrayList<FormQuestion> items;
    private final formListAdapterInterface mInterface;

    public FormItemListAdapter(Activity context,
                               ArrayList<FormQuestion> srcItems,
                               formListAdapterInterface adapterInterface) {
        super(context, R.layout.item_question, srcItems);
        this.context = context;
        this.items = srcItems;
        this.mInterface = adapterInterface;
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
            rowView = inflater.inflate(R.layout.item_question, parent, false);
            // configure view holder

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mFormItemQuestion = (TextView) rowView.findViewById(R.id.form_item_question);
            viewHolder.mFormItemType = (TextView) rowView.findViewById(R.id.form_item_type);
            viewHolder.mFormItemAllowedValues = (TextView) rowView.findViewById(R.id.form_item_values);
            viewHolder.mFormItemRemove = (Button) rowView.findViewById(R.id.form_item_delete);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.mFormItemRemove.setTag(position);
        final FormQuestion item = items.get(position);

        holder.mFormItemQuestion.setText(item.getQuestion());
        holder.mFormItemType.setText(item.getType().toString());

        if (item.getType().equals(FormEnumType.MULTIPLE_CHOICE)) {
            holder.mFormItemAllowedValues.setText(item.getAllowedValues().toString());
        } else {
            holder.mFormItemAllowedValues.setVisibility(View.GONE);
        }

        holder.mFormItemRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterface.onItemRemoval(items.get(position));
                items.remove(position);
                notifyDataSetChanged();
            }
        });

        Animation animation = AnimationUtils.makeInAnimation(context, false);
        rowView.startAnimation(animation);
        return rowView;
    }

    static class ViewHolder {
        public TextView mFormItemQuestion;
        public TextView mFormItemAllowedValues;
        public TextView mFormItemType;
        public Button mFormItemRemove;
    }

    public interface formListAdapterInterface {
        //called whenever a file is selected for removal
        public void onItemRemoval(FormQuestion q);
    }

}
