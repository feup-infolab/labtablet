package pt.up.fe.alpha.labtablet.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.db_handlers.FormMgr;
import pt.up.fe.alpha.labtablet.models.Form;
import pt.up.fe.alpha.labtablet.models.FormInstance;
import pt.up.fe.alpha.labtablet.utils.OnItemClickListener;

/**
 * Handles each form item and shows additional info
 * such as its value and associated descriptor
 */
public class FormListAdapter extends RecyclerView.Adapter<FormListAdapter.FormListVH> {

    private final Context context;

    private final HashMap<String, ArrayList<FormInstance>> items;
    private ArrayList<Form> templateForms;
    private static OnItemClickListener listener;


    public FormListAdapter(HashMap<String, ArrayList<FormInstance>> srcItems,
                               OnItemClickListener clickListener,
                               Context context) {

        this.context = context;
        this.items = srcItems;
        this.templateForms = FormMgr.getCurrentBaseForms(context);
        listener = clickListener;
    }

    @Override
    public FormListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_form_list, parent, false);
        return new FormListVH(v);
    }

    @Override
    public void onBindViewHolder(FormListVH holder, int position) {
        String baseFormName = (String) items.keySet().toArray()[position];

        if (items.get(baseFormName).size() > 0) {
            holder.mFormDescription.setText(templateForms.get(position).getFormDescription());
        } else {
            holder.mFormDescription.setVisibility(View.INVISIBLE);
        }
        holder.mFormDuration.setVisibility(View.INVISIBLE);
        holder.mFormItemCount.setText(items.get(baseFormName).size() + " items");
        holder.mFormName.setText(baseFormName);

    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class FormListVH extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public final TextView mFormName;
        public final TextView mFormDescription;
        public final TextView mFormDuration;
        public final TextView mFormItemCount;

        public FormListVH(View rowView) {
            super(rowView);
            mFormName = (TextView) rowView.findViewById(R.id.form_name);
            mFormDescription = (TextView) rowView.findViewById(R.id.form_item_description);
            mFormDuration = (TextView) rowView.findViewById(R.id.form_item_duration);
            mFormItemCount = (TextView) rowView.findViewById(R.id.form_item_number_questions);

            rowView.setOnClickListener(this);
            rowView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(view, getPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            /*
            mItemDeleteView.animate();
            mItemDeleteView.setVisibility(View.VISIBLE);
            Button actionDelete = (Button) view.findViewById(R.id.action_metadata_item_delete);
            Button actionCancel = (Button) view.findViewById(R.id.action_metadata_item_delete_cancel);

            actionCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemDeleteView.setVisibility(View.GONE);
                }
            });

            actionDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemLongClick(view, getPosition());
                }
            });
            */
            return true;
        }
    }

}
