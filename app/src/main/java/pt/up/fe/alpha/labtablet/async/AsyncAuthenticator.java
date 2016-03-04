package pt.up.fe.alpha.labtablet.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import pt.up.fe.alpha.labtablet.api.DendroAPI;

/**
 * Authenticates the available credentials with the repository
 */
public class AsyncAuthenticator extends AsyncTask<Object, Integer, String> {

    //input, remove, output
    private final AsyncTaskHandler<String> mHandler;
    private Exception error;


    public AsyncAuthenticator(AsyncTaskHandler<String> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mHandler.onProgressUpdate(values[0]);
    }


    @Override
    protected void onCancelled() {
        Log.e("", "cancelled");
        super.onCancelled();
    }

    @Override
    protected String doInBackground(Object... params) {

        if (!(params[0] instanceof Context)) {
            error = new Exception("Type mismatch, (expected Context)");
            return null;
        }

        Context mContext;
        String cookie;

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

