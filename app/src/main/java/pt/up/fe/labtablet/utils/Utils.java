package pt.up.fe.labtablet.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pt.up.fe.labtablet.models.AssociationItem;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Dendro.DendroDescriptor;
import pt.up.fe.labtablet.models.Dendro.DendroFolderItem;
import pt.up.fe.labtablet.models.Dendro.DendroMetadataRecord;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.Form;

public class Utils {

    //Contextual tags that will make the associated descriptor to be suggested
    //when the context applies
    public static final String AUDIO_TAGS = "audio";
    public static final String PICTURE_TAGS = "image";
    public static final String GEO_TAGS = "position";
    public static final String TEXT_TAGS = "text";
    public static final String MAGNETIC_TAGS = "magnetic";
    public static final String TEMP_TAGS = "temperature";
    public static final String TITLE_TAG = "title";
    public static final String DESCRIPTION_TAG = "description";
    public static final String GENERIC_TAG = "generic";
    public static final String CREATED_TAG = "created";

    //Extension used to state a certain file is a folder (when ls)
    public static final String DENDRO_FOLDER_EXTENSION = "folder";
    public static final String DENDRO_RESPONSE_ERROR = "error";
    public static final String DENDRO_RESPONSE_ERROR_2 = "Error";
    public static final String DENDRO_CONFS_ENTRY = "dendro_configurations";
    public static final int DESCRIPTOR_STATE_VALIDATED = 1;
    public static final int DESCRIPTOR_STATE_NOT_VALIDATED = 0;

    public static final List<String> knownImageMimeTypes = Arrays.asList("image/jpeg", "image/png");

    //When calling metadatavalidationActivity and in return geting an array with the
    //validated records;
    public static final int METADATA_VALIDATION = 2;
    //Pick an application profile to load
    public static final int PROFILE_PICK = 3;
    //Pick a descriptor and associate it with the extension
    public static final int DESCRIPTOR_ASSOCIATE = 4;
    //Capture image from camera
    public static final int CAMERA_INTENT_REQUEST = 5;
    //Launch sketchActivity
    public static final int SKETCH_INTENT_REQUEST = 6;
    //When the metadata edition changed the title
    public static final int BUILD_FORM_QUESTION = 8;
    //When the form solver activity is launched
    public static final int SOLVE_FORM = 9;
    //When an item preview is triggered
    public static final int ITEM_PREVIEW = 10;
    public static final int DATA_ITEM_CHANGED = 1;
    public static final int METADATA_ITEM_CHANGED = 0;

    public static final Type ARRAY_ASSOCIATION_ITEM = new TypeToken<ArrayList<AssociationItem>>() {
    }.getType();
    public static final Type ARRAY_CHANGELOG_ITEM = new TypeToken<ArrayList<ChangelogItem>>() {
    }.getType();
    public static final Type ARRAY_DESCRIPTORS = new TypeToken<ArrayList<Descriptor>>() {
    }.getType();
    public static final Type ARRAY_DIRECTORY_LISTING = new TypeToken<ArrayList<DendroFolderItem>>() {
    }.getType();
    public static final Type ARRAY_DENDRO_METADATA_RECORD = new TypeToken<ArrayList<DendroMetadataRecord>>() {
    }.getType();
    public static final Type ARRAY_FORM = new TypeToken<ArrayList<Form>>() {
    }.getType();

    // ---- Activities Results ---------
    public static final Type ARRAY_DENDRO_DESCRIPTORS = new TypeToken<ArrayList<DendroDescriptor>>() {
    }.getType();
    //Configuration entries. If updated, the application must be completely reinstalled
    //should not use previous instances
    public static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?";
    public static final String ASSOCIATIONS_CONFIG_ENTRY = "associations";

    public static final String BASE_DESCRIPTORS_ENTRY = "base_descriptors";
    public static final String BASE_FORMS_ENTRY = "base_forms";
    public static final String CHANGELOG_CONFIG_ENTRY = "changelogs";

    public static final long SAMPLE_MILLIS = 5000;
    //Select a descriptor and return it
    public static final int DESCRIPTOR_GET = 0;
    //Select a descriptor, define its value and return it
    public static final int DESCRIPTOR_DEFINE = 1;
    //Upload proccess
    public static final int SUBMISSION_VALIDATION = 7;
    //Pick a file from the storage
    public static final int PICK_FILE_INTENT = 8;


    public static String getDate() {
        return String.format("%tFT%<tRZ", new Date());
    }

    public static String getDate(long date) {
        return String.format("%tFT%<tRZ", date);
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static void openFile(Context context, File url) {
        // Create URI
        Uri uri = Uri.fromFile(url);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if(url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if(url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if(url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if(url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if(url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if(url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if(url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if(url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if(url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
