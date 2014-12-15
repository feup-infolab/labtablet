package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.MainActivity;
import pt.up.fe.labtablet.fragments.FormViewFragment;
import pt.up.fe.labtablet.fragments.ListFormFragment;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.OnItemClickListener;

/**
 * Created by ricardo on 11-12-2014.
 */
public class BaseFormListAdapter extends RecyclerView.Adapter<BaseFormListAdapter.BaseFormVH> {

    private final ArrayList<Form> items;
    private int rowLayout;
    private static OnItemClickListener listener;

    public BaseFormListAdapter(ArrayList<Form> srcItems,
                                   int rowLayout,
                                   OnItemClickListener clickListener) {
        this.items = srcItems;
        this.rowLayout = rowLayout;

        listener = clickListener;
    }

    @Override
    public BaseFormVH onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new BaseFormVH(v);
    }

    @Override
    public void onBindViewHolder(BaseFormVH holder, int i) {
        Form item = items.get(i);

        holder.mFormItemQuestions.setText(item.getFormQuestions().size() + " Questions");
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
        public TextView mFormTitle;
        public TextView mFormItemDescription;
        public TextView mFormItemQuestions;
        public TextView mFormItemDuration;
        public LinearLayout mFormItemDeleteView;

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
