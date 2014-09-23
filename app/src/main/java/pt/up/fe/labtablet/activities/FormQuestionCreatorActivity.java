package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.FormEnumType;
import pt.up.fe.labtablet.models.FormQuestion;
import pt.up.fe.labtablet.utils.Utils;


public class FormQuestionCreatorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_form_question_creator);

        Button btSubmit = (Button) findViewById(R.id.new_form_question_submit);
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FormQuestion fq = new FormQuestion(FormEnumType.FREE_TEXT, "Is this heaven?", "Yes it is. With cheeseburgers and all");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("form_question", new Gson().toJson(fq));
                setResult(Utils.BUILD_FORM_QUESTION, returnIntent);
                finish();
            }
        });

    }
}
