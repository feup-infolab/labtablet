package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.FileMgr;


public class FormSolverActivity extends Activity {

    private Form targetForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            targetForm = new Gson().fromJson(savedInstanceState.getString("form"), Form.class);
        } else {
            //TODO SHOW DIALOG WITH AVAILABLE FORM ENTRIES
            final ArrayList<Form> availableForms = FileMgr.getForms(this);
            final CharSequence[] items = {"1", "2", "3"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.select_form_solve));

        }

        setContentView(R.layout.activity_form_solver);

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