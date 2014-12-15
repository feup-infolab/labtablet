package pt.up.fe.labtablet.fragments;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.FavoriteListAdapter;
import pt.up.fe.labtablet.async.AsyncSearchTask;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.models.FavoriteItem;

public class SearchFragment extends Fragment {


    public SearchFragment(){}
    private EditText et_query;
    private ProgressDialog progress;
    private FavoriteListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ListView lv_results = (ListView) rootView.findViewById(R.id.lv_results);
        Button bt_submit = (Button) rootView.findViewById(R.id.bt_search);
        et_query = (EditText) rootView.findViewById(R.id.et_search_query);

        //mAdapter = new FavoriteListAdapter(getActivity(), new ArrayList<FavoriteItem>());

        //lv_results.setAdapter(mAdapter);
        lv_results.setDividerHeight(0);

        bt_submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(et_query.getText().toString().isEmpty())
                    return;

                progress = new ProgressDialog(getActivity());
                progress.setTitle("Loading");
                progress.setMessage("Please wait while the results are fetched from the repository. This may take a while, depending on your connection.");
                progress.show();


                new AsyncSearchTask(new AsyncTaskHandler<ArrayList<FavoriteItem>>() {

                    @Override
                    public void onSuccess(ArrayList<FavoriteItem> result) {
                        if (getActivity() == null) {
                            return;
                        }
                        //mAdapter.notifyDataSetInvalidated();
                        //mAdapter.clear();
                        //mAdapter.addAll(result);
                        //mAdapter.notifyDataSetChanged();
                        // To dismiss the dialog
                        progress.dismiss();
                    }

                    @Override
                    public void onProgressUpdate(int value) {
                    }

                    @Override
                    public void onFailure(Exception error) {
                        if (getActivity() == null) {
                            return;
                        }
                        Log.e("search", "failure", error);
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.check_connectivity), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                }).execute(et_query.getText().toString());
            }
        });

        return rootView;
    }

}
