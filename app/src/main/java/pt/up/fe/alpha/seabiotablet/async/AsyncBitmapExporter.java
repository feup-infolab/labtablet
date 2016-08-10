package pt.up.fe.alpha.seabiotablet.async;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class AsyncBitmapExporter extends AsyncTask<Object, Integer, Void> {
    //input, remove, output

    private final AsyncTaskHandler<Void> mHandler;
    private Exception error;

    public AsyncBitmapExporter(AsyncTaskHandler<Void> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected Void doInBackground(Object... params) {

        if (!(params[0] instanceof String)
                || !(params[1] instanceof Bitmap)) {
            Log.e("BITMAP", "Wrong instances!");
            return null;
        }

        String path = (String) params[0];
        Bitmap bitmap = (Bitmap) params[1];
        File file = new File(path);
        try {
            if (!file.exists()) {
                Log.d("Bitmap Export", "" + file.createNewFile());
            }
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);
            ostream.close();
        } catch (Exception e) {
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(null);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}