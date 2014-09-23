package pt.up.fe.labtablet.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.FormQuestionCreatorActivity;
import pt.up.fe.labtablet.adapters.FormItemListAdapter;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.models.FormQuestion;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;


public class FormViewFragment extends Fragment {

    private ListView lvFormItems;
    private Form currentForm;
    private Button btAddFormItem;
    private Button btEditFormItems;
    private RelativeLayout rlEmptyForm;
    FormItemListAdapter mAdapter;

    public FormViewFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_form_creator, container, false);

        if (savedInstanceState != null) {
            currentForm = new Gson().fromJson(
                    savedInstanceState.getString("form"),
                    Form.class);

        } else if (!this.getArguments().containsKey("form")) {
            Toast.makeText(getActivity(), "No form received", Toast.LENGTH_SHORT).show();
            return super.onCreateView(inflater, container, savedInstanceState);

        } else {
            currentForm = new Gson().fromJson(
                    this.getArguments().getString("form"),
                    Form.class);
        }

        getActivity().getActionBar().setTitle(currentForm.getFormName());

        lvFormItems = (ListView) rootView.findViewById(R.id.lv_form_items);
        rlEmptyForm = (RelativeLayout) rootView.findViewById(R.id.empty_form_view);
        btAddFormItem = (Button) rootView.findViewById(R.id.bt_form_new_question);
        mAdapter = new FormItemListAdapter(getActivity(), currentForm.getFormQuestions());

        lvFormItems.setAdapter(mAdapter);
        lvFormItems.setDividerHeight(0);

        btAddFormItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launch intent to build a new question
                Intent mIntent = new Intent(getActivity(), FormQuestionCreatorActivity.class);
                startActivityForResult(mIntent, Utils.BUILD_FORM_QUESTION);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("form", new Gson().toJson(currentForm));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        if (currentForm.getFormQuestions().size() == 0) {
            rlEmptyForm.setVisibility(View.VISIBLE);
        } else {
            rlEmptyForm.setVisibility(View.GONE);
        }
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != Utils.BUILD_FORM_QUESTION)
            return;

        FormQuestion recFQ = new Gson().fromJson(
                data.getStringExtra("form_question"),
                FormQuestion.class);

        currentForm.addQuestion(recFQ);
        FileMgr.updateForm(currentForm, getActivity());
        mAdapter = new FormItemListAdapter(getActivity(), currentForm.getFormQuestions());
        lvFormItems.setAdapter(mAdapter);
    }
}
