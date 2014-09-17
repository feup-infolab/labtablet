package pt.up.fe.labtablet.fragments;


import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.FormCreatorActivity;
import pt.up.fe.labtablet.adapters.FormListAdapter;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.Utils;

public class FormListFragment extends ListFragment {

    ArrayList<Form> items;
    FormListAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDividerHeight(0);
        getListView().setBackgroundColor(0);
        setHasOptionsMenu(true);

        items = new ArrayList<Form>();
        Form item = new Form("Forulário Leproso");
        items.add(item);


        mAdapter = new FormListAdapter(getActivity(), items);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {

        Intent intent = new Intent(getActivity(), FormCreatorActivity.class);
        intent.putExtra("form", new Gson().toJson(items.get(position), Utils.ARRAY_FORM_ITEM));
        startActivity(intent);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_form_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add_form) {
            Toast.makeText(getActivity(), "Já vai!", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
