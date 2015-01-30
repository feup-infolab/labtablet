package pt.up.fe.labtablet.fragments;


import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.ChangelogListAdapter;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.ChangelogItem;

/**
 * Fragment to show the recorded logs from the application
 */
public class ListChangelogFragment extends ListFragment {

    private ArrayList<ChangelogItem> items;
    private ChangelogListAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDividerHeight(0);
        getListView().setBackgroundColor(0);
        setHasOptionsMenu(true);

        items = ChangelogManager.getItems(getActivity());
        mAdapter = new ChangelogListAdapter(getActivity(), items);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        ChangelogItem item = items.get(position);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder.setTitle(item.getTitle());
        alertDialogBuilder
                .setMessage(item.getMessage())
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.form_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(getResources().getString(R.string.dismiss), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //remove item from the logs and remove the view
                        ChangelogManager.remove(getActivity(), items.get(position));
                        items.remove(items.get(position));
                        mAdapter.notifyDataSetChanged();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();



    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_changelog, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_changelog_clear) {
            ChangelogManager.clearLogs(getActivity());
            items = new ArrayList<>();
            mAdapter = new ChangelogListAdapter(getActivity(), items);
            getListView().setAdapter(mAdapter);
        }
        return super.onOptionsItemSelected(item);
    }
}
