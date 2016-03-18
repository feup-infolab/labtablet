package pt.up.fe.alpha.labtablet.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.activities.FormSolverActivity;
import pt.up.fe.alpha.labtablet.models.FormQuestion;
import pt.up.fe.alpha.labtablet.utils.OnItemClickListener;

/**
 * Dialog to show the existing rows for a MultiRowQuestion type
 */
public class QuestionRowDialogFragment extends DialogFragment implements OnItemClickListener {
    private FormQuestion fq;
    private QuestionListAdapter mAdapter;
    private RecyclerView rvItems;
    private ScrollView editView;
    private LinearLayout editRootView;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     * @param fq form question to extract the rows from
     */
    public static QuestionRowDialogFragment newInstance(FormQuestion fq) {
        QuestionRowDialogFragment f = new QuestionRowDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("fq", new Gson().toJson(fq));
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fq = new Gson().fromJson(getArguments().getString("fq"), FormQuestion.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_question_rows, container, false);

        editView = (ScrollView) rootView.findViewById(R.id.fragment_dialog_edit_view);
        editRootView = (LinearLayout) rootView.findViewById(R.id.fragment_dialog_edit_layout);

        rootView.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        rootView.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FormSolverActivity)getActivity()).onFormQuestionUpdate(fq);
                dismiss();
            }
        });

        rvItems = (RecyclerView) rootView.findViewById(R.id.question_items_list);
        mAdapter = new QuestionListAdapter(fq.getRows(), this);
        rvItems.setAdapter(mAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getActivity(), "TOUCHÉ", Toast.LENGTH_SHORT).show();
        rvItems.setVisibility(View.GONE);
        editView.setVisibility(View.VISIBLE);

        for (String s : fq.getAllowedValues()) {
            //add each view for edition here
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(getActivity(), "LONG TOUCHÉ", Toast.LENGTH_SHORT).show();
    }

    private class QuestionListAdapter extends RecyclerView.Adapter<QuestionListAdapter.FormInstanceVH> {
        private final ArrayList<String> rows;
        private OnItemClickListener listener;


        public QuestionListAdapter(ArrayList<String> srcItems,
                                   OnItemClickListener clickListener) {

            this.rows = srcItems;
            listener = clickListener;
        }

        @Override
        public FormInstanceVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_question_row, parent, false);
            return new FormInstanceVH(v);
        }

        @Override
        public void onBindViewHolder(FormInstanceVH holder, final int position) {

            holder.rowText.setText(rows.get(position));
            holder.instanceDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fq.getRows().remove(position);
                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "REMOVED", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return rows == null ? 0 : rows.size();
        }

        /**
         * ViewHolder for an instance item, timestamp would be the selection criteria for the user's
         * interaction as an instance may not have a name
         */
        class FormInstanceVH extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            public final TextView rowText;
            public final ImageButton instanceDelete;

            public FormInstanceVH(View rowView) {
                super(rowView);
                rowText = (TextView) rowView.findViewById(R.id.instance_body);
                instanceDelete = (ImageButton) rowView.findViewById(R.id.instance_delete);

                rowView.setOnClickListener(this);
                rowView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View view) {
                listener.onItemClick(view, getPosition());
            }

            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        }
    }
}