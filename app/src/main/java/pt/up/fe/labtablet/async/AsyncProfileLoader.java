package pt.up.fe.labtablet.async;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.Utils;

public class AsyncProfileLoader extends AsyncTask<File, Integer, ArrayList<Descriptor>> {
    //input, remove, output
    private AsyncTaskHandler<ArrayList<Descriptor>> mHandler;
    private Exception error;

    public AsyncProfileLoader(AsyncTaskHandler<ArrayList<Descriptor>> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected ArrayList<Descriptor> doInBackground(File... params) {

        publishProgress(1);
        ArrayList<Descriptor> mResult = new ArrayList<Descriptor>();

        try {
            File inputFile = params[0];
            FileInputStream input = new FileInputStream(inputFile);
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
