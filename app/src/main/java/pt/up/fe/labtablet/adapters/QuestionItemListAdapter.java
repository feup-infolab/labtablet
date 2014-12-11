package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.FormEnumType;
import pt.up.fe.labtablet.models.FormQuestion;

/**
 * Adapter to manage info about each form, in a similar way as it is done
 * with favorites
 */
public class QuestionItemListAdapter extends RecyclerView.Adapter<QuestionItemListAdapter.ViewHolder> {

    private final Activity context;
    private final ArrayList<FormQuestion> items;
    private int rowLayout;

    public QuestionItemListAdapter(Activity context,
                                   ArrayList<FormQuestion> srcItems,
                                   int rowLayout) {
        this.context = context;
        this.items = srcItems;
        this.rowLayout = rowLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        //Apply data
        FormQuestion item = items.get(i);
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
                Toast.makeText(context, "TO BE CONTINUED...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mFormItemQuestion;
        public TextView mFormItemAllowedValues;
        public TextView mFormItemType;
        public Button mFormItemRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            mFormItemQuestion = (TextView) itemView.findViewById(R.id.form_item_question);
            mFormItemAllowedValues = (TextView) itemView.findViewById(R.id.form_item_values);
            mFormItemType = (TextView) itemView.findViewById(R.id.form_item_type);
            mFormItemRemove = (Button) itemView.findViewById(R.id.form_item_delete);
        }
    }
}