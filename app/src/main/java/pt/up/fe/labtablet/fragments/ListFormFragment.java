package pt.up.fe.labtablet.fragments;


import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.FormListAdapter;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.FileMgr;

public class ListFormFragment extends ListFragment {

    ArrayList<Form> items;
    FormListAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDividerHeight(0);
        getListView().setBackgroundColor(0);
        setHasOptionsMenu(true);

        items = FileMgr.getForms(getActivity());
        mAdapter = new FormListAdapter(getActivity(), items);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_form_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() != R.id.action_add_form) {
            Toast.makeText(getActivity(), "Wrong option", Toast.LENGTH_SHORT).show();
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.get_form_name));
        builder.setIcon(R.drawable.ic_communities);

        final EditText input = new EditText(getActivity());

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.field_must_not_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                //TODO check if the form already exists

                FileMgr.addForm(
                        new Form(input.getText().toString()),
                        getActivity()
                );

                items = FileMgr.getForms(getActivity());
                mAdapter = new FormListAdapter(getActivity(), items);
                getListView().setAdapter(mAdapter);

                Toast.makeText(getActivity(), getString(android.R.string.ok), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

        return super.onOptionsItemSelected(item);
    }
}
