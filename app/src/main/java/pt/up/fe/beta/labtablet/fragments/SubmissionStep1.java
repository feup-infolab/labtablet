package pt.up.fe.beta.labtablet.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.api.SubmissionStepHandler;
import pt.up.fe.beta.labtablet.async.AsyncGenericChecker;
import pt.up.fe.beta.labtablet.async.AsyncTaskHandler;

/**
 * Summarizes the device conditions (if there is enough battery, internet connection, and if the records
 * follow the intended validations)
 */
public class SubmissionStep1 extends Fragment {

    private TextView wifiState;
    private Drawable good;
    private Drawable bad;
    private Drawable meh;
    private ConnectionChangeReceiver mReceiver;
    private static SubmissionStepHandler mHandler;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SubmissionStep1 newInstance(String favoriteName, SubmissionStepHandler handler) {
        SubmissionStep1 fragment = new SubmissionStep1();
        Bundle args = new Bundle();
        args.putString("favorite_name", favoriteName);
        fragment.setArguments(args);
        mHandler = handler;
        return fragment;
    }

    public SubmissionStep1() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_submission_step1, container, false);

        TextView title = (TextView) rootView.findViewById(R.id.step1_title);
        TextView batteryLevel = (TextView) rootView.findViewById(R.id.step1_battery_tv);
        wifiState = (TextView) rootView.findViewById(R.id.step1_wifi_tv);

        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            title.setText(getArguments().getString("favorite_name"));
        } else {
            title.setText(savedInstanceState.getString("favorite_name"));
        }

        mReceiver = new ConnectionChangeReceiver();

        float batteryValue = getBatteryLevel();
        good = getResources().getDrawable(R.drawable.ic_check);
        meh = getResources().getDrawable(R.drawable.ic_warning);
        bad = getResources().getDrawable(R.drawable.ab_cross);

        if (batteryValue < 20f) {
            batteryLevel.setCompoundDrawablesWithIntrinsicBounds(null, null, bad, null);
        } else if (batteryValue >= 20f && batteryValue < 50f){
            batteryLevel.setCompoundDrawablesWithIntrinsicBounds(null, null, meh, null);
        } else {
            batteryLevel.setCompoundDrawablesWithIntrinsicBounds(null, null, good, null);
        }

        new AsyncGenericChecker(new AsyncTaskHandler<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (result != 0) {
                    //oh no you didn't just used generic descriptors
                    ((TextView) rootView.findViewById(R.id.step1_metadata_state))
                            .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_warning, 0);
                }
            }

            @Override
            public void onFailure(Exception error) {

            }

            @Override
            public void onProgressUpdate(int value) {

            }
        }).execute(getActivity(), getArguments().getString("favorite_name"));
        return rootView;
    }

    private float getBatteryLevel() {
        Intent batteryIntent = getActivity().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryIntent == null ? 0 : batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent == null ? 0 : batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

    private class ConnectionChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ( activeNetInfo != null ) {
                wifiState.setCompoundDrawablesWithIntrinsicBounds(null, null, meh, null);
                Toast.makeText(context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            }
            if( mobNetInfo != null ) {
                wifiState.setCompoundDrawablesWithIntrinsicBounds(null, null, good, null);
                Toast.makeText( context, "Mobile Network Type : " + mobNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
            }
            if( activeNetInfo == null && mobNetInfo == null) {
                Toast.makeText( context, "No internet for you", Toast.LENGTH_SHORT ).show();
                wifiState.setCompoundDrawablesWithIntrinsicBounds(null, null, bad, null);
            }
        }
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        getActivity().registerReceiver(mReceiver, intentFilter);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("favorite_name", getArguments().getString("favorite_name"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.submission_step1, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_dendro_instructions_confirm) {
            mHandler.nextStep(1);
        }else if (item.getItemId() == 16908332 /*backButton*/){
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }
}
