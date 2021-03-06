package pt.up.fe.beta.labtablet.async;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.beta.labtablet.utils.Utils;

/**
 * Creates a KML file with the received coordinates
 */
public class AsyncKMLCreator extends AsyncTask<Object, Integer, String> {

    private final AsyncTaskHandler<String> mHandler;
    private Exception error;

    public AsyncKMLCreator(AsyncTaskHandler<String> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected String doInBackground(Object... params) {

        if (!(params[0] instanceof ArrayList) || !(params[1] instanceof String)) {
            Log.e("KML creator", "Wrong instances!");
            return null;
        }

        String folder = (String) params[1];
        ArrayList<Location> locations = (ArrayList<Location>) params[0];

        Element kml = new Element("kml");
        kml.setNamespace(Namespace.getNamespace("http://earth.google.com/kml/2.1"));

        if (locations.isEmpty()) {
            return null;
        }

        //document - placemark - name[] - description[] - LineString - coordinates

        Element placemark = new Element("Placemark");
        Element name = new Element("name");
        name.setText(Utils.getDate());
        placemark.addContent(name);

        Element lineString = new Element("LineString");
        Element coordinates = new Element("coordinates");

        for (Location loc : locations) {
            coordinates.addContent(loc.getLongitude() + "," + loc.getLatitude());
        }

        lineString.addContent(coordinates);
        placemark.addContent(lineString);
        kml.addContent(placemark);
        Log.i("XML", new XMLOutputter().outputString(kml));

        try {
            // create the XML Document in memory if the file does not exist
            // otherwise read the file from the disk
            String timeStamp = "" + new Date().getTime();
            File test = new File(folder + File.separator + timeStamp + ".kml");

            FileWriter fwriter = new FileWriter(test);
            XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
            xout.output(kml, fwriter);
            fwriter.flush();
            fwriter.close();
            return folder + File.separator + timeStamp + ".kml";
        } catch (Exception e) {
            error = e;
            Log.e("KML", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
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
