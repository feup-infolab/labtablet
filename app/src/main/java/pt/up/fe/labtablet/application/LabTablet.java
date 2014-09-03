package pt.up.fe.labtablet.application;

import android.app.Application;


//import org.acra.*;
//import org.acra.annotation.*;
/*
@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        formUri = "http://paginas.fe.up.pt/~ei08103/ACRA/dump.php"
)*/
public class LabTablet extends Application {

    public static final String TAG = LabTablet.class
            .getSimpleName();


    //private static LabTablet mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        //mInstance = this;
        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
    }

}
