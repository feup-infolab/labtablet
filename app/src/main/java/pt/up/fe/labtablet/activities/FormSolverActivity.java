package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.FormSolverListAdapter;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;


public class FormSolverActivity extends Activity {

    private Form targetForm;
    private ListView lvFormItems;
    private FormSolverListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            targetForm = new Gson().fromJson(savedInstanceState.getString("form"), Form.class);
        } else {
            String formName = getIntent().getStringExtra("form_name");
            targetForm = FileMgr.getForm(this, formName);
            if (targetForm == null) {
                Toast.makeText(this, "Form not found!", Toast.LENGTH_SHORT);
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
        lvFormItems = (ListView) findViewById(R.id.form_solver_list);

        mAdapter = new FormSolverListAdapter(this, targetForm.getFormQuestions());
        lvFormItems.setAdapter(mAdapter);

        (findViewById(R.id.bt_dismiss_form_intro)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Chronometer)(findViewById(R.id.form_solver_chrono))).start();
                lvFormItems.setVisibility(View.VISIBLE);
                (findViewById(R.id.form_solver_chrono)).setVisibility(View.VISIBLE);
                (findViewById(R.id.rl_form_solver_intro)).setVisibility(View.GONE);
            }
        });
        Toast.makeText(this, targetForm.getFormName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.form_solver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return false;
    }

}