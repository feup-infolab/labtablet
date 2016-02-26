package pt.up.fe.labtablet.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.adapters.DataListAdapter;
import pt.up.fe.labtablet.adapters.MetadataListAdapter;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.OnItemClickListener;

public class FavoriteViewFragment extends Fragment implements OnItemClickListener {


    private RecyclerView itemList;

    public FavoriteViewFragment() {
        // Required empty public constructor
    }

    public static FavoriteViewFragment newInstance(String tag, Object item) {
        FavoriteViewFragment f = new FavoriteViewFragment();
        Bundle args = new Bundle();
        args.putString("items", new Gson().toJson(item));
        args.putString("current_tag", tag);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_generic_list, container, false);

        Bundle args = getArguments();
        if (!args.containsKey("current_tag")) {
            Log.e("FView", "tag was not provided");
            return rootView;
        }

        itemList = (RecyclerView) rootView.findViewById(R.id.list);

        String mCurrentTag = args.getString("current_tag");
        assert mCurrentTag != null;
        switch (mCurrentTag){

            case "data":
                ArrayList<DataItem> dataItems = new Gson().fromJson(args.getString("items"), new TypeToken<ArrayList<DataItem>>(){}.getType());
                if (dataItems.isEmpty()) {
                    rootView.findViewById(R.id.list_state).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.list).setVisibility(View.INVISIBLE);
                    break;
                }
                rootView.findViewById(R.id.list_state).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);
                loadDataView(dataItems);
                break;

            case "metadata":
                ArrayList<Descriptor> metadataItems = new Gson().fromJson(args.getString("items"), new TypeToken<ArrayList<Descriptor>>(){}.getType());
                if (metadataItems.isEmpty()) {
                    rootView.findViewById(R.id.list_state).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.list).setVisibility(View.INVISIBLE);
                    break;
                }
                rootView.findViewById(R.id.list_state).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);
                loadMetaDataView(metadataItems);
                break;
            default:
                Toast.makeText(getActivity(), "NO VIEW ATTACHED", Toast.LENGTH_SHORT).show();
                break;
        }
        itemList.setItemAnimator(new DefaultItemAnimator());
        itemList.animate();

        return rootView;
    }

    private void loadDataView(ArrayList<DataItem> items) {
        DataListAdapter mAdapter = new DataListAdapter(items, this, getActivity());
        itemList.setAdapter(mAdapter);
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void loadMetaDataView(ArrayList<Descriptor> items) {
        MetadataListAdapter mAdapter = new MetadataListAdapter(items, this, getActivity());
        itemList.setAdapter(mAdapter);
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
