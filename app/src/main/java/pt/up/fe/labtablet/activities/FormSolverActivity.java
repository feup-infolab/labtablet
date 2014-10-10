package pt.up.fe.labtablet.activities;

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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.models.FormEnumType;
import pt.up.fe.labtablet.models.FormQuestion;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

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
            targetForm = FileMgr.getForm(this, formName);
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
                Chronometer chronos = (Chronometer)(findViewById(R.id.form_solver_chrono));
                chronos.setVisibility(View.VISIBLE);
                chronos.start();

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
    }

    public View getQuestionView(FormQuestion fq) {
        LayoutInflater inflater = LayoutInflater.from(FormSolverActivity.this);
        View baseView;

        switch (fq.getType()) {
            case FREE_TEXT:
                baseView = inflater.inflate(R.layout.solver_item_text, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                break;
            case RANGE:
                baseView = inflater.inflate(R.layout.solver_item_number, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                NumberPicker np = (NumberPicker) baseView.findViewById(R.id.solver_question_number_picker);
                if (fq.getAllowedValues().size() > 0) {
                    np.setMinValue(Integer.parseInt(fq.getAllowedValues().get(0)));
                    np.setMaxValue(Integer.parseInt(fq.getAllowedValues().get(1)));
                }
                break;
            case NUMBER:
                baseView = inflater.inflate(R.layout.solver_item_text, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                ((EditText)baseView.findViewById(R.id.solver_question_text)).setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case MULTIPLE_CHOICE:
                baseView = inflater.inflate(R.layout.solver_item_spinner, null, false);
                ((TextView)baseView.findViewById(R.id.solver_question_body)).setText(fq.getQuestion());
                Spinner spValues = (Spinner) baseView.findViewById(R.id.solver_question_spinner);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                        (FormSolverActivity.this, android.R.layout.simple_spinner_dropdown_item, fq.getAllowedValues());
                spValues.setAdapter(dataAdapter);
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

        //TODO check for unanswered questions
        int viewCount = table.getChildCount();
        ArrayList<FormQuestion> fqs = targetForm.getFormQuestions();
        for (int i = 0; i < viewCount; ++i) {

            FormEnumType questionType = fqs.get(i).getType();
            View childView = table.getChildAt(i);

            switch (questionType) {
                case NUMBER:
                case FREE_TEXT:
                    EditText etSource = (EditText) childView.findViewById(R.id.solver_question_text);
                    fqs.get(i).setValue(etSource.getText().toString());
                    break;
                case MULTIPLE_CHOICE:
                    Spinner spSource = (Spinner) childView.findViewById(R.id.solver_question_spinner);
                    fqs.get(i).setValue(spSource.getSelectedItem().toString());
                    break;
                case RANGE:
                    NumberPicker npSource = (NumberPicker) childView.findViewById(R.id.solver_question_number_picker);
                    fqs.get(i).setValue("" + npSource.getValue());
                    break;
            }
        }
        Intent returnIntent = new Intent();
        targetForm.setElapsedTime(
                ((Chronometer)findViewById(R.id.form_solver_chrono)).getText().toString());

        returnIntent.putExtra("form", new Gson().toJson(targetForm));
        setResult(Utils.SOLVE_FORM, returnIntent);
        finish();
        return true;
    }

}