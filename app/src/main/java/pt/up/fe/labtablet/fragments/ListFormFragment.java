package pt.up.fe.labtablet.fragments;


import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.BaseFormListAdapter;
import pt.up.fe.labtablet.db_handlers.FormMgr;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.utils.OnItemClickListener;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Fragment to display the list of registered base forms
 */
public class ListFormFragment extends Fragment {

    private ArrayList<Form> items;
    private RecyclerView itemList;
    private BaseFormListAdapter adapter;
    private OnItemClickListener itemClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_base_form_list, container, false);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("items")) {
                items = new Gson().fromJson(savedInstanceState.getString("items"), Utils.ARRAY_FORM);
            } else {
                Toast.makeText(getActivity(), "Failed to load state", Toast.LENGTH_SHORT).show();
            }

        } else {
            items = FormMgr.getCurrentBaseForms(getActivity());
        }

        itemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                //transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                FormViewFragment formDetail = new FormViewFragment();
                Bundle args = new Bundle();
                args.putString("form", new Gson().toJson(items.get(position)));
                formDetail.setArguments(args);
                transaction.replace(R.id.frame_container, formDetail);
                //transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.form_really_delete));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //"é para avançar, by bcp"
                        FormMgr.removeBaseFormEntry(getActivity(), items.get(position));
                        items.remove(position);
                        itemList.animate();
                        adapter.notifyItemRemoved(position);
                    }
                });
                builder.setCancelable(true);
                builder.show();

            }
        };

        adapter = new BaseFormListAdapter(items,
                itemClickListener);

        itemList = (RecyclerView) rootView.findViewById(R.id.base_form_list);
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
        itemList.setItemAnimator(new DefaultItemAnimator());
        itemList.setAdapter(adapter);
        itemList.animate();


        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("items", new Gson().toJson(items));
        super.onSaveInstanceState(outState);
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.form_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() != R.id.action_add_form) {
            Toast.makeText(getActivity(), "Wrong option", Toast.LENGTH_SHORT).show();
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.get_form_name));
        builder.setIcon(R.drawable.ic_description_black_24dp);

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


                for (Form f: items) {
                    if (f.getFormName().equals(input.getText().toString())) {
                        Toast.makeText(getActivity(), getString(R.string.form_already_exists), Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        return;
                    }
                }

                items.add(new Form(input.getText().toString(), ""));
                ArrayList<Form> forms = FormMgr.getCurrentBaseForms(getActivity());
                forms.add(new Form(input.getText().toString(), ""));
                FormMgr.overwriteBaseFormsEntry(getActivity(), forms);

                adapter = new BaseFormListAdapter(items,
                        itemClickListener);

                itemList.setAdapter(adapter);

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
