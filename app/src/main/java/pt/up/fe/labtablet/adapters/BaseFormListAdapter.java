package pt.up.fe.labtablet.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.application.LabTablet;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.OnItemClickListener;

public class BaseFormListAdapter extends RecyclerView.Adapter<BaseFormListAdapter.BaseFormVH> {

    private final ArrayList<Form> items;
    private static OnItemClickListener listener;

    public BaseFormListAdapter(ArrayList<Form> srcItems,
                                   OnItemClickListener clickListener) {
        this.items = srcItems;
        listener = clickListener;
    }

    @Override
    public BaseFormVH onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_form_list, viewGroup, false);
        return new BaseFormVH(v);
    }

    @Override
    public void onBindViewHolder(BaseFormVH holder, int i) {
        Form item = items.get(i);

        holder.mFormItemQuestions.setText(item.getFormQuestions().size() + " " +
                LabTablet.getContext().getString(R.string.questions));

        holder.mFormItemDuration.setText(item.getDuration() + " min");
        if (item.getFormDescription().equals("")) {
            holder.mFormItemDescription.setVisibility(View.GONE);
        } else {
            holder.mFormItemDescription.setVisibility(View.VISIBLE);
            holder.mFormItemDescription.setText(item.getFormDescription());
        }

        holder.mFormTitle.setText(item.getFormName());
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class BaseFormVH extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final TextView mFormTitle;
        public final TextView mFormItemDescription;
        public final TextView mFormItemQuestions;
        public final TextView mFormItemDuration;
        public final LinearLayout mFormItemDeleteView;

        public BaseFormVH(View itemView) {
            super(itemView);
            mFormTitle = (TextView) itemView.findViewById(R.id.form_name);
            mFormItemDescription = (TextView) itemView.findViewById(R.id.form_item_description);
            mFormItemQuestions = (TextView) itemView.findViewById(R.id.form_item_number_questions);
            mFormItemDuration = (TextView) itemView.findViewById(R.id.form_item_duration);
            mFormItemDeleteView = (LinearLayout) itemView.findViewById(R.id.item_form_list_expanded_view);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(view, getPosition());
        }


        @Override
        public boolean onLongClick(View view) {
            mFormItemDeleteView.animate();
            mFormItemDeleteView.setVisibility(View.VISIBLE);
            Button actionDelete = (Button) view.findViewById(R.id.action_form_item_delete);
            Button actionCancel = (Button) view.findViewById(R.id.action_form_item_delete_cancel);

            actionCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFormItemDeleteView.setVisibility(View.GONE);
                }
            });

            actionDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemLongClick(view, getPosition());
                }
            });
            return true;
        }
    }


}
