package pt.up.fe.labtablet.async;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.Utils;

public class AsyncProfileLoader extends AsyncTask<InputStream, Integer, ArrayList<Descriptor>> {
    //input, remove, output
    private final AsyncTaskHandler<ArrayList<Descriptor>> mHandler;
    private Exception error;

    public AsyncProfileLoader(AsyncTaskHandler<ArrayList<Descriptor>> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected ArrayList<Descriptor> doInBackground(InputStream... params) {

        publishProgress(1);
        ArrayList<Descriptor> mResult = new ArrayList<>();

        try {
            InputStream input = params[0];
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String resource = sb.toString();
            mResult = new Gson().fromJson(resource, Utils.ARRAY_DESCRIPTORS);

        } catch (Exception e) {
            error = e;
        }
        return mResult;
    }

    @Override
    protected void onPostExecute(ArrayList<Descriptor> result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(result);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mHandler.onProgressUpdate(values[0]);
    }
}
