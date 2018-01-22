package pt.up.fe.beta.labtablet.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.api.ChangelogManager;
import pt.up.fe.beta.labtablet.models.ChangelogItem;
import pt.up.fe.beta.labtablet.utils.Utils;

/**
 * This asynctask loads a bitmap to create mostly miniatures of the images
 * It also takes into account memory limitations (scaling down the image to save some bits)
 * Normal runtime of this app should not exceed 20Mb of allocated memory
 */
public class AsyncImageLoader extends AsyncTask<Object, Void, Bitmap> {

    private final ImageView imv;
    private final String path;
    private final Context mContext;
    private final boolean compression;

    public AsyncImageLoader(ImageView imv, Context context, boolean imgCompressionEnabled) {
        this.imv = imv;
        this.path = imv.getTag().toString();
        this.mContext = context;
        this.compression = imgCompressionEnabled;
    }

    @Override
    protected Bitmap doInBackground(Object... params) {

        if (path.equals(""))
            return null;

        File file = new File(path);
        if (!file.exists())
            return null;

        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, o);

            //The new size we want to scale to
            final int REQUIRED_SIZE = 70;

            //Optimal scale value - Should be a power of 2
            int scale = 1;
            int scaleFactor = 2;

            if (!compression) {
                scaleFactor = 16;
            }

            while (o.outWidth / scale / scaleFactor >= REQUIRED_SIZE
                    && o.outHeight / scale / scaleFactor >= REQUIRED_SIZE)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
        } catch (FileNotFoundException e) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("Image Loader" + "File was not found: " + e.toString());
            item.setTitle(mContext.getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, mContext);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (!imv.getTag().toString().equals(path)) {
            /**
             * Reject take off. This imv was from another class or
             * was already recycled, so do nothing.
             */
            return;
        }

        //imv != null?
        if (result != null) {
            imv.setVisibility(View.VISIBLE);
            imv.setImageBitmap(result);
        }
    }

}
