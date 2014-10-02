package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.FormEnumType;
import pt.up.fe.labtablet.models.FormQuestion;
import pt.up.fe.labtablet.utils.Utils;

public class FormSolverListAdapter extends ArrayAdapter<FormQuestion> {

    private final Activity context;
    private ArrayList<FormQuestion> items;


    public FormSolverListAdapter(Activity context, ArrayList<FormQuestion> srcItems) {
        super(context, R.layout.solver_item_text, srcItems);
        this.context = context;
        this.items = srcItems;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        FormQuestion fq = items.get(position);

        if (fq.getType().equals(FormEnumType.MULTIPLE_CHOICE)) {
            return Utils.VIEW_TYPE_CLOSED_VOCAB;
        } else if (fq.getType().equals(FormEnumType.NUMBER)) {
            return Utils.VIEW_NUMBER_PICKER;
        } else if (fq.getType().equals(FormEnumType.FREE_TEXT)) {
            return Utils.VIEW_TYPE_TEXT;
        } else {
            return -1;
        }
    }


    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        int viewType = getItemViewType(position);
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            ViewHolder viewHolder = new ViewHolder();

            switch (viewType) {
                case Utils.VIEW_TYPE_TEXT:
                    rowView = inflater.inflate(R.layout.solver_item_text, parent, false);
                    viewHolder.mQuestionBody = (TextView) rowView.findViewById(R.id.solver_question_body);
                    viewHolder.textPicker = (EditText) rowView.findViewById(R.id.solver_question_text);
                    rowView.setTag(viewHolder);
                    break;
                case Utils.VIEW_TYPE_CLOSED_VOCAB:
                    rowView = inflater.inflate(R.layout.solver_item_spinner, parent, false);
                    viewHolder.mQuestionBody = (TextView) rowView.findViewById(R.id.solver_question_body);
                    viewHolder.valuePicker = (Spinner) rowView.findViewById(R.id.solver_question_spinner);
                    rowView.setTag(viewHolder);
                    break;
                case Utils.VIEW_NUMBER_PICKER:
                    rowView = inflater.inflate(R.layout.solver_item_number, parent, false);
                    viewHolder.mQuestionBody = (TextView) rowView.findViewById(R.id.solver_question_body);
                    viewHolder.numberPicker = (NumberPicker) rowView.findViewById(R.id.solver_question_number_picker);
                    rowView.setTag(viewHolder);
                    break;
            }
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.mQuestionBody.setText(items.get(position).getQuestion());
        switch (viewType) {
            case Utils.VIEW_TYPE_TEXT:
                break;
            case Utils.VIEW_NUMBER_PICKER:
                break;
            case Utils.VIEW_TYPE_CLOSED_VOCAB:
                //TODO spinner values
                break;
            default:
                break;
        }
        return rowView;
    }

    static class ViewHolder {
        public TextView mQuestionBody;
        public NumberPicker numberPicker;
        public EditText textPicker;
        public Spinner valuePicker;
    }
}
