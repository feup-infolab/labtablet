package pt.up.fe.alpha.labtablet.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.api.DendroAPI;
import pt.up.fe.alpha.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.alpha.labtablet.models.DataItem;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroMetadataRecord;
import pt.up.fe.alpha.labtablet.models.Descriptor;
import pt.up.fe.alpha.labtablet.models.FavoriteItem;
import pt.up.fe.alpha.labtablet.models.Form;
import pt.up.fe.alpha.labtablet.models.FormInstance;
import pt.up.fe.alpha.labtablet.models.ProgressUpdateItem;
import pt.up.fe.alpha.labtablet.utils.Utils;
import pt.up.fe.alpha.labtablet.utils.Zipper;

import static com.itextpdf.text.Utilities.readFileToString;
import static pt.up.fe.alpha.labtablet.utils.CSVHandler.generateCSV;

public class AsyncUploader extends AsyncTask<Object, ProgressUpdateItem, Void> {
    //input, remove, output
    private final AsyncCustomTaskHandler<Void> mHandler;
    private Exception error;
    AsyncItemMetadataFetcher mItemMetadataFetcher;
    ArrayList<DataItem> dataDescriptionItems;
    String destUri;
    JsonArray childrenURIs;
    String cookie;
    Context mContext;
    String baseUrl;
    String baseResourceUri;

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
        /*
        String destUri;
        String cookie;
        Context mContext;*/
        String favoriteName;

        if (params[0] instanceof String
                && params[2] instanceof String
                && params[3] instanceof Context) {

            favoriteName = (String) params[0];
            destUri = (String) params[2];
            mContext = (Context) params[3];

            SharedPreferences settings = mContext.getSharedPreferences(mContext.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
            if (!settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
                error = new Exception("Dendro configurations were not found");
                return null;
            }

            DendroConfiguration conf = new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);
            baseUrl = conf.getAddress();
            baseResourceUri = destUri.replaceFirst(baseUrl, "");

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
        String fileF = Environment.getExternalStorageDirectory() + "/" + mContext.getResources().getString(R.string.app_name) + "/" + favoriteName;
        //TODO NELSON
        //String from = Environment.getExternalStorageDirectory() + "/" + mContext.getResources().getString(R.string.app_name) + "/data";//"/" + favoriteName;
        String dataFolder = Environment.getExternalStorageDirectory() + "/" + mContext.getResources().getString(R.string.app_name) + "/data";//"/" + favoriteName;


        boolean success = (new File(dataFolder)).mkdirs();
        if (!success) {
            return null;
        }

        copyFileOrDirectory(fileF, dataFolder);

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
        if (new File(dataFolder).listFiles().length > 0) {
            Zipper mZipper = new Zipper();
            String to = Environment.getExternalStorageDirectory() + "/" + favoriteName + ".zip";
            //TODO NELSON
            String from = dataFolder + "/" + favoriteName;
            //TODO NELSON
            Log.i("ZIP_FROM", from);
            Log.i("ZIP_TO", to);
            //TODO NELSON
            Boolean result = mZipper.zipFileAtPath(from, to, mContext);

            if (!result) {
                Log.e("ZIP", "Failed to create zip file");
                error = new Exception("Failed to create zip file");
                return null;
            }

            File dst = new File(dataFolder);

            if (dst.exists()) {
                String deleteCmd = "rm -r " + dataFolder;
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec(deleteCmd);
                } catch (IOException e) { }
            }

            /*String[] children = dst.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dst, children[i]).delete();
            }*/

            publishProgress(new ProgressUpdateItem(25, mContext.getString(R.string.upload_progress_uploading)));

            //httpclient = new DefaultHttpClient();
            //httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);


            /*httppost = new HttpPost(destUri + "?restore");
            Log.d("[AsyncUploader] URI", destUri.replace(" ", "%20") + "?restore");
            httppost.setHeader("Cookie", "connect.sid=" + cookie);*/
            String boundry = "--------------------------122869462475904859705487";
            File file = new File(to);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setBoundary(boundry);
            String fileContent, fileMD5 = "";
            try {
                fileContent = readFileToString(file);
                fileMD5 = Utils.getMD5EncryptedString(fileContent);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileBody fileBody = new FileBody(file);
            //builder.addBinaryBody("file", file);
            builder.addPart(favoriteName + ".zip", fileBody);
            //builder.addTextBody("filename", favoriteName + ".zip");
            //builder.addTextBody("md5_checksum", fileMD5);

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
                conn.addRequestProperty("md5_checksum", fileMD5);

                /*conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;");*/

                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("accept-encoding", "gzip, deflate");
                conn.setRequestProperty("connection", "close");
                conn.setRequestProperty("content-type",
                        "multipart/form-data; boundary=" + boundry);

                //OutputStream os = conn.getOutputStream();
                OutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.write(builder.build().toString().getBytes());
                builder.build().writeTo(os);
                //os.write(builder.build().toString().getBytes());
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

        //TODO LS HERE
        //TODO NELSON here we have to ls into meta folder in dendro then try to map the names of the children and their uris, then set the uris in the decriptor.value fields bellow
        String metaFolderUri;
        /*try {
            metaFolderInfo = getChildUriByName(destUri, "meta");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        metaFolderUri = getChildUriByName(destUri, "meta");
        //TODO NELSON BUILD get children info
        //TODO after that build function that given the descriptor.getValue() returns the uri
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
        //TODO this is commented for debug purposes
        /*ArrayList<DataItem> dataDescriptionItems =
                item.getDataItems();*/

        /*
        dataDescriptionItems = item.getDataItems();
        if (dataDescriptionItems == null) {
            return null;
        }*/

        //TODO this is going into the postExecute method
        /*for (DataItem dataItem : dataDescriptionItems) {

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

                String g = new Gson().toJson(metadataRecords, Utils.ARRAY_DENDRO_DESCRIPTORS);
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
        return null;*/
        publishProgress(new ProgressUpdateItem(100, mContext.getString(R.string.finished)));
        return  null;
    }

    private void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(null);
        }

