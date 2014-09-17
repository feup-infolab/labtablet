package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.FormItemListAdapter;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.FileMgr;

public class FormViewerActivity extends Activity {

    private ListView lvFormItems;
    private Form currentForm;
    private Button btAddFormItem;
    private Button btEditFormItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_creator);


        Intent intent = getIntent();
        currentForm = new Gson().fromJson(intent.getStringExtra("form"), Form.class);

        lvFormItems = (ListView) findViewById(R.id.lv_form_items);

        FormItemListAdapter mAdapter = new FormItemListAdapter(this, currentForm.getFormQuestions());
        lvFormItems.setAdapter(mAdapter);
        lvFormItems.setDividerHeight(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
