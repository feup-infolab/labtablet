package pt.up.fe.labtablet.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.FormQuestionCreatorActivity;
import pt.up.fe.labtablet.adapters.FormItemListAdapter;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.models.FormQuestion;
import pt.up.fe.labtablet.utils.DBCon;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Fragment for the form view with the associated questions
 */
public class FormViewFragment extends Fragment implements FormItemListAdapter.formListAdapterInterface {

    private ListView lvFormItems;
    private Form currentForm;
    private RelativeLayout rlEmptyForm;
    private FormItemListAdapter mAdapter;
    private FormItemListAdapter.formListAdapterInterface mInterface;

    public FormViewFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_question_creator, container, false);

        //restore state, if applicable
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

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setTitle(currentForm.getFormName());
        }

        lvFormItems = (ListView) rootView.findViewById(R.id.lv_form_items);
        rlEmptyForm = (RelativeLayout) rootView.findViewById(R.id.empty_form_view);
        mAdapter = new FormItemListAdapter(getActivity(), currentForm.getFormQuestions(), this);

        lvFormItems.setAdapter(mAdapter);
        lvFormItems.setDividerHeight(0);

        (rootView.findViewById(R.id.bt_form_new_question)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launch intent to build a new question
                Intent mIntent = new Intent(getActivity(), FormQuestionCreatorActivity.class);
                startActivityForResult(mIntent, Utils.BUILD_FORM_QUESTION);
            }
        });

        setHasOptionsMenu(true);

        //handle form description
        if (currentForm.isDescriptionSet()) {
            (rootView.findViewById(R.id.ll_set_form_description)).setVisibility(View.GONE);
            return rootView;
        }

        (rootView.findViewById(R.id.ll_set_form_description)).setVisibility(View.VISIBLE);
        (rootView.findViewById(R.id.set_form_description_no)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dismiss description
                (rootView.findViewById(R.id.set_form_description_no)).setEnabled(false);
                (rootView.findViewById(R.id.set_form_description_yes)).setEnabled(false);
                currentForm.setDescription("");
                DBCon.updateForm(currentForm, getActivity());
                (rootView.findViewById(R.id.ll_set_form_description)).setVisibility(View.GONE);
            }
        });

        (rootView.findViewById(R.id.set_form_description_yes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (rootView.findViewById(R.id.ll_set_form_description_lower_view)).setVisibility(View.VISIBLE);
                (rootView.findViewById(R.id.set_form_description_no)).setEnabled(false);
                (rootView.findViewById(R.id.set_form_description_yes)).setEnabled(false);
                (rootView).findViewById(R.id.bt_form_description_submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText etFormDescription = (EditText) (rootView.findViewById(R.id.et_set_form_description));
                        currentForm.setDescription(etFormDescription.getText().toString());
                        DBCon.updateForm(currentForm, getActivity());
                        (rootView.findViewById(R.id.ll_set_form_description)).setVisibility(View.GONE);
                    }
                });

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

        //event was cancelled
        if (data == null)
            return;

        FormQuestion recFQ = new Gson().fromJson(
                data.getStringExtra("form_question"),
                FormQuestion.class);

        currentForm.addQuestion(recFQ);
        DBCon.updateForm(currentForm, getActivity());
        mAdapter = new FormItemListAdapter(getActivity(), currentForm.getFormQuestions(), mInterface);
        lvFormItems.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.form_edition_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() != R.id.action_edit_form_delete) {
            return false;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.action_delete))
                .setMessage(getString(R.string.form_really_delete))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DBCon.deleteForm(currentForm.getFormName(), getActivity());
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        //transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                        transaction.replace(R.id.frame_container, new ListFormFragment());
                        transaction.commit();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setIcon(R.drawable.ic_recycle)
                .show();

        return true;
    }

    @Override
    public void onItemRemoval(FormQuestion q) {

        //TODO deal with this
    }
}
