package pt.up.fe.alpha.labtablet.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.api.DendroAPI;
import pt.up.fe.alpha.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.alpha.labtablet.models.DataItem;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroMetadataRecord;
import pt.up.fe.alpha.labtablet.models.Descriptor;
import pt.up.fe.alpha.labtablet.models.FavoriteItem;
import pt.up.fe.alpha.labtablet.models.Form;
import pt.up.fe.alpha.labtablet.models.FormInstance;
import pt.up.fe.alpha.labtablet.models.ProgressUpdateItem;
import pt.up.fe.alpha.labtablet.utils.Utils;
import pt.up.fe.alpha.labtablet.utils.Zipper;

import static pt.up.fe.alpha.labtablet.utils.CSVHandler.generateCSV;

public class AsyncUploader extends AsyncTask<Object, ProgressUpdateItem, Void> {
    //input, remove, output
    private final AsyncCustomTaskHandler<Void> mHandler;
    private Exception error;

    public AsyncUploader(AsyncCustomTaskHandler<Void> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected void onProgressUpdate(ProgressUpdateItem... values) {
        mHandler.onProgressUpdate(values[0]);
    }


    @Override
    protected void onCancelled() {
        Log.e("", "cancelled");
        super.onCancelled();
    }

    @Override
    protected Void doInBackground(Object... params) {
        String destUri;
        String cookie;
        Context mContext;
        String favoriteName;

        if (params[0] instanceof String
                && params[2] instanceof String
                && params[3] instanceof Context) {

            favoriteName = (String) params[0];
            destUri = (String) params[2];
            mContext = (Context) params[3];

            if (destUri.equals("")) {
                error = new Exception("Target Uri not defined!");
                return null;
            }

        } else {
            error = new Exception("Type mismatch");
            return null;
        }

        URL url;
        HttpURLConnection conn;

                //HttpClient httpclient;
        //HttpPost httppost;

        destUri = destUri.replace(" ", "%20");

        //upload files (if any)
        String from = Environment.getExternalStorageDirectory() + "/" + mContext.getResources().getString(R.string.app_name) + "/" + favoriteName;

        //AUTHENTICATE USER
        publishProgress(new ProgressUpdateItem(10, mContext.getResources().getString(R.string.upload_progress_authenticating)));
        try {
            cookie = DendroAPI.authenticate(mContext);
        } catch (Exception e) {
            error = e;
            return null;
        }

        // ***** FAVORITE ITEM IS HERE ******

        FavoriteItem item = FavoriteMgr.getFavorite(mContext, favoriteName);

        // ***********************************

        //Generate forms and forms' csv file
        if (item.getLinkedForms().size() > 0) {
            String progress = mContext.getString(R.string.upload_generating_forms);

            ArrayList<FormInstance> linkedForms = item.getLinkedForms();

            publishProgress(new ProgressUpdateItem(15, progress + "0/" + linkedForms.size()));

            try {
                generateCSV(mContext, linkedForms, favoriteName);
            } catch (IOException e) {
                error = e;
                return null;
            }
        }


        //if there are any files to upload, zip them
        publishProgress(new ProgressUpdateItem(20, mContext.getResources().getString(R.string.upload_progress_creating_package)));
        if (new File(from).listFiles().length > 0) {
            Zipper mZipper = new Zipper();
            String to = Environment.getExternalStorageDirectory() + "/" + favoriteName + ".zip";
            Log.i("ZIP_FROM", from);
            Log.i("ZIP_TO", to);
            Boolean result = mZipper.zipFileAtPath(from, to, mContext);

            if (!result) {
                Log.e("ZIP", "Failed to create zip file");
                error = new Exception("Failed to create zip file");
                return null;
            }

            publishProgress(new ProgressUpdateItem(25, mContext.getString(R.string.upload_progress_uploading)));

            //httpclient = new DefaultHttpClient();
            //httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);


            /*httppost = new HttpPost(destUri + "?restore");
            Log.d("[AsyncUploader] URI", destUri.replace(" ", "%20") + "?restore");
            httppost.setHeader("Cookie", "connect.sid=" + cookie);*/
            File file = new File(to);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            FileBody fileBody = new FileBody(file);
            builder.addPart("files[]", fileBody);
            builder.addTextBody("filename", favoriteName + ".zip");

            Log.d("[AsyncUploader]Path", file.getAbsolutePath());

            long totalSize = file.length();


            /*LabTabletUploadEntity mEntity = new LabTabletUploadEntity(builder.build(), totalSize);

            httppost.setEntity(mEntity);
            Log.d("[AsyncUploader]POST", "" + httppost.getRequestLine()); */

            try {
                url = new URL(destUri.replace(" ", "%20") + "?restore");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                Log.d("[AsyncUploader] URI", destUri.replace(" ", "%20") + "?restore");
                conn.setRequestProperty("Cookie", cookie);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(builder.build().toString().getBytes());
                os.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String output;
                StringBuilder resp = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    resp.append(output);
                    resp.append('\r');
                }
                String mes = resp.toString();
                conn.disconnect();

                /*HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity resEntity = httpResponse.getEntity();*/
                DendroResponse response = new Gson().fromJson(mes, DendroResponse.class);

                if (response.result.equals(Utils.DENDRO_RESPONSE_ERROR)) {
                    error = new Exception(response.result +
                            ": " + response.message);
                    return null;
                }

                if (!file.delete()) {
                    Toast.makeText(mContext,
                            mContext.getResources().getString(R.string.upload_progress_deleting_temp_files),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("POST", e.getMessage());
                error = e;
                return null;
            }
        }

        //export metadata
        publishProgress(new ProgressUpdateItem(
                50, mContext.getString(R.string.upload_progress_creating_metadata_package)));

        ArrayList<Descriptor> descriptors = item.getMetadataItems();
        ArrayList<DendroMetadataRecord> metadataRecords = new ArrayList<>();

        if (descriptors.size() == 0) {
            error = new Exception("No metadata found!");
            return null;
        }

        for (Descriptor descriptor : descriptors) {
            if (descriptor.hasFile()) {
                metadataRecords.add(new DendroMetadataRecord(
                        descriptor.getDescriptor(),
                        destUri + File.separator + "meta"
                                + File.separator + descriptor.getValue()
                ));
            } else {
                metadataRecords.add(new DendroMetadataRecord(descriptor.getDescriptor(), descriptor.getValue()));
            }
        }

        //Post folder-level metadata to the repository
        publishProgress(new ProgressUpdateItem(
                70, mContext.getString(R.string.upload_progress_sending_metadata)));

        try {
            url = new URL(destUri + "?update_metadata");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Cookie", cookie);
            conn.setDoOutput(true);

            /*httpclient = new DefaultHttpClient();
            httppost = new HttpPost(destUri + "?update_metadata");
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("Cookie", "connect.sid=" + cookie);*/

            String g = new Gson().toJson(metadataRecords, Utils.ARRAY_DENDRO_METADATA_RECORD);

            OutputStream os = conn.getOutputStream();
            os.write(g.getBytes());
            os.flush();

            //StringEntity se = new StringEntity(new Gson().toJson(metadataRecords, Utils.ARRAY_DENDRO_METADATA_RECORD), HTTP.UTF_8);
            Log.e("metadata", new Gson().toJson(metadataRecords, Utils.ARRAY_DENDRO_METADATA_RECORD));
            //httppost.setEntity(se);

            //HttpResponse resp = httpclient.execute(httppost);
            //HttpEntity ent = resp.getEntity();

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            StringBuilder response = new StringBuilder();
            while ((output = br.readLine()) != null) {
                response.append(output);
                response.append('\r');
            }
            String mes = response.toString();
            conn.disconnect();

            DendroResponse metadataResponse = new Gson().fromJson(mes, DendroResponse.class);
            if (metadataResponse.result.equals(Utils.DENDRO_RESPONSE_ERROR) ||
                    metadataResponse.result.equals(Utils.DENDRO_RESPONSE_ERROR_2)) {
                error = new Exception(metadataResponse.result + ": " + metadataResponse.message);
                return null;
            }

        } catch (Exception e) {
            error = e;
            return null;
        }

        publishProgress(new ProgressUpdateItem(
                90, mContext.getString(R.string.upload_progress_describing_imported_data)));


        //check if there are any file-dependant descriptions
        //if so, upload them
        ArrayList<DataItem> dataDescriptionItems =
                item.getDataItems();

        if (dataDescriptionItems == null) {
            return null;
        }

        for (DataItem dataItem : dataDescriptionItems) {

            metadataRecords = new ArrayList<>();
            for (Descriptor desc : dataItem.getFileLevelMetadata()) {
                metadataRecords.add(
                        new DendroMetadataRecord(desc.getDescriptor(), desc.getValue())
                );
            }

            try {
                String destPath = destUri + File.separator + dataItem.getResourceName() + "?update_metadata";
                String dest = destPath.replace(" ", "%20");
                url = new URL(dest);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setRequestProperty("Cookie", cookie);
                conn.setDoOutput(true);

                /*httpclient = new DefaultHttpClient();
                String destPath = destUri + File.separator + dataItem.getResourceName() + "?update_metadata";
                httppost = new HttpPost(destPath.replace(" ", "%20"));
                httppost.setHeader("Accept", "application/json");
                httppost.setHeader("Content-Type", "application/json");
                httppost.setHeader("Cookie", "connect.sid=" + cookie);*/

                String g = new Gson().toJson(metadataRecords, Utils.ARRAY_DENDRO_DESCRIPTORS);

                //StringEntity se = new StringEntity(new Gson().toJson(metadataRecords, Utils.ARRAY_DENDRO_DESCRIPTORS), HTTP.UTF_8);

                //httppost.setEntity(se);

                /*HttpResponse resp = httpclient.execute(httppost);
                HttpEntity ent = resp.getEntity();*/

                OutputStream os = conn.getOutputStream();
                os.write(g.getBytes());
                os.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String output;
                StringBuilder response = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    response.append(output);
                    response.append('\r');
                }
                String mes = response.toString();
                conn.disconnect();

                DendroResponse metadataResponse = new Gson().fromJson(mes, DendroResponse.class);
                if (metadataResponse.result.equals(Utils.DENDRO_RESPONSE_ERROR) ||
                        metadataResponse.result.equals(Utils.DENDRO_RESPONSE_ERROR_2)) {
                    error = new Exception(metadataResponse.result + ": " + metadataResponse.message);
                    return null;
                }
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        publishProgress(new ProgressUpdateItem(100, mContext.getString(R.string.finished)));
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

    public class DendroResponse {
        public String result;
        public String message;
    }

    /**
     * class to retrieve the upload progress for large files
     * had to implement native methods..
     */
    class LabTabletUploadEntity implements HttpEntity {

        private final HttpEntity mEntity;
        private final long totalSize;

        public LabTabletUploadEntity(HttpEntity mEntity, long size) {
            this.totalSize = size;
            this.mEntity = mEntity;
        }

        @Override
        public void consumeContent() throws IOException {
            mEntity.consumeContent();
        }
        @Override
        public InputStream getContent() throws IOException,
                IllegalStateException {
            return mEntity.getContent();
        }
        @Override
        public Header getContentEncoding() {
            return mEntity.getContentEncoding();
        }
        @Override
        public long getContentLength() {
            return mEntity.getContentLength();
        }
        @Override
        public Header getContentType() {
            return mEntity.getContentType();
        }
        @Override
        public boolean isChunked() {
            return mEntity.isChunked();
        }
        @Override
        public boolean isRepeatable() {
            return mEntity.isRepeatable();
        }
        @Override
        public boolean isStreaming() {
            return mEntity.isStreaming();
        }

        @Override
        public void writeTo(OutputStream outstream) throws IOException {

            class ProxyOutputStream extends FilterOutputStream {
                public ProxyOutputStream(OutputStream proxy) { super(proxy); }
                public void write(int idx) throws IOException { out.write(idx); }
                public void write(byte[] bts) throws IOException { out.write(bts); }
                public void write(byte[] bts, int st, int end) throws IOException { out.write(bts, st, end); }
                public void flush() throws IOException { out.flush(); }
                public void close() throws IOException { out.close(); }
            }

            class ProgressiveOutputStream extends ProxyOutputStream {
                long totalSent;

                public ProgressiveOutputStream(OutputStream proxy) {
                    super(proxy);
                    totalSent = 0;
                }

                public void write(byte[] bts, int st, int end) throws IOException {
                    totalSent += end;
                    ProgressUpdateItem progress = new ProgressUpdateItem(
                            ((int) ((totalSent / (float) totalSize) * 100)),
                            "Uploading data package"
                    );
                    publishProgress(progress);
                    out.write(bts, st, end);
                }
            }

            mEntity.writeTo(new ProgressiveOutputStream(outstream));
        }
    }
}