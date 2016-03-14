package pt.up.fe.alpha.labtablet.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
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
import pt.up.fe.alpha.labtablet.api.ChangelogManager;
import pt.up.fe.alpha.labtablet.models.ChangelogItem;
import pt.up.fe.alpha.labtablet.models.Form;
import pt.up.fe.alpha.labtablet.models.FormEnumType;
import pt.up.fe.alpha.labtablet.models.FormQuestion;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Shows the view to solve a form's set of questions and collect available metrics
 */
public class FormSolverActivity extends Activity {

    private Form targetForm;
    private LinearLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            targetForm = new Gson().fromJson(savedInstanceState.getString("form"), Form.class);
        } else {
            String formName = getIntent().getStringExtra("form_name");
            targetForm =
                    new Gson().fromJson(getIntent().getStringExtra("form"), Form.class);

            if (targetForm == null) {
                Toast.makeText(this, "Form not found!", Toast.LENGTH_SHORT).show();
                ChangelogItem item = new ChangelogItem();
                item.setMessage("Failed to load form " + formName);
                item.setTitle(getString(R.string.developer_error));
                item.setDate(Utils.getDate());
                ChangelogManager.addLog(item, this);
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_form_solver);
        table = (LinearLayout) findViewById(R.id.ll_question_items);
        (findViewById(R.id.bt_dismiss_form_intro)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                table.setVisibility(View.VISIBLE);
                (findViewById(R.id.sv_question_items)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_form_solver_intro)).setVisibility(View.GONE);
            }
        });

        if (getActionBar() != null) {
            getActionBar().setTitle(targetForm.getFormName());
        }

        int questionCount = targetForm.getFormQuestions().size();

        for (int i = 0; i < questionCount; ++i) {
            View v = getQuestionView(targetForm.getFormQuestions().get(i));
            table.addView(v);
        }

        findViewById(R.id.bt_form_save).setOnClickListener(new View.OnClickListener() {
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


                targetForm.setElapsedTime("This feature has been removed");
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:SS");
                targetForm.setTimestamp(dateFormat.format(new Date()));
                returnIntent.putExtra("form", new Gson().toJson(targetForm));
                setResult(Utils.SOLVE_FORM, returnIntent);
                finish();
            }
        });
    }

    private View getQuestionView(FormQuestion fq) {
        LayoutInflater inflater = LayoutInflater.from(FormSolverActivity.this);
        View baseView;

        switch (fq.getType()) {
            case FREE_TEXT:
                baseView = inflater.inflate(R.layout.solver_item_text, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                ((TextView)baseView.findViewById(R.id.solver_question_text)).setText(fq.getValue());
                break;
            case RANGE:
                baseView = inflater.inflate(R.layout.solver_item_number, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                ((TextView)baseView.findViewById(R.id.solver_question_text)).setText(fq.getValue());
                NumberPicker np = (NumberPicker) baseView.findViewById(R.id.solver_question_number_picker);
                if (fq.getAllowedValues().size() > 0) {
                    //0 will be the "pick an item" option
                    //1 and 2 are respectively the lower and upper bound
                    np.setMinValue(Integer.parseInt(fq.getAllowedValues().get(1)));
                    np.setMaxValue(Integer.parseInt(fq.getAllowedValues().get(2)));
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
        targetForm.setElapsedTime("This feature was disabled");

        returnIntent.putExtra("form", new Gson().toJson(targetForm));
        setResult(Utils.SOLVE_FORM, returnIntent);
        finish();
        return true;
    }

}