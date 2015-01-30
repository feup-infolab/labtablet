package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.FormEnumType;
import pt.up.fe.labtablet.models.FormQuestion;
import pt.up.fe.labtablet.utils.Utils;


/**
 * Loads and configures the view to handle creating a question and adds it
 * to a specific form
 */
public class FormQuestionCreatorActivity extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private String questionBody;
    private ArrayList<String> allowedValues;
    private FormEnumType questionType;
    private int from;
    private int to;
    private boolean mandatory;

    //Layouts for visibility handling
    private LinearLayout viewQuestionType;
    private LinearLayout viewQuestionMandatory;
    private LinearLayout viewQuestionVocabularies;
    private LinearLayout viewQuestionRange;
    private LinearLayout viewQuestionConclusion;


    //adapter for the closed vocabulary question
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_form_question_creator);
        allowedValues = new ArrayList<>();

        if (getActionBar() != null) {
            getActionBar().setTitle(getString(R.string.create_form_question_head));
        }

        viewQuestionType = (LinearLayout) findViewById(R.id.ll_question_specify_type);
        viewQuestionMandatory = (LinearLayout)  findViewById(R.id.ll_question_is_mandatory);
        viewQuestionVocabularies = (LinearLayout) findViewById(R.id.ll_question_vocabulary);
        viewQuestionRange = (LinearLayout) findViewById(R.id.ll_question_specify_range);
        viewQuestionConclusion = (LinearLayout) findViewById(R.id.ll_question_save_and_return);

        (findViewById(R.id.question_text_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText etQuestionBody = (EditText) findViewById(R.id.question_specify_text);
                if(etQuestionBody.getText().toString().equals("")) {
                    etQuestionBody.setError(getString(R.string.required));
                    return;
                }

                //keyboard, sit!
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etQuestionBody.getWindowToken(), 0);

                questionBody = etQuestionBody.getText().toString();
                Spinner questionTypeSelection = (Spinner) findViewById(R.id.question_type_spinner);

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(FormQuestionCreatorActivity.this,
                        R.array.question_types, android.R.layout.simple_spinner_item);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Apply the adapter to the spinner
                questionTypeSelection.setAdapter(adapter);
                questionTypeSelection.setOnItemSelectedListener(FormQuestionCreatorActivity.this);
                viewQuestionType.setVisibility(View.VISIBLE);

                questionTypeSelection.setEnabled(true);
                (findViewById(R.id.question_specify_text)).setEnabled(false);
                (findViewById(R.id.question_text_submit)).setEnabled(false);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.question_edition_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.action_question_cancel) {
            finish();
            return true;
        }
        if (itemId != R.id.action_question_undo)
            return false;

        //start over, manipulate views visibility
        viewQuestionType.setVisibility(View.GONE);
        viewQuestionVocabularies.setVisibility(View.GONE);
        viewQuestionRange.setVisibility(View.GONE);
        viewQuestionMandatory.setVisibility(View.GONE);
        viewQuestionConclusion.setVisibility(View.GONE);

        (findViewById(R.id.question_specify_text)).setEnabled(true);
        (findViewById(R.id.question_text_submit)).setEnabled(true);

        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if(i ==0 ) {
            return;
        }

        (findViewById(R.id.question_type_spinner)).setEnabled(false);

        //no special handling required for FREE_TEXT, NUMBER, YES/NO
        switch (i) {
            case 1:
                questionType = FormEnumType.FREE_TEXT;
                viewQuestionRange.setVisibility(View.GONE);
                viewQuestionVocabularies.setVisibility(View.GONE);

                enableMandatoryView();
                break;
            case 2:
                questionType = FormEnumType.NUMBER;
                viewQuestionRange.setVisibility(View.GONE);
                viewQuestionVocabularies.setVisibility(View.GONE);

                enableMandatoryView();
                break;
            case 3:
                questionType = FormEnumType.MULTIPLE_CHOICE;
                viewQuestionRange.setVisibility(View.GONE);
                viewQuestionVocabularies.setVisibility(View.GONE);

                allowedValues = new ArrayList<>();
                allowedValues.add(getString(R.string.yes));
                allowedValues.add(getString(R.string.no));
                enableMandatoryView();
                break;
            case 4:
                questionType = FormEnumType.MULTIPLE_CHOICE;
                viewQuestionVocabularies.setVisibility(View.VISIBLE);
                (findViewById(R.id.question_et_add_word)).setEnabled(true);
                (findViewById(R.id.question_add_word)).setEnabled(true);

                ListView lv_allowed_word = (ListView) findViewById(R.id.list_allowed_vocabulary);
                allowedValues = new ArrayList<>();
                mAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, allowedValues);
                lv_allowed_word.setAdapter(mAdapter);

                (findViewById(R.id.question_add_word)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText etWord = (EditText) findViewById(R.id.question_et_add_word);
                        if (etWord.getText().toString().equals("")) {
                            etWord.setError(getString(R.string.required));
                            return;
                        }

                        allowedValues.add(etWord.getText().toString());
                        etWord.setText("");
                        mAdapter.notifyDataSetChanged();
                    }
                });

                (findViewById(R.id.question_vocabulary_submit)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (allowedValues.isEmpty()) {
                            Toast.makeText(FormQuestionCreatorActivity.this, "At least two options should be added.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        (findViewById(R.id.question_add_word)).setEnabled(false);
                        (findViewById(R.id.question_et_add_word)).setEnabled(false);

                        questionType = FormEnumType.MULTIPLE_CHOICE;
                        enableMandatoryView();
                    }
                });
                break;
            case 5:
                questionType = FormEnumType.RANGE;
                viewQuestionRange.setVisibility(View.VISIBLE);
                (findViewById(R.id.question_specify_range_submit)).setEnabled(true);

                (findViewById(R.id.question_specify_range_submit)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText etFrom = (EditText) findViewById(R.id.question_range_from);
                        EditText etTo = (EditText) findViewById(R.id.question_range_to);

                        if(etFrom.getText().toString().equals("") ||
                                etFrom.getText().toString().equals("")) {
                            Toast.makeText(FormQuestionCreatorActivity.this, "The range fields must be specified", Toast.LENGTH_SHORT);
                            return;
                        }

                        from = Integer.parseInt(etFrom.getText().toString());
                        to = Integer.parseInt(etTo.getText().toString());
                        allowedValues = new ArrayList<>();
                        allowedValues.add("" +from);
                        allowedValues.add("" +to);
                        questionType = FormEnumType.RANGE;

                        if( from >= to) {
                            Toast.makeText(FormQuestionCreatorActivity.this, "a -> A,", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        (findViewById(R.id.question_specify_range_submit)).setEnabled(false);
                        enableMandatoryView();
                    }
                });
                break;

            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void enableMandatoryView() {
        viewQuestionMandatory.setVisibility(View.VISIBLE);
        (findViewById(R.id.mandatory_no)).setEnabled(true);
        (findViewById(R.id.mandatory_yes)).setEnabled(true);

        (findViewById(R.id.mandatory_no)).setOnClickListener(this);
        (findViewById(R.id.mandatory_yes)).setOnClickListener(this);
    }

    private void enableSubmissionView() {
        viewQuestionConclusion.setVisibility(View.VISIBLE);
        (findViewById(R.id.mandatory_no)).setEnabled(false);
        (findViewById(R.id.mandatory_yes)).setEnabled(false);

        (findViewById(R.id.question_save_and_return)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText etDuration = (EditText) findViewById(R.id.question_expected_duration);

                allowedValues.add(0, getString(R.string.pick_from_spinner));
                FormQuestion fq = new FormQuestion(questionType, questionBody, allowedValues);
                if(!etDuration.getText().toString().equals("")) {
                    fq.setDuration(Integer.parseInt(etDuration.getText().toString()));
                } else {
                    fq.setDuration(0);
                }

                fq.setMandatory(mandatory);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("form_question", new Gson().toJson(fq));
                setResult(Utils.BUILD_FORM_QUESTION, returnIntent);
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mandatory_no:
                mandatory = false;
                break;
            case R.id.mandatory_yes:
                mandatory = true;
                break;
            default:
                return;
        }
        (findViewById(R.id.mandatory_no)).setEnabled(false);
        (findViewById(R.id.mandatory_yes)).setEnabled(false);
        enableSubmissionView();
    }
}
