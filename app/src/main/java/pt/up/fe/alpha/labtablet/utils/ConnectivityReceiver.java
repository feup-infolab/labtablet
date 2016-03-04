package pt.up.fe.alpha.labtablet.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.activities.FieldModeActivity;
import pt.up.fe.alpha.labtablet.voiceManager.GoogleVoiceRecognition;
import pt.up.fe.alpha.labtablet.voiceManager.OfflineVoiceRecognition;


public class ConnectivityReceiver extends BroadcastReceiver {

    public String prev_conn_state;
    private FieldModeActivity fieldMode;

    public ConnectivityReceiver(FieldModeActivity fieldMode){
        this.fieldMode = fieldMode;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(prev_conn_state == null) return;
        final String action = intent.getAction();
        Log.e("action", action);

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean state = isNetworkOnline(context);


            if (state) {
                if (prev_conn_state.equals("OFFLINE")) {
                    //do stuff
                    Log.i("connection", "online");
                    if(fieldMode.getSw_handsFree().isChecked()) {
                        fieldMode.shutdownRecognizer();

                        fieldMode.setGoogleRecognizer(new GoogleVoiceRecognition(fieldMode));
                        fieldMode.getGoogleRecognizer().setupGoogleRecognizer();
                        fieldMode.getLocationListener().setGoogleRecognizer(fieldMode.getGoogleRecognizer());

                        fieldMode.getGoogleRecognizer().audioUnmute(); //for ttsVoice

                        fieldMode.turnOnGoogleRec();
                    }

                    Toast.makeText(fieldMode, fieldMode.getResources().getString(R.string.tts_good_conn), Toast.LENGTH_LONG).show();

                }
                prev_conn_state = "ONLINE";

            } else {
                if (prev_conn_state.equals("ONLINE")) {
                    //do stuff
                    Log.i("connection", "offline");
                    //fieldMode.getSw_handsFree().performClick();
                    if(fieldMode.getSw_handsFree().isChecked()) {
                        fieldMode.shutdownRecognizer();
                        fieldMode.setOfflineRecognizer(new OfflineVoiceRecognition(fieldMode));
                        fieldMode.turnOnSphinxRec();
                        Toast.makeText(fieldMode, fieldMode.getResources().getString(R.string.tts_lost_conn), Toast.LENGTH_LONG);
                    }

                }
                prev_conn_state = "OFFLINE";



            }

        }
    }


    public boolean isNetworkOnline(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;

    }

}
