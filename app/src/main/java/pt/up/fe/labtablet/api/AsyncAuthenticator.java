package pt.up.fe.labtablet.api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import pt.up.fe.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.labtablet.utils.FileMgr;

public class AsyncAuthenticator extends AsyncTask<Object, Integer, String> {
    //input, remove, output
    private AsyncTaskHandler<String> mHandler;
    private Exception error;

    private Context mContext;
    private String cookie;

    public AsyncAuthenticator(AsyncTaskHandler<String> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mHandler.onProgressUpdate(values[0]);
    }


    @Override
    protected void onCancelled() {
        Log.e("","cancelled");
        super.onCancelled();
    }

    @Override
    protected String doInBackground(Object... params) {

        if ( !(params[0] instanceof Context)) {
            error = new Exception("Type mismatch, (expected Context)");
            return null;
        }

        mContext = (Context) params[0];

        try {
            cookie = DendroAPI.authenticate(mContext);
            return cookie;
        } catch (Exception e) {
            error = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(null);
        }
    }
}