        //getChildrenUris();
        //mItemMetadataFetcher.execute(destUri);
        //updateChildrenMetadata(dataDescriptionItems);
        //mItemMetadataFetcher.execute(destUri);

        /*
        getChildrenUris();
        mItemMetadataFetcher.execute(baseResourceUri, mContext);
        */

        publishProgress(new ProgressUpdateItem(100, mContext.getString(R.string.finished)));
    }


    public String getChildUriByName(String destUri, String childNameToLook) {
        URL url;
        HttpURLConnection conn;
        String result = "";
        String childUri = "";

        try {
            url = new URL(destUri + "?ls&title=" + childNameToLook);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("Accept","application/json");
            conn.setDoInput(true);
                /*
            request.setURI(new URI(requestString));
            request.setHeader("Accept", "application/json");
            request.setHeader("Cookie", "connect.sid=" + cookie);

            response = client.execute(request);
            */

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            result = response.toString();
            JsonParser parser = new JsonParser();
            try{
                JsonArray objArray = parser.parse(result).getAsJsonArray();
                Iterator<JsonElement> it = objArray.iterator();
                JsonElement elem = it.next();
                JsonObject obj = elem.getAsJsonObject();
                childUri = obj.get("uri").getAsString();
            }
            catch (Exception e)
            {
                JsonObject obj = parser.parse(result).getAsJsonObject();
                childUri = obj.get("uri").getAsString();
            }

        /*
        dendroFolderItems = new Gson().fromJson(
                obj,
                Utils.ARRAY_DIRECTORY_LISTING);

        return dendroFolderItems;*/

            return childUri;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    };

    public void getChildrenUris()
    {
        mItemMetadataFetcher = new AsyncItemMetadataFetcher(new AsyncTaskHandler<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject reader = new JSONObject(result);
                    String descriptors = reader.getJSONArray("descriptors").toString();
                    JsonParser parser = new JsonParser();
                    JsonArray descriptorsArray = parser.parse(descriptors).getAsJsonArray();
                    for(Iterator<JsonElement> it = descriptorsArray.iterator(); it.hasNext(); )
                    {
                        JsonElement elem = it.next();
                        JsonObject obj = elem.getAsJsonObject();
                        String currentValue = "";
                        String currentDescriptorUri = obj.get("uri").getAsString();
                        //TODO change this into a constant
                        if(currentDescriptorUri.equals("http://www.semanticdesktop.org/ontologies/2007/01/19/nie#hasLogicalPart"))
                        {
                            //currentValue = obj.get("value").getAsString();
                            try {
                                childrenURIs = obj.get("value").getAsJsonArray();
                                for(JsonElement childUri : childrenURIs)
                                {
                                    getChildInfo();
                                    mItemMetadataFetcher.execute(childUri.toString(), mContext);
                                }
                            }
                            catch (Exception e)
                            {
                                currentValue = obj.get("value").getAsString();
                                childrenURIs = new JsonArray();
                                childrenURIs.add(currentValue);
                                getChildInfo();
                                mItemMetadataFetcher.execute(currentValue, mContext);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getChildrenUris", error.getMessage());
            }

            @Override
            public void onProgressUpdate(int value) {

            }
        });
    }

    public void getChildInfo()
    {
        mItemMetadataFetcher = new AsyncItemMetadataFetcher(new AsyncTaskHandler<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    //JsonArray mapperTitleAndUri = new JsonArray();
                    JsonObject mapperObject = new JsonObject();
                    JSONObject reader = new JSONObject(result);
                    String currentChildUri = reader.get("uri").toString();
                    String descriptors = reader.getJSONArray("descriptors").toString();
                    JsonParser parser = new JsonParser();
                    JsonArray descriptorsArray = parser.parse(descriptors).getAsJsonArray();
                    /*for(Iterator<JsonElement> it = descriptorsArray.iterator(); it.hasNext(); )
                    {
                        JsonElement elem = it.next();
                        JsonObject obj = elem.getAsJsonObject();
                        String currentValue = "";
                        String currentDescriptorUri = obj.get("uri").getAsString();
                        //TODO change this into a constant
                        if(currentDescriptorUri.equals("http://www.semanticdesktop.org/ontologies/2007/01/19/nie#hasLogicalPart"))
                        {
                            currentValue = obj.get("value").getAsString();
                            childrenURIs = currentValue;
                        }
                    }*/

                    for(Iterator<JsonElement> it = descriptorsArray.iterator(); it.hasNext(); )
                    {
                        JsonElement elem = it.next();
                        JsonObject obj = elem.getAsJsonObject();
                        String currentTitle = "";
                        String currentDescriptorUri = obj.get("uri").getAsString();
                        //TODO change this into a constant
                        if(currentDescriptorUri.equals("http://www.semanticdesktop.org/ontologies/2007/01/19/nie#title"))
                        {
                            currentTitle = obj.get("value").getAsString();
                            //JsonObject mapperObject = new JsonObject();
                            mapperObject.addProperty(currentTitle, currentChildUri);
                            //mapperTitleAndUri.add(mapperObject);
                        }
                    }

                    /*
                    if(childrenURIs.size() >= mapperTitleAndUri.size())
                    {
                        updateChildrenMetadata(mapperTitleAndUri);
                    }*/
                    if(childrenURIs.size() >= mapperObject.size())
                    {
                        updateChildrenMetadata(mapperObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getChildInfo", error.getMessage());
            }

            @Override
            public void onProgressUpdate(int value) {

            }
        });
    }

    //public void updateChildrenMetadata(JsonArray mapperTitleAndUri)
    public void updateChildrenMetadata(JsonObject mapperTitleAndUri)
    {
        URL url;
        HttpURLConnection conn;
        for (DataItem dataItem : dataDescriptionItems) {

            ArrayList<DendroMetadataRecord> metadataRecords = new ArrayList<>();
            for (Descriptor desc : dataItem.getFileLevelMetadata()) {
                metadataRecords.add(
                        new DendroMetadataRecord(desc.getDescriptor(), desc.getValue())
                );
            }

            try {
                //String destPath = destUri + File.separator + dataItem.getResourceName() + "?update_metadata";
                String destPath = baseUrl + mapperTitleAndUri.get(dataItem.getResourceName()) + "?update_metadata";
                String dest = destPath.replace(" ", "%20");
                url = new URL(dest);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setRequestProperty("Cookie", cookie);
                conn.setDoOutput(true);

                String g = new Gson().toJson(metadataRecords, Utils.ARRAY_DENDRO_DESCRIPTORS);
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
                    //return null;
                }
            } catch (Exception e) {
                error = e;
                //return null;
            }
        }
    };

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