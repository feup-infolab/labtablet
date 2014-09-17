package pt.up.fe.labtablet.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.FormItemListAdapter;

public class FormCreatorActivity extends Activity {

    private ListView lvFormItems;
    private Button btAddFormItem;
    private Button btEditFormItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_creator);

        lvFormItems = (ListView) findViewById(R.id.lv_form_items);



        FormItemListAdapter mAdapter = new FormItemListAdapter(this, items);
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
