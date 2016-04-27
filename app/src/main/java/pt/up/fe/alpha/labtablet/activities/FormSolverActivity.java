package pt.up.fe.alpha.labtablet.activities;


import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.fragments.QuestionRowDialogFragment;
import pt.up.fe.alpha.labtablet.models.FormEnumType;
import pt.up.fe.alpha.labtablet.models.FormInstance;
import pt.up.fe.alpha.labtablet.models.FormQuestion;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Shows the view to solve a form's set of questions and collect available metrics
 */
public class FormSolverActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener, View.OnFocusChangeListener {

    private FormInstance targetForm;
    private LinearLayout table;
    private FloatingActionButton fab;
    private CoordinatorLayout coordinatorLayout;
    private LinearLayout assistingLayout;
    private EditText focusedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            targetForm = new Gson().fromJson(savedInstanceState.getString("form"), FormInstance.class);
        } else {
            targetForm =
                    new Gson().fromJson(getIntent().getStringExtra("form"), FormInstance.class);
        }

        setContentView(R.layout.activity_form_solver);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        table = (LinearLayout) findViewById(R.id.ll_question_items);
        fab = (FloatingActionButton) findViewById(R.id.bt_form_save);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(targetForm.getParent());
            getSupportActionBar().setSubtitle("Use the buttons bellow to assist you with the form");
        }

        int questionCount = targetForm.getFormQuestions().size();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(10, 10, 10, 10);
        for (int i = 0; i < questionCount; ++i) {
            View v = getQuestionView(targetForm.getFormQuestions().get(i));
            v.setFocusable(true);
            v.setOnTouchListener(this);
            table.addView(v, layoutParams);
        }

        assistingLayout = (LinearLayout) findViewById(R.id.assisting_layout);
        OnAssistRequestListener mListener = new OnAssistRequestListener();
        for (int i = 0; i < assistingLayout.getChildCount(); ++i) {
            if (!(assistingLayout.getChildAt(i) instanceof Button))
                continue;

            assistingLayout.getChildAt(i).setOnClickListener(mListener);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean requirementsMet = true;
                int viewCount = table.getChildCount();
                ArrayList<FormQuestion> fqs = targetForm.getFormQuestions();
                for (int i = 0; i < viewCount; ++i) {

                    FormEnumType questionType = fqs.get(i).getType();
                    View childView = table.getChildAt(i);
                    //CHeck whether the question is mandatory or not
                    ImageView questionStatus = (ImageView) childView.findViewById(R.id.solver_question_status);

                    switch (questionType) {
                        case NUMBER:
                        case FREE_TEXT:
                            EditText etSource = (EditText) childView.findViewById(R.id.solver_question_text);
                            if (fqs.get(i).isMandatory() && etSource.getText().toString().equals("")) {
                                questionStatus.setVisibility(View.VISIBLE);
                                requirementsMet = false;
                                break;
                            }

                            fqs.get(i).setValue(etSource.getText().toString());
                            break;

                        case MULTIPLE_CHOICE:
                            Spinner spSource = (Spinner) childView.findViewById(R.id.solver_question_spinner);
                            if (fqs.get(i).isMandatory() &&
                                    spSource.getSelectedItem().toString().equals(getString(R.string.pick_allowed_values))) {
                                questionStatus.setVisibility(View.VISIBLE);
                                requirementsMet = false;
                                break;
                            }
                            fqs.get(i).setValue(spSource.getSelectedItem().toString());
                            break;
                        case RANGE:
                            NumberPicker npSource = (NumberPicker) childView.findViewById(R.id.solver_question_number_picker);
                            fqs.get(i).setValue("" + npSource.getValue());
                            break;
                    }
                }

                if (!requirementsMet) {
                    Toast.makeText(FormSolverActivity.this, getString(R.string.empty_questions_exist), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent returnIntent = new Intent();

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
                if (targetForm.getInstanceTimestamp() == null || targetForm.getInstanceTimestamp().equals(""))
                    targetForm.setInstanceTimestamp(dateFormat.format(new Date()));

                returnIntent.putExtra("form", new Gson().toJson(targetForm));
                setResult(Utils.SOLVE_FORM, returnIntent);
                finish();
            }
        });
    }


    private View getQuestionView(final FormQuestion fq) {
        LayoutInflater inflater = LayoutInflater.from(FormSolverActivity.this);
        final View baseView;

        switch (fq.getType()) {
            case FREE_TEXT:
                baseView = inflater.inflate(R.layout.solver_item_text, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                ((TextView)baseView.findViewById(R.id.solver_question_text)).setText(fq.getValue());
                break;
            case RANGE:
                baseView = inflater.inflate(R.layout.solver_item_number, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                NumberPicker np = (NumberPicker) baseView.findViewById(R.id.solver_question_number_picker);
                if (fq.getAllowedValues().size() > 0) {
                    //0 will be the "pick an item" option
                    //1 and 2 are respectively the lower and upper bound
                    np.setMinValue(Integer.parseInt(fq.getAllowedValues().get(1)));
                    np.setMaxValue(Integer.parseInt(fq.getAllowedValues().get(2)));
                }
                if (!fq.getValue().equals("")) {
                    np.setValue(Integer.parseInt(fq.getValue()));
                }
                break;
            case NUMBER:
                baseView = inflater.inflate(R.layout.solver_item_text, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                ((EditText)baseView.findViewById(R.id.solver_question_text)).setInputType(InputType.TYPE_CLASS_NUMBER);
                ((TextView)baseView.findViewById(R.id.solver_question_text)).setText(fq.getValue());
                break;
            case MULTIPLE_CHOICE:
                baseView = inflater.inflate(R.layout.solver_item_spinner, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                Spinner spValues = (Spinner) baseView.findViewById(R.id.solver_question_spinner);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                        (FormSolverActivity.this,
                                R.layout.solver_spinner_item,
                                fq.getAllowedValues());
                spValues.setAdapter(dataAdapter);

                if (fq.getValue().equals(""))
                    break;

                for (int i = 0; i < fq.getAllowedValues().size(); ++i) {
                    if (fq.getAllowedValues().get(i).equals(fq.getValue())) {
                        spValues.setSelection(i);
                        break;
                    }
                }
                break;
            case MULTI_INSTANCE_RESPONSE:
                baseView = inflater.inflate(R.layout.solver_item_multi_instance_response, null, false);
                ((TextView) baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                ((TextView) baseView.findViewById(R.id.question_items_count)).setText(fq.getRows().size() + " items");

                Button newRowButton = (Button) baseView.findViewById(R.id.question_add_response_instance);
                baseView.findViewById(R.id.question_items_count).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (fq.getRows().size() > 0)
                            showDialog(fq);
                        else
                            Toast.makeText(FormSolverActivity.this, getString(R.string.no_rows), Toast.LENGTH_SHORT).show();
                    }
                });
                newRowButton.setOnClickListener(new onRowAddedListener(baseView, fq));
                break;
            default:
                baseView = null;
        }

        return baseView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.form_solver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() != R.id.action_finish_form) {
            return false;
        }

        boolean requirementsMet = true;
        int viewCount = table.getChildCount();
        ArrayList<FormQuestion> fqs = targetForm.getFormQuestions();
        for (int i = 0; i < viewCount; ++i) {

            FormEnumType questionType = fqs.get(i).getType();
            View childView = table.getChildAt(i);
            //CHeck whether the question is mandatory or not
            ImageView questionStatus = (ImageView) childView.findViewById(R.id.solver_question_status);

            switch (questionType) {
                case NUMBER:
                case FREE_TEXT:
                    EditText etSource = (EditText) childView.findViewById(R.id.solver_question_text);
                    if (fqs.get(i).isMandatory() && etSource.getText().toString().equals("")) {
                        questionStatus.setVisibility(View.VISIBLE);
                        requirementsMet = false;
                        break;
                    }

                    fqs.get(i).setValue(etSource.getText().toString());
                    break;

                case MULTIPLE_CHOICE:
                    Spinner spSource = (Spinner) childView.findViewById(R.id.solver_question_spinner);
                    if (fqs.get(i).isMandatory() &&
                            spSource.getSelectedItem().toString().equals(getString(R.string.pick_allowed_values))) {
                        questionStatus.setVisibility(View.VISIBLE);
                        requirementsMet = false;
                        break;
                    }
                    fqs.get(i).setValue(spSource.getSelectedItem().toString());
                    break;
                case RANGE:
                    NumberPicker npSource = (NumberPicker) childView.findViewById(R.id.solver_question_number_picker);
                    fqs.get(i).setValue("" + npSource.getValue());
                    break;
            }
        }

        if (!requirementsMet) {
            Toast.makeText(this, getString(R.string.empty_questions_exist), Toast.LENGTH_SHORT).show();
            return true;
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra("form", new Gson().toJson(targetForm));
        setResult(Utils.SOLVE_FORM, returnIntent);
        finish();
        return true;
    }

    /**
     * This should be called whenever the user changed a question's values
     * for multi option type
     * @param fq updated form question
     */
    public void onFormQuestionUpdate(FormQuestion fq) {
        for (FormQuestion question : targetForm.getFormQuestions()) {
            if (question.getQuestion().equals(fq.getQuestion())) {
                question.setRows(fq.getRows());
                return;
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.e("FOCUS", "");
        return false;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "TOUCHÉ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocusjoao) {
        Log.e("FOCUS", "FOCUS CHANGED TO " + view.getId());
    }

    /**
     * Called whenever the user saves the info provided in each column
     * (adds the row to the form)
     */
    private class onRowSavedListener implements View.OnClickListener {
        private View rootView;
        private FormQuestion fq;

        public onRowSavedListener(View baseView, FormQuestion fq) {
            this.rootView = baseView;
            this.fq = fq;
        }

        @Override
        public void onClick(View view) {

            //Extract values from children
            LinearLayout rowsView = (LinearLayout) rootView.findViewById(R.id.repeatable_items);

            String row = "";
            for (int i = 0; i < rowsView.getChildCount(); ++i) {
                EditText et = (EditText) rowsView.getChildAt(i).findViewById(R.id.input_row);
                row += et.getText().toString() + getString(R.string.row_sepparator);
            }
            fq.addNewRow(row);
            TextView tvCount = (TextView) rootView.findViewById(R.id.question_items_count);

            tvCount.setText(fq.getRows().size() + " items");
            Toast.makeText(FormSolverActivity.this, "SAVED", Toast.LENGTH_SHORT).show();

            Animation animation = new ScaleAnimation(1,1.1f,1,1.1f);
            animation.setDuration(300);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setRepeatCount(1);
            tvCount.startAnimation(animation);

            //Remove any child from view
            ((LinearLayout) rootView.findViewById(R.id.repeatable_items)).removeAllViews();
            ((Button) rootView.findViewById(R.id.question_add_response_instance)).setText(getString(R.string.action_new_row));
            rootView.findViewById(R.id.question_add_response_instance).setOnClickListener(new onRowAddedListener(rootView, fq));
            fab.show();
        }
    }

    /**
     * Displays a dialog containing the dorm question that is passed as an argument
     * @param fq qeustion to extract the values from
     */
    void showDialog(FormQuestion fq) {
        // Create the fragment and show it as a dialog.
        DialogFragment newFragment = QuestionRowDialogFragment.newInstance(fq);
        newFragment.show(getSupportFragmentManager(), "rows_dialog");
    }

    private class onRowAddedListener implements View.OnClickListener {
        private View rootView;
        private FormQuestion fq;

        public onRowAddedListener(View baseView, FormQuestion fq) {
            this.rootView = baseView;
            this.fq = fq;
        }

        @Override
        public void onClick(View view) {
            final LinearLayout repeatableItems = (LinearLayout) rootView.findViewById(R.id.repeatable_items);
            final Button newRowButton = (Button) rootView.findViewById(R.id.question_add_response_instance);

            fab.hide();
            for (String s : fq.getAllowedValues()) {
                View editView = View.inflate(FormSolverActivity.this, R.layout.row_repeatable_question, null);
                EditText myEditText = (EditText) editView.findViewById(R.id.input_row);
                myEditText.setHint(s);
                repeatableItems.addView(editView);
            }
            newRowButton.setText(android.R.string.ok);
            newRowButton.setOnClickListener(new onRowSavedListener(rootView, fq));
        }
    }

    /**
     * Listener to handle assist request from the user to fill in structured values
     */
    private class OnAssistRequestListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            //find active/focused view
            ArrayList<EditText> focusableViews = getAllEditTexts(table);
            for (EditText et : focusableViews) {
                if (et.isFocused())
                    focusedView = et;
            }

            if (focusedView == null) {
                dispatchSnackBar("Touch one of the form's fields to fill in this value");
                return;
            }

            switch (view.getId()) {
                case R.id.assist_position:
                    break;

                case R.id.assist_date:
                    focusedView.setText(focusedView.getText() + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
                    break;

                case R.id.assist_temperature:
                    break;

                case R.id.assist_dictionary:
                    break;
            }
        }
    }

    /**
     * Displays a snackbar with the provided message
     * @param message message to display
     */
    private void dispatchSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    /**
     * Gets all editTexts associated with the viewgroup
     * @param rootView
     * @return
     */
    private ArrayList<EditText> getAllEditTexts(ViewGroup rootView) {
        ArrayList<EditText> outputs = new ArrayList<>();

        for (int i = 0; i < rootView.getChildCount(); i++)
        {
            Object child = rootView.getChildAt(i);
            if (child instanceof EditText) {
                outputs.add((EditText) child);
            }
            else if(child instanceof ViewGroup) {
                outputs.addAll(getAllEditTexts((ViewGroup)child));  // Recursive call.
            }
        }

        return outputs;
    }
}