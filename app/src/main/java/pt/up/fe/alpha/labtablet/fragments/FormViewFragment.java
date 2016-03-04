package pt.up.fe.alpha.labtablet.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.activities.FormQuestionCreatorActivity;
import pt.up.fe.alpha.labtablet.adapters.QuestionItemListAdapter;
import pt.up.fe.alpha.labtablet.db_handlers.FormMgr;
import pt.up.fe.alpha.labtablet.models.Form;
import pt.up.fe.alpha.labtablet.models.FormQuestion;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Fragment for the form view with the associated questions
 */
public class FormViewFragment extends Fragment {

    private RecyclerView lvFormItems;
    private QuestionItemListAdapter mAdapter;

    private Form currentForm;
    private RelativeLayout rlEmptyForm;

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
            return super.onCreateView(inflater, container, null);

        } else {
            currentForm = new Gson().fromJson(
                    this.getArguments().getString("form"),
                    Form.class);
        }

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setSubtitle(currentForm.getFormName());
        }

        lvFormItems = (RecyclerView) rootView.findViewById(R.id.lv_form_items);
        rlEmptyForm = (RelativeLayout) rootView.findViewById(R.id.empty_form_view);
        mAdapter = new QuestionItemListAdapter(getActivity(),
                currentForm.getFormQuestions());

        lvFormItems.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvFormItems.setItemAnimator(new DefaultItemAnimator());
        lvFormItems.setAdapter(mAdapter);

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

        final LinearLayout descriptionHeader = (LinearLayout) rootView.findViewById(R.id.ll_set_form_description);
        descriptionHeader.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        //descriptionHeader.setVisibility(View.VISIBLE);

        (rootView.findViewById(R.id.set_form_description_no)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dismiss description
                (rootView.findViewById(R.id.set_form_description_no)).setEnabled(false);
                (rootView.findViewById(R.id.set_form_description_yes)).setEnabled(false);
                currentForm.setDescription("");

                descriptionHeader.animate()
                        .translationY(-descriptionHeader.getHeight()).setInterpolator(new AccelerateInterpolator())
                        .alpha(0.0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                descriptionHeader.setVisibility(View.GONE);
                            }
                        });
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
                        descriptionHeader.animate()
                                .translationY(-descriptionHeader.getHeight()).setInterpolator(new AccelerateInterpolator())
                                .alpha(0.0f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        descriptionHeader.setVisibility(View.GONE);
                                    }
                                });
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
        mAdapter = new QuestionItemListAdapter(getActivity(),
                currentForm.getFormQuestions());

        lvFormItems.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.form_edition_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_edit_form_save ) {
            //Update db entry

            FormMgr.updateFormEntry(currentForm, getActivity());
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.abc_shrink_fade_out_from_bottom, R.anim.abc_shrink_fade_out_from_bottom);
            transaction.replace(R.id.frame_container, new ListFormFragment());
            transaction.commit();
            return true;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.action_delete))
                .setMessage(getString(R.string.form_really_delete))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Form> forms = FormMgr.getCurrentBaseForms(getActivity());

                        if (!forms.contains(currentForm)) {
                            Toast.makeText(getActivity(), "FAIL", Toast.LENGTH_SHORT).show();
                        }
                        forms.remove(currentForm);
                        FormMgr.overwriteBaseFormsEntry(getActivity(), forms);

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
}
