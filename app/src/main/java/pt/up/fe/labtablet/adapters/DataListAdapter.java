package pt.up.fe.labtablet.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.FileMgr;

/**
 * Created by ricardo on 9/11/14.
 */
public class DataListAdapter extends ArrayAdapter<Descriptor> {

    private final Activity context;
    private final String favoriteName;
    private final ArrayList<Descriptor> items;


    public DataListAdapter(Activity context, ArrayList<Descriptor> srcItems, String favoriteName) {
        super(context, R.layout.item_data_list, srcItems);
        this.context = context;
        this.items = srcItems;
        this.favoriteName = favoriteName;
    }

    @Override
    public void notifyDataSetChanged() {
        items.clear();
        items.addAll(FileMgr.getDescriptors(favoriteName, context));
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_data_list, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mDescriptorName = (TextView) rowView.findViewById(R.id.metadata_item_title);
            viewHolder.mDescriptorType = (ImageView) rowView.findViewById(R.id.metadata_item_type);
            viewHolder.mDescriptorDate = (TextView) rowView.findViewById(R.id.metadata_item_date);
            viewHolder.mDescriptorValue = (TextView) rowView.findViewById(R.id.metadata_item_value);
            viewHolder.mDescriptorSize = (TextView) rowView.findViewById(R.id.metadata_item_size);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final Descriptor item = items.get(position);

        holder.mDescriptorDate.setText(item.getDateModified());
        holder.mDescriptorValue.setText(item.getValue());
        holder.mDescriptorName.setText(item.getName());
        holder.mDescriptorType.setTag(item.getFilePath());
        holder.mDescriptorSize.setText(item.getSize());

        holder.mDescriptorType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(items.get(position).getFilePath());
                String mime = FileMgr.getMimeType(file.getAbsolutePath());

                if (mime == null) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.no_apps_available),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), mime);
                context.startActivity(intent);
            }
        });

        new LoadImage(holder.mDescriptorType).execute();

        Animation animation = AnimationUtils.makeInAnimation(context, false);
        rowView.startAnimation(animation);

        return rowView;
    }

    static class ViewHolder {
        public TextView mDescriptorName;
        public TextView mDescriptorValue;
        public ImageView mDescriptorType;
        public TextView mDescriptorDate;
        public TextView mDescriptorSize;
    }

    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private ImageView imv;
        private String path;

        public LoadImage(ImageView imv) {
            this.imv = imv;
            this.path = imv.getTag().toString();
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

                //Find the correct scale value. It should be the power of 2.
                int scale = 1;
                while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;

                //Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
            } catch (FileNotFoundException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (!imv.getTag().toString().equals(path)) {
               /* The path is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
                return;
            }

            if (result != null && imv != null) {
                imv.setVisibility(View.VISIBLE);
                imv.setImageBitmap(result);
            } else {
                imv.setImageResource(R.drawable.ic_metadata);
            }
        }

    }
}
